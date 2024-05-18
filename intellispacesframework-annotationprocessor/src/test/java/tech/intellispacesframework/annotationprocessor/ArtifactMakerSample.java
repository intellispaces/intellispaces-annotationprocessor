package tech.intellispacesframework.annotationprocessor;

import tech.intellispacesframework.annotationprocessor.maker.TemplateBasedJavaArtifactMaker;
import tech.intellispacesframework.javastatements.statement.custom.CustomType;

import java.util.Map;

public class ArtifactMakerSample extends TemplateBasedJavaArtifactMaker {
  private String sourceClassName;
  private String generatedClassName;

  public ArtifactMakerSample(CustomType annotatedType) {
    super(annotatedType);
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
  protected boolean analyze() {
    sourceClassName = annotatedType.canonicalName();
    generatedClassName = annotatedType.packageName() + ".GeneratedSample";
    return true;
  }
}
