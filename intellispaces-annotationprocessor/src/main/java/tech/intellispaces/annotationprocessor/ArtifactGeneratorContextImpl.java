package tech.intellispaces.annotationprocessor;

import javax.annotation.processing.RoundEnvironment;
import java.lang.annotation.Annotation;

class ArtifactGeneratorContextImpl implements ArtifactGeneratorContext {
  private final RoundEnvironment roundEnvironment;
  private final ProcessingContext processingContext;

  public ArtifactGeneratorContextImpl(RoundEnvironment roundEnvironment, ProcessingContext processingContext) {
    this.roundEnvironment = roundEnvironment;
    this.processingContext = processingContext;
  }

  @Override
  public RoundEnvironment roundEnvironment() {
    return roundEnvironment;
  }

  @Override
  public boolean isProcessingFinished(String sourceArtifactName, Class<? extends Annotation> annotation) {
    return processingContext.isProcessingFinished(sourceArtifactName, annotation);
  }

  @Override
  public boolean isGenerated(String generatedArtifactName) {
    return processingContext.isAlreadyGenerated(generatedArtifactName);
  }
}
