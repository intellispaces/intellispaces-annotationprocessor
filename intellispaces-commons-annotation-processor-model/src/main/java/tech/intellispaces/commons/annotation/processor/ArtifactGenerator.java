package tech.intellispaces.commons.annotation.processor;

import tech.intellispaces.commons.java.reflection.customtype.CustomType;

import java.util.Optional;

/**
 * The one-time artifact generator.
 */
public interface ArtifactGenerator {

  /**
   * The source artifact.
   */
  CustomType sourceArtifact();

  /**
   * The full qualified name of the expected generated artifact.
   * <p>
   * The name of the generated artifact must already be known before it is generated.
   */
  String generatedArtifactName();

  boolean isRelevant(ArtifactGeneratorContext context);

  Optional<Artifact> generate(ArtifactGeneratorContext context) throws Exception;
}