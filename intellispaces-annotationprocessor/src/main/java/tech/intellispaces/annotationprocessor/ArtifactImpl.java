package tech.intellispaces.annotationprocessor;

record ArtifactImpl(
    String name,
    char[] chars
) implements Artifact {
}