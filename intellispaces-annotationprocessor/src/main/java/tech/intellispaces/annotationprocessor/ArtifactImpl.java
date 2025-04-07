package tech.intellispaces.annotationprocessor;

public record ArtifactImpl(
    ArtifactKind type,
    String name,
    char[] chars
) implements Artifact {
}