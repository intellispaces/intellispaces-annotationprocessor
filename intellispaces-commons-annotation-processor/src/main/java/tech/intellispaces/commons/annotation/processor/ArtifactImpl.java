package tech.intellispaces.commons.annotation.processor;

public record ArtifactImpl(
    ArtifactKind type,
    String name,
    char[] chars
) implements Artifact {
}