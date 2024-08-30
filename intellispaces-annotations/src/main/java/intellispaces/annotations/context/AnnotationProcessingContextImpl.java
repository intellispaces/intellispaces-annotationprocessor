package intellispaces.annotations.context;

import intellispaces.annotations.generator.GenerationTask;
import intellispaces.commons.exception.UnexpectedViolationException;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnnotationProcessingContextImpl implements AnnotationProcessingContext {
  private final Set<String> generatedArtifacts = new HashSet<>();
  private final Map<String, Map<Class<? extends Annotation>, SourceArtifactStatus>> sourceArtifactStatuses = new HashMap<>();

  public void finishTask(
      Class<? extends Annotation> annotation, int commonNumberTasks, GenerationTask task
  ) {
    generatedArtifacts.add(task.artifactName());

    Map<Class<? extends Annotation>, SourceArtifactStatus> map = sourceArtifactStatuses.computeIfAbsent(
        task.initiatorType().canonicalName(), k -> new HashMap<>()
    );
    SourceArtifactStatus status = map.computeIfAbsent(
        annotation, k -> new SourceArtifactStatus(commonNumberTasks));
    if (status.commonNumberTasks() != commonNumberTasks) {
      throw UnexpectedViolationException.withMessage("Inconsistent common number of tasks");
    }
    status.processedTasks().add(task);
  }

  @Override
  public boolean isGenerated(String generatedArtifactName) {
    return generatedArtifacts.contains(generatedArtifactName);
  }

  @Override
  public boolean isProcessingFinished(Class<? extends Annotation> annotation, String annotatedTypeName) {
    Map<Class<? extends Annotation>, SourceArtifactStatus> map = sourceArtifactStatuses.get(annotatedTypeName);
    if (map == null) {
      return false;
    }
    SourceArtifactStatus status = map.get(annotation);
    if (status == null) {
      return false;
    }
    return status.commonNumberTasks() == status.processedTasks().size();
  }

  private static final class SourceArtifactStatus {
    private final int commonNumberTasks;
    private final List<GenerationTask> processedTasks = new ArrayList<>();

    SourceArtifactStatus(int commonNumberTasks) {
      this.commonNumberTasks = commonNumberTasks;
    }

    int commonNumberTasks() {
      return commonNumberTasks;
    }

    List<GenerationTask> processedTasks() {
      return processedTasks;
    }
  }
}
