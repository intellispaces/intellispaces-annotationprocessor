package tech.intellispaces.java.annotation.artifact;

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
