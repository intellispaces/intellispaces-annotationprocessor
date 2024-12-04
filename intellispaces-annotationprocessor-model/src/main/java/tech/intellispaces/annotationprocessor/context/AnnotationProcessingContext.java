package tech.intellispaces.annotationprocessor.context;

import java.lang.annotation.Annotation;

public interface AnnotationProcessingContext {

  boolean isProcessingFinished(Class<? extends Annotation> annotation, String annotatedTypeName);

  boolean isGenerated(String generatedArtifactName);
}
