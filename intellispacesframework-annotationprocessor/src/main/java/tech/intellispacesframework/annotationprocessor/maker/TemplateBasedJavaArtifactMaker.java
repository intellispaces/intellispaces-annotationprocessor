package tech.intellispacesframework.annotationprocessor.maker;

import tech.intellispacesframework.annotationprocessor.artifact.Artifact;
import tech.intellispacesframework.annotationprocessor.artifact.JavaArtifactImpl;
import tech.intellispacesframework.commons.exception.UnexpectedViolationException;
import tech.intellispacesframework.commons.resource.ResourceFunctions;
import tech.intellispacesframework.javastatements.statement.custom.CustomType;
import tech.intellispacesframework.templateengine.TemplateEngine;
import tech.intellispacesframework.templateengine.template.Template;

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
