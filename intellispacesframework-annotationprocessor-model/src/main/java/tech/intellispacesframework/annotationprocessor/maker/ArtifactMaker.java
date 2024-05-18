package tech.intellispacesframework.annotationprocessor.maker;

import tech.intellispacesframework.annotationprocessor.artifact.Artifact;

import java.util.Optional;

public interface ArtifactMaker {

  Optional<Artifact> make() throws Exception;
}
