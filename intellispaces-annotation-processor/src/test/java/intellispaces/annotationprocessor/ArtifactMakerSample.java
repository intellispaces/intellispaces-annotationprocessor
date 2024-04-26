package intellispaces.annotationprocessor;

import intellispaces.annotationprocessor.maker.TemplateBasedJavaArtifactMaker;
import intellispaces.javastatements.statement.custom.CustomType;

import java.util.Map;

public class ArtifactMakerSample extends TemplateBasedJavaArtifactMaker {
  private String sourceClassName;
  private String generatedClassName;

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
  protected boolean analyze(CustomType annotatedType) {
    sourceClassName = annotatedType.canonicalName();
    generatedClassName = annotatedType.packageName() + ".GeneratedSample";
    return true;
  }
}
