package tech.intellispaces.annotationprocessor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;

import tech.intellispaces.reflection.customtype.CustomType;

class ArtifactProcessorContext {
  private final List<RoundEnvironment> roundEnvironments = new ArrayList<>();
  private final List<GenerationTask> tasks = new ArrayList<>();
  private final Set<String> generatedArtifacts = new HashSet<>();
  private final Map<String, Map<Class<? extends Annotation>, AnnotatedTypeProcessingContext>> artifactStatuses = (
      new HashMap<>()
  );
  private boolean penaltyRound;

  public List<RoundEnvironment> roundEnvironments() {
    return roundEnvironments;
  }

  public boolean isPenaltyRound() {
    return penaltyRound;
  }

  public void setPenaltyRound(boolean penaltyRound) {
    this.penaltyRound = penaltyRound;
  }

  public int numberTasks() {
    return tasks.size();
  }

  public Iterable<GenerationTask> allTasks() {
    return tasks;
  }

  public void addTasks(CustomType source, Class<? extends Annotation> annotation, List<GenerationTask> tasks) {
    Map<Class<? extends Annotation>, AnnotatedTypeProcessingContext> annotationToContextIndex = artifactStatuses.computeIfAbsent(
        source.canonicalName(), k -> new HashMap<>());
    var artifactProcessingContext = new AnnotatedTypeProcessingContext(source, annotation, tasks);
    annotationToContextIndex.put(annotation, artifactProcessingContext);
    this.tasks.addAll(tasks);
  }

  public void finishTask(GenerationTask task) {
    ArtifactGenerator generator = task.generator();

    generatedArtifacts.add(generator.generatedArtifactName());

    Map<Class<? extends Annotation>, AnnotatedTypeProcessingContext> annotationToContextIndex = artifactStatuses.get(
        task.source().canonicalName()
    );
    AnnotatedTypeProcessingContext annotatedTypeProcessingContext = annotationToContextIndex.get(task.annotation());
    annotatedTypeProcessingContext.incrementProcessedTasks();
  }

  public boolean isAlreadyGenerated(String generatedArtifactName) {
    return generatedArtifacts.contains(generatedArtifactName);
  }

  public boolean isProcessingFinished(String sourceArtifactName, Class<? extends Annotation> annotation) {
    Map<Class<? extends Annotation>, AnnotatedTypeProcessingContext> annotationToContextIndex = artifactStatuses.get(
        sourceArtifactName
    );
    if (annotationToContextIndex == null) {
      return false;
    }
    AnnotatedTypeProcessingContext annotatedTypeProcessingContext = annotationToContextIndex.get(annotation);
    if (annotatedTypeProcessingContext == null) {
      return false;
    }
    return (annotatedTypeProcessingContext.processedTasks() == annotatedTypeProcessingContext.tasks().size());
  }

  private static final class AnnotatedTypeProcessingContext {
    private final CustomType source;
    private final Class<? extends Annotation> annotation;
    private final List<GenerationTask> tasks;
    private int numberProcessedTasks;

    AnnotatedTypeProcessingContext(
        CustomType source,
        Class<? extends Annotation> annotation,
        List<GenerationTask> tasks
    ) {
      this.source = source;
      this.annotation = annotation;
      this.tasks = tasks;
    }

    CustomType source() {
      return source;
    }

    Class<? extends Annotation> annotation() {
      return annotation;
    }

    List<GenerationTask> tasks() {
      return tasks;
    }

    int processedTasks() {
      return numberProcessedTasks;
    }

    void incrementProcessedTasks() {
      this.numberProcessedTasks++;
    }
  }
}
