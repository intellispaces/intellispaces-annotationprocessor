package intellispaces.annotationprocessor.model.maker;

import intellispaces.annotationprocessor.model.artifact.GeneratedArtifact;
import intellispaces.javastatements.model.custom.CustomType;

import java.util.List;

public interface ArtifactMaker {

  List<GeneratedArtifact> make(CustomType annotatedType) throws Exception;
}
