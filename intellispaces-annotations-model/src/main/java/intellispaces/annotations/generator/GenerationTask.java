package intellispaces.annotations.generator;

import intellispaces.annotations.artifact.Artifact;
import intellispaces.annotations.context.AnnotationProcessingContext;
import intellispaces.javastatements.customtype.CustomType;

import javax.annotation.processing.RoundEnvironment;
import java.util.Optional;

/**
 * Artifact generation task.
 */
public interface GenerationTask {

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

  Optional<Artifact> execute(RoundEnvironment roundEnv) throws Exception;
}