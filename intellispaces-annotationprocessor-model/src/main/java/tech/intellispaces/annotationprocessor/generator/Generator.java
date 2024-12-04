package tech.intellispaces.annotationprocessor.generator;

import tech.intellispaces.annotationprocessor.artifact.Artifact;
import tech.intellispaces.annotationprocessor.context.AnnotationProcessingContext;
import tech.intellispaces.java.reflection.customtype.CustomType;

import javax.annotation.processing.RoundEnvironment;
import java.util.Optional;

/**
 * The artifact generator.
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