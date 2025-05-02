package tech.intellispaces.annotationprocessor;

import tech.intellispaces.statementsj.customtype.CustomType;

/**
 * The source artifact validator.
 */
public interface ArtifactValidator {

  void validate(CustomType sourceArtifact);
}
