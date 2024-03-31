package intellispaces.annotationprocessor;

import intellispaces.annotationprocessor.object.maker.TemplatedJavaArtifactMaker;
import intellispaces.javastatements.model.custom.CustomType;

import java.util.Map;

public class TemplatedJavaArtifactMakerSample extends TemplatedJavaArtifactMaker {
  private String sourceClassName;
  private String generatedClassName;

  @Override
  protected String templateName() {
    return "/sample.template";
  }

  @Override
  protected Map<String, Object> templateParams() {
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
