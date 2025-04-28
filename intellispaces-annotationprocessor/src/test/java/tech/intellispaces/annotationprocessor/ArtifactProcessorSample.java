package tech.intellispaces.annotationprocessor;

import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;

import tech.intellispaces.reflection.customtype.CustomType;

public class ArtifactProcessorSample extends ArtifactProcessor {

  public ArtifactProcessorSample() {
    super(Set.of(ElementKind.INTERFACE), AnnotationSample.class, SourceVersion.RELEASE_17);
  }

  @Override
  public boolean isApplicable(CustomType source) {
    return source.selectAnnotation(AnnotationSample.class).orElseThrow().enableAutoGenerate();
  }

  @Override
  public ArtifactValidator validator() {
    return null;
  }

  @Override
  public List<ArtifactGenerator> makeGenerators(CustomType source, ArtifactGeneratorContext context) {
    return List.of(new SampleArtifactGenerator(source));
  }
}
