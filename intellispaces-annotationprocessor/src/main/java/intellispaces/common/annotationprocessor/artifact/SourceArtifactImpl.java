package intellispaces.common.annotationprocessor.artifact;

public record SourceArtifactImpl(
    String name,
    String source
) implements SourceArtifact {

  @Override
  public ArtifactType type() {
    return ArtifactTypes.SourceArtifact;
  }
}