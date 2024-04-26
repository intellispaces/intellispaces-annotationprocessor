package intellispaces.annotationprocessor.maker;

import intellispaces.annotationprocessor.artifact.Artifact;
import intellispaces.annotationprocessor.artifact.JavaArtifactImpl;
import intellispaces.commons.exception.UnexpectedViolationException;
import intellispaces.commons.resource.ResourceFunctions;
import intellispaces.javastatements.statement.custom.CustomType;
import intellispaces.templateengine.TemplateEngine;
import intellispaces.templateengine.template.Template;

import java.util.List;
import java.util.Map;

/**
 * One-off template based Java class artifact maker.
 */
public abstract class TemplateBasedJavaArtifactMaker implements ArtifactMaker {

  /**
   * Returns template name.
   */
  protected abstract String templateName();

  /**
   * Returns template variables.
   */
  protected abstract Map<String, Object> templateVariables();

  /**
   * Returns canonical class name of the generated artefact.
   */
  protected abstract String canonicalName();

  /**
   * Analyzes type and returns <code>true</code> if artifact should be created or <code>false</code> otherwise.
   */
  protected abstract boolean analyze(CustomType annotatedType);

  @Override
  public List<Artifact> make(CustomType annotatedType) throws Exception {
    if (!analyze(annotatedType)) {
      return List.of();
    }
    String source = synthesize();
    return List.of(new JavaArtifactImpl(canonicalName(), source));
  }

  private String synthesize() throws Exception {
    String templateSource = ResourceFunctions.readResourceAsString(TemplateBasedJavaArtifactMaker.class, templateName()).orElseThrow(
        () -> UnexpectedViolationException.withMessage("Template for generate artifact is not found. Template name {}", templateName())
    );
    Template template = TemplateEngine.parseTemplate(templateSource);
    return TemplateEngine.resolveTemplate(template, templateVariables());
  }
}
