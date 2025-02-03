package tech.intellispaces.commons.annotation.processor;

record ArtifactImpl(
    String name,
    char[] chars
) implements Artifact {
}