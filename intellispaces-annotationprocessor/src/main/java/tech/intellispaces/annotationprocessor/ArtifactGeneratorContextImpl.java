package tech.intellispaces.annotationprocessor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;

import tech.intellispaces.commons.collection.CollectionFunctions;

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

  @Override
  public Optional<FileObject> getFile(JavaFileManager.Location location, String relativeName) {
    try {
      return Optional.of(processorContext.processingEnv().getFiler().getResource(location, "", relativeName));
    } catch (IOException e) {
      return Optional.empty();
    }
  }
}
