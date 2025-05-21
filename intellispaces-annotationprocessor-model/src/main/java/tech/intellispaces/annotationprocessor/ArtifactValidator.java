package tech.intellispaces.annotationprocessor;

import tech.intellispaces.javareflection.customtype.CustomType;

/**
 * The source artifact validator.
 */
public interface ArtifactValidator {

  void validate(CustomType sourceArtifact);
}
