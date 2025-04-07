package tech.intellispaces.annotationprocessor;

/**
 * The artifact.
 */
public interface Artifact {

  /**
   * The artifact type.
   */
  ArtifactKind type();

  /**
   * The full qualified artifact name.
   */
  String name();

  /**
   * The artifact characters.
   */
  char[] chars();
}
