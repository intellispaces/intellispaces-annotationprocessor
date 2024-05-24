package tech.intellispacesframework.annotationprocessor;

import tech.intellispacesframework.annotationprocessor.artifact.Artifact;
import tech.intellispacesframework.annotationprocessor.artifact.ArtifactTypes;
import tech.intellispacesframework.annotationprocessor.artifact.JavaArtifact;
import tech.intellispacesframework.annotationprocessor.generator.ArtifactGenerator;
import tech.intellispacesframework.commons.exception.UnexpectedViolationException;
import tech.intellispacesframework.javastatements.JavaStatements;
import tech.intellispacesframework.javastatements.statement.custom.CustomType;

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

  protected abstract List<ArtifactGenerator> makeArtifactGenerators(CustomType annotatedStatement);

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
      processCustomType((TypeElement) annotatedElement);
    }
    return true;
  }

  private void processCustomType(TypeElement annotatedElement) {
    TypeElement typeElement = null;
    try {
      typeElement = annotatedElement;
      CustomType annotatedType = JavaStatements.customTypeStatement(typeElement);
      if (!isApplicable(annotatedType)) {
        return;
      }
      List<ArtifactGenerator> generators = makeArtifactGenerators(annotatedType);
      for (ArtifactGenerator generator : generators) {
        Optional<Artifact> artifact = generator.generate();
        if (artifact.isPresent()) {
          writeArtifact(artifact.get());
        }
      }
    } catch (Exception e) {
      logError(typeElement, e);
    }
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
    var errorMessage = "ERROR. Failed to auto generate an artifact for class " + getElementName(element) +
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
