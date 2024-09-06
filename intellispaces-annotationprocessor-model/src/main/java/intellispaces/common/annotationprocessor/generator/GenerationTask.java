package intellispaces.common.annotationprocessor.generator;

import intellispaces.common.annotationprocessor.artifact.Artifact;
import intellispaces.common.annotationprocessor.context.AnnotationProcessingContext;
import intellispaces.common.javastatement.customtype.CustomType;

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