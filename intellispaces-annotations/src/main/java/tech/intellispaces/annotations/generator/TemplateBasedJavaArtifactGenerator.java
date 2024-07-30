package tech.intellispaces.annotations.generator;

import tech.intellispaces.annotations.artifact.Artifact;
import tech.intellispaces.annotations.artifact.JavaArtifactImpl;
import tech.intellispaces.commons.exception.UnexpectedViolationException;
import tech.intellispaces.commons.function.Functions;
import tech.intellispaces.commons.resource.ResourceFunctions;
import tech.intellispaces.javastatements.customtype.CustomType;
import tech.intellispaces.templates.TemplateEngine;
import tech.intellispaces.templates.template.Template;

import javax.annotation.processing.RoundEnvironment;
import java.util.HashMap;
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
  protected abstract boolean analyzeAnnotatedType(RoundEnvironment roundEnv);

  @Override
  public Optional<Artifact> generate(RoundEnvironment roundEnv) throws Exception {
    if (!analyzeAnnotatedType(roundEnv)) {
      return Optional.empty();
    }
    String source = synthesizeArtifact();
    return Optional.of(new JavaArtifactImpl(canonicalName(), source));
  }

  private String synthesizeArtifact() throws Exception {
    Template template = TEMPLATE_CACHE.computeIfAbsent(templateName(),
        Functions.coveredThrowableFunction(this::makeTemplate)
    );
    return template.resolve(templateVariables());
  }

  private Template makeTemplate(String templateName) throws Exception {
    String templateSource = ResourceFunctions.readResourceAsString(
        TemplateBasedJavaArtifactGenerator.class, templateName()
    ).orElseThrow(
        () -> UnexpectedViolationException.withMessage("Template for generate artifact is not found. Template name {}",
            templateName())
    );
    return TemplateEngine.parseTemplate(templateSource);
  }

  private static final Map<String, Template> TEMPLATE_CACHE = new HashMap<>();
}
