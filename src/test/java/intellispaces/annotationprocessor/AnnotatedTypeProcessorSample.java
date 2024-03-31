package intellispaces.annotationprocessor;

import intellispaces.annotationprocessor.model.maker.ArtifactMaker;
import intellispaces.javastatements.model.custom.CustomType;
import intellispaces.javastatements.object.StatementTypes;

import javax.lang.model.element.ElementKind;
import java.util.List;
import java.util.Set;

public class AnnotatedTypeProcessorSample extends AnnotatedTypeProcessor {

  public AnnotatedTypeProcessorSample() {
    super(AnnotationSample.class, Set.of(ElementKind.INTERFACE));
  }

  @Override
  protected boolean isApplicable(CustomType annotatedType) {
    return StatementTypes.Interface == annotatedType.statementType();
  }

  @Override
  protected List<ArtifactMaker> getArtifactMakers(CustomType customType) {
    return List.of(new TemplatedJavaArtifactMakerSample());
  }
}
