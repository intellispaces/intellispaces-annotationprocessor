package tech.intellispaces.annotationprocessor;

import javax.annotation.processing.RoundEnvironment;
import java.lang.annotation.Annotation;
import java.util.List;

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
