package intellispaces.annotationprocessor.maker;

import intellispaces.annotationprocessor.artifact.Artifact;
import intellispaces.javastatements.statement.custom.CustomType;

import java.util.List;

public interface ArtifactMaker {

  List<Artifact> make(CustomType annotatedType) throws Exception;
}
