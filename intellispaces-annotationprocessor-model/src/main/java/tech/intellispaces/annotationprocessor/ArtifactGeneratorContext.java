package tech.intellispaces.annotationprocessor;

import javax.annotation.processing.RoundEnvironment;
import java.lang.annotation.Annotation;

public interface ArtifactGeneratorContext {

  RoundEnvironment roundEnvironment();

  boolean isProcessingFinished(String sourceArtifactName, Class<? extends Annotation> annotation);

  boolean isGenerated(String generatedArtifactName);
}
