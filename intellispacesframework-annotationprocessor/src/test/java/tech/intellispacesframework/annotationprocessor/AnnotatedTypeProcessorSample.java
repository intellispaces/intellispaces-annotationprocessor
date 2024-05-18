package tech.intellispacesframework.annotationprocessor;

import tech.intellispacesframework.annotationprocessor.maker.ArtifactMaker;
import tech.intellispacesframework.javastatements.statement.custom.CustomType;

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
  protected List<ArtifactMaker> getArtifactMakers(CustomType annotatedType) {
    return List.of(new ArtifactMakerSample(annotatedType));
  }
}
