package intellispaces.annotations;

import intellispaces.annotations.generator.GenerationTask;
import intellispaces.annotations.validator.AnnotatedTypeValidator;
import intellispaces.javastatements.customtype.CustomType;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import java.util.List;
import java.util.Set;

public class AnnotatedTypeProcessorSample extends AnnotatedTypeProcessor {

  public AnnotatedTypeProcessorSample() {
    super(AnnotationSample.class, Set.of(ElementKind.INTERFACE));
  }

  @Override
  public boolean isApplicable(CustomType annotatedType) {
    return annotatedType.selectAnnotation(AnnotationSample.class).orElseThrow().enableAutoGenerate();
  }

  @Override
  public AnnotatedTypeValidator getValidator() {
    return null;
  }

  @Override
  public List<GenerationTask> makeTasks(
      CustomType initiatorType, CustomType processedType, RoundEnvironment roundEnv
  ) {
    return List.of(new GenerationTaskSample(processedType));
  }
}
