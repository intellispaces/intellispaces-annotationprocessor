package tech.intellispaces.annotationprocessor;

import tech.intellispaces.java.reflection.customtype.CustomType;

/**
 * The source artifact validator.
 */
public interface ArtifactValidator {

  void validate(CustomType sourceArtifact);
}
