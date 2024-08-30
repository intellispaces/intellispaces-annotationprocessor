package intellispaces.annotations;

import intellispaces.annotations.artifact.Artifact;
import intellispaces.annotations.artifact.ArtifactTypes;
import intellispaces.annotations.artifact.SourceArtifact;
import intellispaces.annotations.context.AnnotationProcessingContextImpl;
import intellispaces.annotations.generator.GenerationTask;
import intellispaces.annotations.validator.AnnotatedTypeValidator;
import intellispaces.commons.exception.UnexpectedViolationException;
import intellispaces.javastatements.JavaStatements;
import intellispaces.javastatements.customtype.CustomType;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Annotation processor of annotated types (annotated classes, interfaces, records, enums and annotations).
 */
public abstract class AnnotatedTypeProcessor extends AbstractProcessor {
  private final Class<? extends Annotation> annotation;
  private final Set<ElementKind> applicableKinds;

  private static final AnnotationProcessingState STATE = new AnnotationProcessingState();

  private static final Set<ElementKind> ENABLED_ELEMENT_KINDS = Set.of(
      ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.RECORD, ElementKind.ENUM, ElementKind.ANNOTATION_TYPE
  );

  public AnnotatedTypeProcessor(Class<? extends Annotation> annotation, Set<ElementKind> applicableKinds) {
    this.annotation = annotation;
    for (ElementKind elementKind : applicableKinds) {
      if (!ENABLED_ELEMENT_KINDS.contains(elementKind)) {
        throw UnexpectedViolationException.withMessage("Unsupported element kind {}", elementKind);
      }
    }
    this.applicableKinds = applicableKinds;
  }

  public abstract boolean isApplicable(CustomType annotatedStatement);

  public abstract AnnotatedTypeValidator getValidator();

  public abstract List<GenerationTask> makeTasks(
      CustomType initiatorType, CustomType processedType, RoundEnvironment roundEnv
  );

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_17;
  }

  @Override
  public final Set<String> getSupportedAnnotationTypes() {
    return Set.of(annotation.getName());
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotation)) {
      if (!applicableKinds.contains(annotatedElement.getKind())) {
        continue;
      }
      processCustomType((TypeElement) annotatedElement, roundEnv);
    }
    return true;
  }

  private void processCustomType(TypeElement annotatedElement, RoundEnvironment roundEnv) {
    try {
      CustomType annotatedType = JavaStatements.customTypeStatement(annotatedElement);
      if (!isApplicable(annotatedType)) {
        return;
      }

      AnnotatedTypeValidator validator = getValidator();
      if (validator != null) {
        validator.validate(annotatedType);
      }

      List<GenerationTask> tasks = makeTasks(annotatedType, annotatedType, roundEnv);
      for (GenerationTask task : tasks) {
        if (!task.isRelevant(STATE.context())) {
          STATE.deferredTasks().add(new TaskDescription(annotation, annotatedElement, tasks.size(), task));
          continue;
        }
        executeTask(annotatedElement, task, tasks.size(), roundEnv);
        processDeferredTasks(roundEnv);
      }
    } catch (Exception e) {
      logError(annotatedElement, e);
    }
  }

  private void executeTask(
      TypeElement annotatedElement, GenerationTask task, int numberTasks, RoundEnvironment roundEnv
  ) {
    if (STATE.context().isGenerated(task.artifactName())) {
      log(Diagnostic.Kind.NOTE, annotatedElement, "Artifact %s has already been generated before", task.artifactName());
      return;
    }

    final Optional<Artifact> artifact;
    try {
      artifact = task.execute(roundEnv);
    } catch (Exception e) {
      logErrorWhileGeneratingArtifact(annotatedElement, task, e);
      return;
    }

    if (artifact.isPresent()) {
      try {
        writeArtifact(annotatedElement, artifact.get());
      } catch (Exception e) {
        logErrorWhileWritingArtifact(annotatedElement, e);
      }
    }

    STATE.context().finishTask(annotation, numberTasks, task);
  }

  private void processDeferredTasks(RoundEnvironment roundEnv) {
    Iterator<TaskDescription> iterator = STATE.deferredTasks().iterator();
    while (iterator.hasNext()) {
      TaskDescription taskDescription = iterator.next();
      if (taskDescription.task().isRelevant(STATE.context())) {
        iterator.remove();
        executeTask(taskDescription.annotatedElement(), taskDescription.task(), taskDescription.numberTasks(), roundEnv);
      }
    }
  }

  private void writeArtifact(TypeElement annotatedElement, Artifact artifact) throws IOException {
    if (artifact.type() == ArtifactTypes.SourceArtifact) {
      writeJavaArtifact(annotatedElement, artifact.asSourceArtifact().orElseThrow());
    } else {
      throw UnexpectedViolationException.withMessage("Unsupported artifact type " + artifact.type().name());
    }
  }

  private void writeJavaArtifact(TypeElement annotatedElement, SourceArtifact artifact) throws IOException {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
        "Write auto generated Java file " + artifact.name());
    Filer filer = processingEnv.getFiler();
    JavaFileObject fileObject;
    try {
      fileObject = filer.createSourceFile(artifact.name());
    } catch (FilerException e) {
      logMandatoryWarning(annotatedElement, "Failed to write generated source JAVA file. " + e.getMessage());
      return;
    }
    try (var writer = new PrintWriter(fileObject.openWriter())) {
      writer.write(artifact.source());
    }
  }

  protected void logMandatoryWarning(Element element, String message, Object... messageArguments) {
    log(Diagnostic.Kind.MANDATORY_WARNING, element, message, messageArguments);
  }

  protected void logError(Element element, String message, Object... messageArguments) {
    log(Diagnostic.Kind.ERROR, element, message, messageArguments);
  }

  protected void log(Diagnostic.Kind level, Element element, String message, Object... messageArguments) {
    var errorMessage = "Process to generate an artifact(s) for class " + getElementName(element) +
        " marked with an annotation @" +  annotation.getSimpleName() + ". " +
        message.formatted(messageArguments);
    processingEnv.getMessager().printMessage(level, errorMessage);
  }

  protected void logError(Element element, Exception ex) {
    var sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    logError(element, sw.toString());
  }

  protected void logErrorWhileGeneratingArtifact(Element element, GenerationTask generator, Exception ex) {
    var sw = new StringWriter();
    sw.write("Artifact generator " + generator.getClass().getCanonicalName() + ".\n");
    ex.printStackTrace(new PrintWriter(sw));
    logError(element, sw.toString());
  }

  protected void logErrorWhileWritingArtifact(Element element, Exception ex) {
    var sw = new StringWriter();
    sw.write("Could not write artifact.\n");
    ex.printStackTrace(new PrintWriter(sw));
    logError(element, sw.toString());
  }

  private String getElementName(Element element) {
    if (element instanceof TypeElement) {
      return ((TypeElement) element).getQualifiedName().toString();
    } else {
      return element.getSimpleName().toString();
    }
  }

  private record AnnotationProcessingState(
      AnnotationProcessingContextImpl context,
      List<TaskDescription> deferredTasks
  ) {
    public AnnotationProcessingState() {
      this(new AnnotationProcessingContextImpl(), new LinkedList<>());
    }

    private AnnotationProcessingState(
        AnnotationProcessingContextImpl context, List<TaskDescription> deferredTasks
    ) {
      this.context = context;
      this.deferredTasks = deferredTasks;
    }
  }

  private record TaskDescription(
      Class<? extends Annotation> annotation,
      TypeElement annotatedElement,
      int numberTasks,
      GenerationTask task
  ) {}
}
