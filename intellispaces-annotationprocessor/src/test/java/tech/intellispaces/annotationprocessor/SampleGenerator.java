package tech.intellispaces.annotationprocessor;

import tech.intellispaces.annotationprocessor.context.AnnotationProcessingContext;
import tech.intellispaces.annotationprocessor.generator.TemplatedGenerator;
import tech.intellispaces.java.reflection.customtype.CustomType;

import javax.annotation.processing.RoundEnvironment;
import java.util.Map;

public class SampleGenerator extends TemplatedGenerator {
  private String sourceClassName;
  private String generatedClassName;

  public SampleGenerator(CustomType annotatedType) {
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
