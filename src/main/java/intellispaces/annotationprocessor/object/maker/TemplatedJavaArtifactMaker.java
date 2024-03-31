package intellispaces.annotationprocessor.object.maker;

import intellispaces.annotationprocessor.model.artifact.GeneratedArtifact;
import intellispaces.annotationprocessor.model.maker.ArtifactMaker;
import intellispaces.annotationprocessor.object.artifact.JavaArtifactImpl;
import intellispaces.commons.exception.UnexpectedViolationException;
import intellispaces.commons.function.ResourceFunctions;
import intellispaces.javastatements.model.custom.CustomType;
import intellispaces.templateengine.TemplateEngine;
import intellispaces.templateengine.model.TextTemplate;

import java.util.List;
import java.util.Map;

/**
 * One-off template based Java class artifact maker.
 */
public abstract class TemplatedJavaArtifactMaker implements ArtifactMaker {

  /**
   * Returns template name.
   */
  protected abstract String templateName();

  /**
   * Returns template parameters.
   */
  protected abstract Map<String, Object> templateParams();

  /**
   * Returns canonical class name of the generated artefact.
   */
  protected abstract String canonicalName();

  /**
   * Analyzes type and returns <code>true</code> if artifact should be created or <code>false</code> otherwise.
   */
  protected abstract boolean analyze(CustomType annotatedType);

  @Override
  public List<GeneratedArtifact> make(CustomType annotatedType) throws Exception {
    if (!analyze(annotatedType)) {
      return List.of();
    }
    String javaSource = synthesize();
    return List.of(new JavaArtifactImpl(canonicalName(), javaSource));
  }

  private String synthesize() throws Exception {
    String templateSource = ResourceFunctions.readResourceAsString(TemplatedJavaArtifactMaker.class, templateName()).orElseThrow(
        () -> new UnexpectedViolationException("Template for generate artifact is not found. Template name {}", templateName())
    );
    TextTemplate template = TemplateEngine.parseTemplate(templateSource);
    return TemplateEngine.resolveTemplate(template, templateParams());
  }
}
