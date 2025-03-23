package tech.intellispaces.commons.annotation.processor;

import tech.intellispaces.commons.exception.UnexpectedExceptions;
import tech.intellispaces.commons.reflection.JavaStatements;
import tech.intellispaces.commons.reflection.customtype.CustomType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The processor of annotated class, interface, record, enum or annotation artifacts.
 */
public abstract class AnnotatedArtifactProcessor extends AbstractProcessor {
  private final Class<? extends Annotation> annotation;
  private final Set<ElementKind> applicableKinds;
  private final SourceVersion sourceVersion;

  public AnnotatedArtifactProcessor(
      ElementKind applicableKind,
      Class<? extends Annotation> annotation,
      SourceVersion sourceVersion
  ) {
    this(Set.of(applicableKind), annotation, sourceVersion);
  }

  public AnnotatedArtifactProcessor(
      Set<ElementKind> applicableKinds,
      Class<? extends Annotation> annotation,
      SourceVersion sourceVersion
  ) {
    for (ElementKind elementKind : applicableKinds) {
      if (!ALLOWABLE_ELEMENT_KINDS.contains(elementKind)) {
        throw UnexpectedExceptions.withMessage("Unsupported element kind {0}", elementKind);
      }
    }
    this.applicableKinds = applicableKinds;
    this.annotation = annotation;
    this.sourceVersion = sourceVersion;
  }

  public abstract boolean isApplicable(CustomType source);

  /**
   * Returns source artifact validator or <code>null</code> if is not applicable.
   */
  public abstract ArtifactValidator validator();

  /**
   * Creates new artifact generators.
   *
   * @param source the source artifact.
   * @param context the generator context.
   * @return the list of generators.
   */
  public abstract List<ArtifactGenerator> makeGenerators(CustomType source, ArtifactGeneratorContext context);

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return sourceVersion;
  }

  @Override
  public final Set<String> getSupportedAnnotationTypes() {
    return Set.of(annotation.getName());
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
    CONTEXT.roundEnvironments().add(roundEnvironment);

    for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(annotation)) {
      if (applicableKinds.contains(annotatedElement.getKind())) {
        processAnnotatedElement((TypeElement) annotatedElement);
      }
    }

    runGenerators();

    if (isLastRound(roundEnvironment)) {
      checkRemainingGenerators();
    }
    return true;
  }

  private void processAnnotatedElement(TypeElement annotatedElement) {
    CustomType source = JavaStatements.customTypeStatement(annotatedElement);
    try {
      if (!isApplicable(source)) {
        return;
      }

      ArtifactValidator validator = validator();
      if (validator != null) {
        validator.validate(source);
      }

      log(Diagnostic.Kind.NOTE, "Create new artifact generators for origin artifact %s", source.canonicalName());
      createGenerators(source);
    } catch (Exception e) {
      logError(source, e);
    }
  }

  private void createGenerators(CustomType source) {
    var context = new ArtifactGeneratorContextImpl(CONTEXT);
    List<ArtifactGenerator> generators = makeGenerators(source, context);
    List<GenerationTask> tasks = generators.stream()
      .map(generator -> new GenerationTask(source, annotation, generator, context))
      .toList();
    CONTEXT.addTasks(source, annotation, tasks);
  }

  private void runGenerators() {
    boolean anyTaskExecuted = false;
    Iterator<GenerationTask> iterator = CONTEXT.allTasks().iterator();
    while (iterator.hasNext()) {
      GenerationTask task = iterator.next();
      ArtifactGenerator generator = task.generator();
      var context = (ArtifactGeneratorContextImpl) task.context();
      if (generator.isRelevant(context)) {
        iterator.remove();
        runGenerator(task);
        CONTEXT.finishTask(task);
        anyTaskExecuted = true;
      }
    }

    // Re-run remaining generators, as the context may have changed
    if (anyTaskExecuted) {
      runGenerators();
    }
  }

  private void runGenerator(GenerationTask task) {
    ArtifactGenerator generator = task.generator();
    if (CONTEXT.isAlreadyGenerated(generator.generatedArtifactName())) {
      log(Diagnostic.Kind.NOTE, task.source(), "Artifact %s has already been generated before",
          generator.generatedArtifactName());
      return;
    }

    final Optional<Artifact> generatedArtifact;
    try {
      log(Diagnostic.Kind.NOTE, "Start to generate artifact %s", generator.generatedArtifactName());
      generatedArtifact = generator.generate(task.context());
    } catch (Exception e) {
      logErrorWhileGeneratingArtifact(task.source(), generator, e);
      return;
    }

    if (generatedArtifact.isPresent()) {
      try {
        writeArtifact(task.source(), generatedArtifact.get());
      } catch (Exception e) {
        logErrorWhileWritingArtifact(task.source(), e);
      }
    }
  }

  private void writeArtifact(CustomType source, Artifact artifact) throws IOException {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Write auto generated file " + artifact.name());
    Filer filer = processingEnv.getFiler();
    JavaFileObject fileObject;
    try {
      fileObject = filer.createSourceFile(artifact.name());
    } catch (FilerException e) {
      logMandatoryWarning(source, "Failed to write generated artifact file. " + e.getMessage());
      return;
    }
    try (var writer = fileObject.openWriter()) {
      writer.write(artifact.chars());
    }
  }

  private boolean isLastRound(RoundEnvironment roundEnvironment) {
    return roundEnvironment.processingOver();
  }

  private void checkRemainingGenerators() {
    if (CONTEXT.numberTasks() > 0) {
      var sb = new StringBuilder();
      CONTEXT.allTasks().forEach(t -> sb
          .append("Generator ").append(t.generator().getClass().getCanonicalName())
          .append(". Generated artifact ").append(t.generator().generatedArtifactName())
          .append("\n")
      );
      log(Diagnostic.Kind.ERROR, "There are still not run generators: " + sb);
    }
  }

  protected void log(Diagnostic.Kind level, String message, Object... messageArguments) {
    var fullMessage = "[" + new Date() + "] " + message.formatted(messageArguments);
    processingEnv.getMessager().printMessage(level, fullMessage);
  }

  protected void log(Diagnostic.Kind level, CustomType source, String message, Object... messageArguments) {
    var fullMessage = "Generate an artifact(s) for class " + source.canonicalName() +
        " marked with an annotation @" +  annotation.getSimpleName() + ". " +
        message.formatted(messageArguments);
    log(level, fullMessage);
  }

  protected void logMandatoryWarning(CustomType source, String message, Object... messageArguments) {
    log(Diagnostic.Kind.MANDATORY_WARNING, source, message, messageArguments);
  }

  protected void logError(CustomType source, String message, Object... messageArguments) {
    log(Diagnostic.Kind.ERROR, source, message, messageArguments);
  }

  protected void logError(CustomType source, Exception ex) {
    var sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    logError(source, sw.toString());
  }

  protected void logErrorWhileGeneratingArtifact(CustomType source, ArtifactGenerator task, Exception e) {
    var sw = new StringWriter();
    sw.write("Artifact generator " + task.getClass().getCanonicalName() + ".\n");
    e.printStackTrace(new PrintWriter(sw));
    logError(source, sw.toString());
  }

  protected void logErrorWhileWritingArtifact(CustomType source, Exception e) {
    var sw = new StringWriter();
    sw.write("Could not write artifact.\n");
    e.printStackTrace(new PrintWriter(sw));
    logError(source, sw.toString());
  }

  private static final ProcessorContext CONTEXT = new ProcessorContext();

  private static final Set<ElementKind> ALLOWABLE_ELEMENT_KINDS = Set.of(
      ElementKind.CLASS,
      ElementKind.INTERFACE,
      ElementKind.RECORD,
      ElementKind.ENUM,
      ElementKind.ANNOTATION_TYPE
  );
}
