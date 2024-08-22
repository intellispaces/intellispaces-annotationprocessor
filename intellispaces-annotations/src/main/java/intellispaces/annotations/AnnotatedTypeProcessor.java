package intellispaces.annotations;

import intellispaces.annotations.artifact.ArtifactTypes;
import intellispaces.annotations.validator.AnnotatedTypeValidator;
import intellispaces.annotations.artifact.Artifact;
import intellispaces.annotations.artifact.JavaArtifact;
import intellispaces.annotations.generator.ArtifactGenerator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Annotation processor of annotated types (annotated classes, interfaces, records, enums and annotations).
 */
public abstract class AnnotatedTypeProcessor extends AbstractProcessor {
  private final Class<? extends Annotation> annotation;
  private final Set<ElementKind> applicableKinds;

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

  protected abstract boolean isApplicable(CustomType annotatedStatement);

  protected abstract AnnotatedTypeValidator getValidator();

  protected abstract List<ArtifactGenerator> makeArtifactGenerators(
      CustomType annotatedStatement, RoundEnvironment roundEnv
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
    TypeElement typeElement = null;
    try {
      typeElement = annotatedElement;
      CustomType annotatedType = JavaStatements.customTypeStatement(typeElement);
      if (!isApplicable(annotatedType)) {
        return;
      }

      AnnotatedTypeValidator validator = getValidator();
      if (validator != null) {
        validator.validate(annotatedType);
      }

      List<ArtifactGenerator> generators = makeArtifactGenerators(annotatedType, roundEnv);
      for (ArtifactGenerator generator : generators) {
        String artifactName = generator.getArtifactName();
        if (GENERATED_ARTIFACT_CACHE.contains(artifactName)) {
          log(Diagnostic.Kind.NOTE, typeElement, "Artifact %s has already been generated before", artifactName);
          continue;
        }

        final Optional<Artifact> artifact;
        try {
          artifact = generator.generate(roundEnv);
        } catch (Exception e) {
          logErrorWhileGeneratingArtifact(typeElement, generator, e);
          return;
        }

        if (artifact.isPresent()) {
          try {
            writeArtifact(annotatedElement, artifact.get());
            GENERATED_ARTIFACT_CACHE.add(artifactName);
          } catch (Exception e) {
            logErrorWhileWritingArtifact(typeElement, e);
          }
        }
      }
    } catch (Exception e) {
      logError(typeElement, e);
    }
  }

  private void writeArtifact(TypeElement annotatedElement, Artifact artifact) throws IOException {
    if (artifact.type() == ArtifactTypes.JavaFile) {
      writeJavaArtifact(annotatedElement, artifact.asJavaArtifact().orElseThrow());
    } else {
      throw UnexpectedViolationException.withMessage("Unsupported artifact type " + artifact.type().name());
    }
  }

  private void writeJavaArtifact(TypeElement annotatedElement, JavaArtifact artifact) throws IOException {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
        "Write auto generated Java file " + artifact.canonicalName());
    Filer filer = processingEnv.getFiler();
    JavaFileObject fileObject;
    try {
      fileObject = filer.createSourceFile(artifact.canonicalName());
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

  protected void logErrorWhileGeneratingArtifact(Element element, ArtifactGenerator generator, Exception ex) {
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

  private static final Set<String> GENERATED_ARTIFACT_CACHE = new HashSet<>();
}
