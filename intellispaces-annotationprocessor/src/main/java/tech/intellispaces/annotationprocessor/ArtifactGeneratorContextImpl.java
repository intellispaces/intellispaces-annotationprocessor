package tech.intellispaces.annotationprocessor;

import tech.intellispaces.commons.collection.CollectionFunctions;

import javax.annotation.processing.RoundEnvironment;
import java.lang.annotation.Annotation;
import java.util.List;

class ArtifactGeneratorContextImpl implements ArtifactGeneratorContext {
  private final ArtifactProcessorContext processorContext;

  public ArtifactGeneratorContextImpl(ArtifactProcessorContext processorContext) {
    this.processorContext = processorContext;
  }

  @Override
  public List<RoundEnvironment> roundEnvironments() {
    return processorContext.roundEnvironments();
  }

  @Override
  public RoundEnvironment initialRoundEnvironment() {
    return processorContext.roundEnvironments().get(0);
  }

  @Override
  public RoundEnvironment activeRoundEnvironment() {
    return processorContext.roundEnvironments().get(processorContext.roundEnvironments().size() - 1);
  }

  @Override
  public boolean isProcessingFinished(String sourceArtifactName, Class<? extends Annotation> annotation) {
    return processorContext.isProcessingFinished(sourceArtifactName, annotation);
  }

  @Override
  public boolean isGenerated(String generatedArtifactName) {
    return processorContext.isAlreadyGenerated(generatedArtifactName);
  }

  @Override
  public List<ArtifactGenerator> generatorQueue() {
    return CollectionFunctions.toList(processorContext.allTasks().iterator()).stream()
        .map(GenerationTask::generator)
        .toList();
  }

  @Override
  public boolean isPenaltyRound() {
    return processorContext.isPenaltyRound();
  }

  @Override
  public boolean isOverRound() {
    return activeRoundEnvironment().processingOver();
  }
}
