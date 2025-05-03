package tech.intellispaces.annotationprocessor;

import java.util.Map;

import tech.intellispaces.jstatements.customtype.CustomType;

public class SampleArtifactGenerator extends TemplatedArtifactGenerator {
  private String sourceClassName;
  private String artifactName;

  public SampleArtifactGenerator(CustomType annotatedType) {
    super(annotatedType);
  }

  @Override
  protected ArtifactKind generatedArtifactKind() {
    return ArtifactKinds.JavaFile;
  }

  @Override
  public String generatedArtifactName() {
    return artifactName;
  }

  @Override
  public boolean isRelevant(ArtifactGeneratorContext context) {
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
  protected boolean analyzeSourceArtifact(ArtifactGeneratorContext context) {
    sourceClassName = sourceArtifact().canonicalName();
    artifactName = sourceArtifact().packageName() + ".GeneratedSample";
    return true;
  }
}
