package tech.intellispaces.commons.annotation.processor;

import javax.annotation.processing.RoundEnvironment;
import java.lang.annotation.Annotation;

class ArtifactGeneratorContextImpl implements ArtifactGeneratorContext {
  private final ProcessorContext processorContext;

  public ArtifactGeneratorContextImpl(ProcessorContext processorContext) {
    this.processorContext = processorContext;
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
}
