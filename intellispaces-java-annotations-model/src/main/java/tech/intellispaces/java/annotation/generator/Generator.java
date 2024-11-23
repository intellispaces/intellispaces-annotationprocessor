package tech.intellispaces.java.annotation.generator;

import tech.intellispaces.java.annotation.artifact.Artifact;
import tech.intellispaces.java.annotation.context.AnnotationProcessingContext;
import tech.intellispaces.java.reflection.customtype.CustomType;

import javax.annotation.processing.RoundEnvironment;
import java.util.Optional;

/**
 * Artifact generator.
 */
public interface Generator {

  /**
   * Initiator type.
   */
  CustomType initiatorType();

  /**
   * Annotated type.
   */
  CustomType annotatedType();

  /**
   * Name of the generated artifact.
   */
  String artifactName();

  boolean isRelevant(AnnotationProcessingContext context);

  Optional<Artifact> run(RoundEnvironment roundEnv) throws Exception;
}