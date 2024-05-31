package tech.intellispaces.framework.annotationprocessor.artifact;

public record JavaArtifactImpl(
    String canonicalName,
    String source
) implements JavaArtifact {

  @Override
  public ArtifactType type() {
    return ArtifactTypes.JavaFile;
  }
}