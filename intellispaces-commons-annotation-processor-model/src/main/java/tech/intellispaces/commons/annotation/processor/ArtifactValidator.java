package tech.intellispaces.commons.annotation.processor;

import tech.intellispaces.commons.reflection.customtype.CustomType;

/**
 * The source artifact validator.
 */
public interface ArtifactValidator {

  void validate(CustomType sourceArtifact);
}
