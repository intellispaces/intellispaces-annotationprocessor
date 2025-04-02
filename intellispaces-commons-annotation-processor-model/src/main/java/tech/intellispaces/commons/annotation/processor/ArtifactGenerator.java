package tech.intellispaces.commons.annotation.processor;

import java.util.Optional;

/**
 * The one-time artifact generator.
 */
public interface ArtifactGenerator {

  /**
   * The full qualified name of the expected generated artifact.
   * <p>
   * The name of the generated artifact must already be known before it is generated.
   */
  String generatedArtifactName();

  boolean isRelevant(ArtifactGeneratorContext context);

  Optional<Artifact> generate(ArtifactGeneratorContext context) throws Exception;
}