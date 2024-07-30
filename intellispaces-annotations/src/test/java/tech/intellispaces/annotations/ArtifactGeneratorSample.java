package tech.intellispaces.annotations;

import tech.intellispaces.annotations.generator.TemplateBasedJavaArtifactGenerator;
import tech.intellispaces.javastatements.customtype.CustomType;

import javax.annotation.processing.RoundEnvironment;
import java.util.Map;

public class ArtifactGeneratorSample extends TemplateBasedJavaArtifactGenerator {
  private String sourceClassName;
  private String generatedClassName;

  public ArtifactGeneratorSample(CustomType annotatedType) {
    super(annotatedType);
  }

  @Override
  public String getArtifactName() {
    return generatedClassName;
  }

  @Override
  protected String templateName() {
    return "/sample.template";
  }

  protected Map<String, Object> templateVariables() {
    return Map.of("SOURCE_CLASS_NAME", sourceClassName);
  }

  @Override
  protected String canonicalName() {
    return generatedClassName;
  }

  @Override
  protected boolean analyzeAnnotatedType(RoundEnvironment roundEnv) {
    sourceClassName = annotatedType.canonicalName();
    generatedClassName = annotatedType.packageName() + ".GeneratedSample";
    return true;
  }
}
