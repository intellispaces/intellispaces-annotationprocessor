package tech.intellispaces.annotationprocessor;

import java.lang.annotation.Annotation;
import java.util.List;
import javax.annotation.processing.RoundEnvironment;

public interface ArtifactGeneratorContext {

  List<RoundEnvironment> roundEnvironments();

  RoundEnvironment initialRoundEnvironment();

  RoundEnvironment activeRoundEnvironment();

  boolean isProcessingFinished(String sourceArtifactName, Class<? extends Annotation> annotation);

  boolean isGenerated(String generatedArtifactName);

  List<ArtifactGenerator> generatorQueue();

  boolean isPenaltyRound();

  boolean isOverRound();
}
