package tech.intellispaces.framework.annotationprocessor.generator;

import tech.intellispaces.framework.annotationprocessor.artifact.Artifact;
import tech.intellispaces.framework.annotationprocessor.artifact.JavaArtifactImpl;
import tech.intellispaces.framework.commons.exception.UnexpectedViolationException;
import tech.intellispaces.framework.commons.resource.ResourceFunctions;
import tech.intellispaces.framework.javastatements.statement.custom.CustomType;
import tech.intellispaces.framework.templateengine.TemplateEngine;
import tech.intellispaces.framework.templateengine.template.Template;

import java.util.Map;
import java.util.Optional;

/**
 * One-off template based Java class artifact maker.
 */
public abstract class TemplateBasedJavaArtifactGenerator implements ArtifactGenerator {
  protected final CustomType annotatedType;

  public TemplateBasedJavaArtifactGenerator(CustomType annotatedType) {
    this.annotatedType = annotatedType;
  }

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
  protected abstract boolean analyzeAnnotatedType();

  @Override
  public Optional<Artifact> generate() throws Exception {
    if (!analyzeAnnotatedType()) {
      return Optional.empty();
    }
    String source = synthesizeArtifact();
    return Optional.of(new JavaArtifactImpl(canonicalName(), source));
  }

  private String synthesizeArtifact() throws Exception {
    String templateSource = ResourceFunctions.readResourceAsString(
        TemplateBasedJavaArtifactGenerator.class, templateName()
    ).orElseThrow(
        () -> UnexpectedViolationException.withMessage("Template for generate artifact is not found. Template name {}",
            templateName())
    );
    Template template = TemplateEngine.parseTemplate(templateSource);
    return TemplateEngine.resolveTemplate(template, templateVariables());
  }
}
