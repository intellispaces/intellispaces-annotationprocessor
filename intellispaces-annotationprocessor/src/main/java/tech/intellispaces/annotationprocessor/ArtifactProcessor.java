package tech.intellispaces.annotationprocessor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import tech.intellispaces.commons.exception.NotImplementedExceptions;
import tech.intellispaces.commons.exception.UnexpectedExceptions;
import tech.intellispaces.javareflection.JavaStatements;
import tech.intellispaces.javareflection.customtype.CustomType;

/**
 * The annotated artifact processor.
 * <p>
 * This annotation processor precessed annotated class, interface, record, enum and annotation artifacts.
 */
public abstract class ArtifactProcessor extends AbstractProcessor {
  private final Class<? extends Annotation> annotation;
  private final Set<ElementKind> applicableKinds;
  private final SourceVersion sourceVersion;

  private static final ArtifactProcessorContext CONTEXT = new ArtifactProcessorContext();

  public ArtifactProcessor(
      ElementKind applicableKind,
      Class<? extends Annotation> annotation,
      SourceVersion sourceVersion
  ) {
    this(Set.of(applicableKind), annotation, sourceVersion);
  }

  public ArtifactProcessor(
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
    logRoundStart(roundEnvironment);
    CONTEXT.roundEnvironments().add(roundEnvironment);
    for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(annotation)) {
      if (applicableKinds.contains(annotatedElement.getKind())) {
        processAnnotatedElement((TypeElement) annotatedElement);
      }
    }

    int numberGeneratedArtifacts = runGenerators();
    if (numberGeneratedArtifacts > 0) {
      prepareNotPenaltyRound();
    } else if (needPenaltyRound()) {
      preparePenaltyRound();
      return true;
    }
    if (isOverRound(roundEnvironment)) {
      checkNumberTasks();
    }
    return true;
  }

  private boolean needPenaltyRound() {
    return (CONTEXT.numberTasks() > 0 && !CONTEXT.isPenaltyRound());
  }

  private void preparePenaltyRound() {
    Optional<ArtifactGenerator> generator = penaltyRoundArtifactGenerator();
    if (generator.isPresent()) {
      try {
        var ctx = new ArtifactGeneratorContextImpl(CONTEXT);
        Artifact artifact = generator.get().generate(ctx).orElseThrow();
        writeArtifact(null, artifact);
        CONTEXT.setPenaltyRound(true);
      } catch (Exception e) {
        logErrorWhileWritingArtifact(null, e);
      }
    }
  }

  private void prepareNotPenaltyRound() {
    CONTEXT.setPenaltyRound(false);
  }

  protected Optional<ArtifactGenerator> penaltyRoundArtifactGenerator() {
    return Optional.empty();
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

      logInfo("Create new artifact generators for origin artifact %s", source.canonicalName());
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

  private int runGenerators() {
    int numberGeneratedArtifacts = 0;
    boolean anyTaskExecuted = false;
    Iterator<GenerationTask> iterator = CONTEXT.allTasks().iterator();
    while (iterator.hasNext()) {
      GenerationTask task = iterator.next();
      ArtifactGenerator generator = task.generator();
      var context = (ArtifactGeneratorContextImpl) task.context();
      if (generator.isRelevant(context)) {
        iterator.remove();
        if (runGenerator(task)) {
          numberGeneratedArtifacts++;
        }
        CONTEXT.finishTask(task);
        anyTaskExecuted = true;
      }
    }

    // Re-run remaining generators, as the context may have changed
    if (anyTaskExecuted) {
      numberGeneratedArtifacts += runGenerators();
    }
    return numberGeneratedArtifacts;
  }

  private boolean runGenerator(GenerationTask task) {
    ArtifactGenerator generator = task.generator();
    if (CONTEXT.isAlreadyGenerated(generator.generatedArtifactName())) {
      logWarn(task.source(), "Artifact %s has already been generated before", generator.generatedArtifactName());
      return false;
    }

    final Optional<Artifact> generatedArtifact;
    try {
      logInfo("Start to generate artifact %s", generator.generatedArtifactName());
      generatedArtifact = generator.generate(task.context());
    } catch (Exception e) {
      logErrorWhileGeneratingArtifact(task.source(), generator, e);
      return false;
    }

    if (generatedArtifact.isPresent()) {
      try {
        writeArtifact(task.source(), generatedArtifact.get());
        return true;
      } catch (Exception e) {
        logErrorWhileWritingArtifact(task.source(), e);
      }
    }
    return false;
  }

  private void writeArtifact(CustomType source, Artifact artifact) throws IOException {
    logInfo("Write auto generated file " + artifact.name());
    if (ArtifactKinds.JavaFile.equals(artifact.type())) {
      writeJavaArtifact(source, artifact);
    } else if (ArtifactKinds.ResourceFile.equals(artifact.type())) {
      writeResourceArtifact(source, artifact);
    } else {
      throw NotImplementedExceptions.withCode("rNvg0S23");
    }
  }

  private void writeJavaArtifact(CustomType source, Artifact artifact) throws IOException {
    JavaFileObject fileObject;
    try {
      Filer filer = processingEnv.getFiler();
      fileObject = filer.createSourceFile(artifact.name());
    } catch (FilerException e) {
      logWarn(source, "Failed to write generated Java source artifact. " + e.getMessage());
      return;
    }
    try (var writer = fileObject.openWriter()) {
      writer.write(artifact.chars());
    }
  }

  private void writeResourceArtifact(CustomType source, Artifact artifact) throws IOException {
    FileObject fileObject;
    try {
      Filer filer = processingEnv.getFiler();
      fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", artifact.name());
    } catch (FilerException e) {
      logWarn(source, "Failed to write generated resource artifact. " + e.getMessage());
      return;
    }
    try (var writer = fileObject.openWriter()) {
      writer.write(artifact.chars());
    }
  }

  private boolean isOverRound(RoundEnvironment roundEnvironment) {
    return roundEnvironment.processingOver();
  }

  private void checkNumberTasks() {
    if (CONTEXT.numberTasks() > 0) {
      logUnfinishedTasks();
    }
  }

  private void logRoundStart(RoundEnvironment roundEnvironment) {
    var sb = new StringBuilder();
    sb.append("Run annotation processor round #").append(CONTEXT.roundEnvironments().size());
    sb.append(". Annotation ").append(annotation.getCanonicalName());
    if (roundEnvironment.errorRaised()) {
      sb.append(". The error was found in the previous round. Check the previous messages");
    }
    if (CONTEXT.isPenaltyRound() && !isOverRound(roundEnvironment)) {
      sb.append(". Current round is penalty round");
    } else if (!CONTEXT.isPenaltyRound() && isOverRound(roundEnvironment)) {
      sb.append(". Current round is finally over round");
    } else if (CONTEXT.isPenaltyRound() && isOverRound(roundEnvironment)) {
      sb.append(". Current round is penalty and finally over round. Results of this round can be ignored");
    }
    if (roundEnvironment.errorRaised()) {
      logError(sb.toString());
    } else {
      logInfo(sb.toString());
    }
  }

  private void logUnfinishedTasks() {
    var sb = new StringBuilder();
    CONTEXT.allTasks().forEach(t -> sb
            .append("Generator ").append(t.generator().getClass().getCanonicalName())
            .append(". Generated artifact ").append(t.generator().generatedArtifactName())
            .append("\n")
    );
    logError("There are still not run generators: " + sb);
  }

  protected void log(Diagnostic.Kind level, String message, Object... messageArguments) {
    var fullMessage = message.formatted(messageArguments) + "\n";
    processingEnv.getMessager().printMessage(level, fullMessage);
  }

  protected void log(Diagnostic.Kind level, CustomType source, String message, Object... messageArguments) {
    var fullMessage = "Generate an artifact(s) for class " +
        (source != null ? source.canonicalName() : "<undefined>") +
        " marked with an annotation @" +  annotation.getSimpleName() + ". " +
        message.formatted(messageArguments) + "\n";
    log(level, fullMessage);
  }

  protected void logInfo(String message, Object... messageArguments) {
    log(Diagnostic.Kind.NOTE, message, messageArguments);
  }

  protected void logWarn(String message, Object... messageArguments) {
    log(Diagnostic.Kind.MANDATORY_WARNING, message, messageArguments);
  }

  protected void logWarn(CustomType source, String message, Object... messageArguments) {
    log(Diagnostic.Kind.MANDATORY_WARNING, source, message, messageArguments);
  }

  protected void logError(String message, Object... messageArguments) {
    log(Diagnostic.Kind.ERROR, message, messageArguments);
  }

  protected void logError(CustomType source, String message, Object... messageArguments) {
    log(Diagnostic.Kind.ERROR, source, message, messageArguments);
  }

  protected void logError(CustomType source, Exception ex) {
    var sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    logError(source, " Error: " + sw);
  }

  protected void logErrorWhileGeneratingArtifact(CustomType source, ArtifactGenerator task, Exception e) {
    var sw = new StringWriter();
    sw.write("Artifact generator " + task.getClass().getCanonicalName() + ": ");
    e.printStackTrace(new PrintWriter(sw));
    logError(source, sw.toString());
  }

  protected void logErrorWhileWritingArtifact(CustomType source, Exception e) {
    var sw = new StringWriter();
    sw.write("Could not write artifact.\n");
    e.printStackTrace(new PrintWriter(sw));
    logError(source, " Error: " + sw);
  }

  private static final Set<ElementKind> ALLOWABLE_ELEMENT_KINDS = Set.of(
      ElementKind.CLASS,
      ElementKind.INTERFACE,
      ElementKind.RECORD,
      ElementKind.ENUM,
      ElementKind.ANNOTATION_TYPE
  );
}
