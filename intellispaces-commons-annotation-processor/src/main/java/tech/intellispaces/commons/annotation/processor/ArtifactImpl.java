package tech.intellispaces.commons.annotation.processor;

public record ArtifactImpl(
    ArtifactType type,
    String name,
    char[] chars
) implements Artifact {
}