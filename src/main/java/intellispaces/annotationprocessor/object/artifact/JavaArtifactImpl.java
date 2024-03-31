package intellispaces.annotationprocessor.object.artifact;

import intellispaces.annotationprocessor.model.artifact.JavaArtifact;

public record JavaArtifactImpl(
    String canonicalName,
    String javaSource
) implements JavaArtifact {
}