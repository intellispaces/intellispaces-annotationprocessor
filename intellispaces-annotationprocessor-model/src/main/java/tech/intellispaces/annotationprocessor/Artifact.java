package tech.intellispaces.annotationprocessor;

/**
 * The artifact.
 */
public interface Artifact {

  /**
   * The full qualified artifact name.
   */
  String name();

  /**
   * The artifact characters.
   */
  char[] chars();
}
