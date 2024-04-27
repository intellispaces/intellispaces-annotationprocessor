package tech.intellispacesframework.annotationprocessor.artifact;

import java.util.Optional;

public interface JavaArtifact extends Artifact {

    /**
     * Java class canonical name.
     */
    String canonicalName();

    /**
     * Java artifact sourcecode.
     */
    String source();

    @Override
    default Optional<JavaArtifact> asJavaArtifact() {
        return Optional.of(this);
    }
}