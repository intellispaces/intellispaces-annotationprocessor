package tech.intellispaces.java.annotation.artifact;

import java.util.Optional;

public interface SourceArtifact extends Artifact {

    /**
     * Artifact sourcecode.
     */
    String source();

    @Override
    default Optional<SourceArtifact> asSourceArtifact() {
        return Optional.of(this);
    }
}