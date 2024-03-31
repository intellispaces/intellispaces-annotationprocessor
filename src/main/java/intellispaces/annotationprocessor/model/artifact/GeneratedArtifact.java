package intellispaces.annotationprocessor.model.artifact;

import java.util.Optional;

public interface GeneratedArtifact {

  GeneratedArtifactType type();

  Optional<JavaArtifact> asJavaArtifact();
}
