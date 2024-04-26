package intellispaces.annotationprocessor.artifact;

import java.util.Optional;

public interface Artifact {

  ArtifactType type();

  Optional<JavaArtifact> asJavaArtifact();
}
