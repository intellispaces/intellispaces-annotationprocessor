package intellispaces.annotations;

import intellispaces.annotations.context.AnnotationProcessingContext;
import intellispaces.annotations.generator.TemplateSourceArtifactGenerationTask;
import intellispaces.javastatements.customtype.CustomType;

import javax.annotation.processing.RoundEnvironment;
import java.util.Map;

public class GenerationTaskSample extends TemplateSourceArtifactGenerationTask {
  private String sourceClassName;
  private String generatedClassName;

  public GenerationTaskSample(CustomType annotatedType) {
    super(annotatedType, annotatedType);
  }

  @Override
  public String artifactName() {
    return generatedClassName;
  }

  @Override
  public boolean isRelevant(AnnotationProcessingContext context) {
    return true;
  }

  @Override
  protected String templateName() {
    return "/sample.template";
  }

  protected Map<String, Object> templateVariables() {
    return Map.of("SOURCE_CLASS_NAME", sourceClassName);
  }

  @Override
  protected boolean analyzeAnnotatedType(RoundEnvironment roundEnv) {
    sourceClassName = annotatedType.canonicalName();
    generatedClassName = annotatedType.packageName() + ".GeneratedSample";
    return true;
  }
}
