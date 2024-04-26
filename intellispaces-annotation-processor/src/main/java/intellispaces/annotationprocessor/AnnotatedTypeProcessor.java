package intellispaces.annotationprocessor;

import intellispaces.annotationprocessor.artifact.Artifact;
import intellispaces.annotationprocessor.artifact.ArtifactTypes;
import intellispaces.annotationprocessor.artifact.JavaArtifact;
import intellispaces.annotationprocessor.maker.ArtifactMaker;
import intellispaces.commons.exception.UnexpectedViolationException;
import intellispaces.javastatements.JavaStatements;
import intellispaces.javastatements.statement.custom.CustomType;

import javax.annotation.processing.AbstractProcessor;
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
import java.util.List;
import java.util.Set;

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

  protected abstract boolean isApplicable(CustomType annotatedType);

  protected abstract List<ArtifactMaker> getArtifactMakers(CustomType customType);

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_17;
  }

  @Override
  public final Set<String> getSupportedAnnotationTypes() {
    return Set.of(annotation.getName());
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotatedTypeElements, RoundEnvironment roundEnv) {
    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotation)) {
      if (!applicableKinds.contains(annotatedElement.getKind())) {
        return true;
      }

      TypeElement typeElement = null;
      try {
        typeElement = (TypeElement) annotatedElement;
        CustomType annotatedType = JavaStatements.customTypeStatement(typeElement);
        if (!isApplicable(annotatedType)) {
          return true;
        }
        List<ArtifactMaker> makers = getArtifactMakers(annotatedType);
        for (ArtifactMaker maker : makers) {
          List<Artifact> artifacts = maker.make(annotatedType);
          for (Artifact artifact : artifacts) {
            writeArtifact(artifact);
          }
        }
      } catch (Exception e) {
        logError(typeElement, e);
      }
    }
    return true;
  }

  private void writeArtifact(Artifact artifact) throws IOException {
    if (artifact.type() == ArtifactTypes.JavaFile) {
      writeJavaArtifact(artifact.asJavaArtifact().orElseThrow());
    } else {
      throw UnexpectedViolationException.withMessage("Unsupported artifact type " + artifact.type().name());
    }
  }

  private void writeJavaArtifact(JavaArtifact artifact) throws IOException {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Write auto generated Java file " + artifact.canonicalName());
    JavaFileObject file = processingEnv.getFiler().createSourceFile(artifact.canonicalName());
    try (var writer = new PrintWriter(file.openWriter())) {
      writer.write(artifact.source());
    }
  }

  protected void logError(Element element, String message, Object... messageArguments) {
    var errorMessage = "ERROR. Failed to auto generate an artifact for element " + getElementName(element) +
        " marked with an annotation @" +  annotation.getSimpleName() + ". " +
        message.formatted(messageArguments);
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorMessage);
  }

  protected void logError(Element element, Exception ex) {
    var sw = new StringWriter();
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
}
