package tech.intellispaces.annotationprocessor;

import tech.intellispaces.general.exception.UnexpectedExceptions;
import tech.intellispaces.java.reflection.JavaStatements;
import tech.intellispaces.java.reflection.customtype.CustomType;

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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The processor of annotated class, interface, record, enum or annotation artifacts.
 */
public abstract class ArtifactProcessor extends AbstractProcessor {
  private final Class<? extends Annotation> annotation;
  private final Set<ElementKind> applicableKinds;
  private final SourceVersion sourceVersion;

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
   * Creates artifact generators.
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
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotation)) {
      if (applicableKinds.contains(annotatedElement.getKind())) {
        processAnnotatedElement((TypeElement) annotatedElement, roundEnv);
      }
    }
    return true;
  }

  private void processAnnotatedElement(TypeElement annotatedElement, RoundEnvironment roundEnv) {
    CustomType source = JavaStatements.customTypeStatement(annotatedElement);
    try {
      if (!isApplicable(source)) {
        return;
      }

      ArtifactValidator validator = validator();
      if (validator != null) {
        validator.validate(source);
      }

      createTasks(source, roundEnv);

      processTasks();
    } catch (Exception e) {
      logError(source, e);
    }
  }

  private void createTasks(CustomType source, RoundEnvironment roundEnv) {
    var context = new ArtifactGeneratorContextImpl(roundEnv, PROCESSING_CONTEXT);
    List<ArtifactGenerator> generators = makeGenerators(source, context);
    List<Task> tasks = generators.stream()
      .map(generator -> new Task(generator, context, source, annotation))
      .toList();
    PROCESSING_CONTEXT.addTasks(source, annotation, tasks);
  }

  private void processTasks() {
    boolean anyTaskExecuted = false;
    Iterator<Task> iterator = PROCESSING_CONTEXT.allTasks().iterator();
    while (iterator.hasNext()) {
      Task task = iterator.next();
      ArtifactGenerator generator = task.generator();
      ArtifactGeneratorContext context = task.context();
      if (generator.isRelevant(context)) {
        iterator.remove();
        executeTask(task);
        PROCESSING_CONTEXT.finishTask(task);
        anyTaskExecuted = true;
      }
    }
    if (anyTaskExecuted) {
      processTasks();
    }
  }

  private void executeTask(Task task) {
    ArtifactGenerator generator = task.generator();
    if (PROCESSING_CONTEXT.isAlreadyGenerated(generator.generatedArtifactName())) {
      log(Diagnostic.Kind.NOTE, task.source(), "Artifact %s has already been generated before",
          generator.generatedArtifactName());
      return;
    }

    final Optional<Artifact> generatedArtifact;
    try {
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

  protected void logMandatoryWarning(CustomType source, String message, Object... messageArguments) {
    log(Diagnostic.Kind.MANDATORY_WARNING, source, message, messageArguments);
  }

  protected void logError(CustomType source, String message, Object... messageArguments) {
    log(Diagnostic.Kind.ERROR, source, message, messageArguments);
  }

  protected void log(Diagnostic.Kind level, CustomType source, String message, Object... messageArguments) {
    var errorMessage = "Generate an artifact(s) for class " + source.canonicalName() +
        " marked with an annotation @" +  annotation.getSimpleName() + ". " +
        message.formatted(messageArguments);
    processingEnv.getMessager().printMessage(level, errorMessage);
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

  private static final ProcessingContext PROCESSING_CONTEXT = new ProcessingContext();

  private static final Set<ElementKind> ALLOWABLE_ELEMENT_KINDS = Set.of(
      ElementKind.CLASS,
      ElementKind.INTERFACE,
      ElementKind.RECORD,
      ElementKind.ENUM,
      ElementKind.ANNOTATION_TYPE
  );
}
