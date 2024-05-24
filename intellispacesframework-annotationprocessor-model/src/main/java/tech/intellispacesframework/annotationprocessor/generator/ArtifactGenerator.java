package tech.intellispacesframework.annotationprocessor.generator;

import tech.intellispacesframework.annotationprocessor.artifact.Artifact;

import java.util.Optional;

/**
 * Artifact code generator.
 */
public interface ArtifactGenerator {

  Optional<Artifact> generate() throws Exception;
}
