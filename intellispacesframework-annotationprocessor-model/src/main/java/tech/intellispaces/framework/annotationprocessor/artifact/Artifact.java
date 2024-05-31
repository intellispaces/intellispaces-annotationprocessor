package tech.intellispaces.framework.annotationprocessor.artifact;

import java.util.Optional;

public interface Artifact {

  ArtifactType type();

  Optional<JavaArtifact> asJavaArtifact();
}
