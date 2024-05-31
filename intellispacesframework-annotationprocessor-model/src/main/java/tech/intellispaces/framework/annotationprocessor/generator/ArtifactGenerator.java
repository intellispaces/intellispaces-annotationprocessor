package tech.intellispaces.framework.annotationprocessor.generator;

import tech.intellispaces.framework.annotationprocessor.artifact.Artifact;

import java.util.Optional;

/**
 * Artifact code generator.
 */
public interface ArtifactGenerator {

  Optional<Artifact> generate() throws Exception;
}
