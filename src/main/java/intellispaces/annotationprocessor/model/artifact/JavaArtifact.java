package intellispaces.annotationprocessor.model.artifact;

import intellispaces.annotationprocessor.object.artifact.GeneratedArtifactTypes;

import java.util.Optional;

public interface JavaArtifact extends GeneratedArtifact {

    @Override
    default GeneratedArtifactType type() {
        return GeneratedArtifactTypes.Java;
    }

    @Override
    default Optional<JavaArtifact> asJavaArtifact() {
        return Optional.of(this);
    }

    String canonicalName();

    String javaSource();
}