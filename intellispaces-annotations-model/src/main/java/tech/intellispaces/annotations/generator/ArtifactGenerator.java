package tech.intellispaces.annotations.generator;

import tech.intellispaces.annotations.artifact.Artifact;

import javax.annotation.processing.RoundEnvironment;
import java.util.Optional;

/**
 * Artifact code generator.
 */
public interface ArtifactGenerator {

  String getArtifactName();

  Optional<Artifact> generate(RoundEnvironment roundEnv) throws Exception;
}
