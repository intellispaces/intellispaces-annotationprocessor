package tech.intellispaces.annotationprocessor.artifact;

import java.util.Optional;

public interface Artifact {

  /**
   * Artifact name.
   */
  String name();

  /**
   * Artifact type.
   */
  ArtifactType type();

  Optional<SourceArtifact> asSourceArtifact();
}
