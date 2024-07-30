package tech.intellispaces.annotations;

import tech.intellispaces.annotations.generator.ArtifactGenerator;
import tech.intellispaces.annotations.validator.AnnotatedTypeValidator;
import tech.intellispaces.javastatements.customtype.CustomType;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import java.util.List;
import java.util.Set;

public class AnnotatedTypeProcessorSample extends AnnotatedTypeProcessor {

  public AnnotatedTypeProcessorSample() {
    super(AnnotationSample.class, Set.of(ElementKind.INTERFACE));
  }

  @Override
  protected boolean isApplicable(CustomType annotatedType) {
    return annotatedType.selectAnnotation(AnnotationSample.class).orElseThrow().enableAutoGenerate();
  }

  @Override
  protected AnnotatedTypeValidator getValidator() {
    return null;
  }

  @Override
  protected List<ArtifactGenerator> makeArtifactGenerators(
      CustomType annotatedType, RoundEnvironment roundEnv
  ) {
    return List.of(new ArtifactGeneratorSample(annotatedType));
  }
}
