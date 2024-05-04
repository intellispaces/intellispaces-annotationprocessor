package tech.intellispacesframework.annotationprocessor.maker;

import tech.intellispacesframework.annotationprocessor.artifact.Artifact;
import tech.intellispacesframework.javastatements.statement.custom.CustomType;

import java.util.List;

public interface ArtifactMaker {

  List<Artifact> make(CustomType annotatedType) throws Exception;
}
