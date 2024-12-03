package tech.intellispaces.java.annotation.generator;

import tech.intellispaces.java.annotation.artifact.Artifact;
import tech.intellispaces.java.annotation.artifact.SourceArtifactImpl;
import tech.intellispaces.general.exception.UnexpectedExceptions;
import tech.intellispaces.general.function.Functions;
import tech.intellispaces.general.resource.ResourceFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.intellispaces.java.reflection.customtype.CustomType;
import tech.intellispaces.templateengine.TemplateEngine;
import tech.intellispaces.templateengine.template.Template;

import javax.annotation.processing.RoundEnvironment;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Template based source artifact generator.
 */
public abstract class TemplatedGenerator implements Generator {
  protected final CustomType initiatorType;
  protected final CustomType annotatedType;

  private static final Map<String, Template> TEMPLATE_CACHE = new HashMap<>();
  private static final Logger LOG = LoggerFactory.getLogger(TemplatedGenerator.class);

  public TemplatedGenerator(CustomType initiatorType, CustomType annotatedType) {
    this.initiatorType = initiatorType;
    this.annotatedType = annotatedType;
  }

  /**
   * Template name.
   */
  protected abstract String templateName();

  /**
   * Template variables.
   */
  protected abstract Map<String, Object> templateVariables();

  /**
   * Analyzes type and returns <code>true</code> if artifact should be created or <code>false</code> otherwise.
   */
  protected abstract boolean analyzeAnnotatedType(RoundEnvironment roundEnv);

  @Override
  public CustomType initiatorType() {
    return initiatorType;
  }

  @Override
  public CustomType annotatedType() {
    return annotatedType;
  }

  @Override
  public Optional<Artifact> run(RoundEnvironment roundEnv) throws Exception {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Process class {} to generate class {}. Annotation processor generator {}",
          annotatedType.canonicalName(), artifactName(), this.getClass().getSimpleName()
      );
    }
    if (!analyzeAnnotatedType(roundEnv)) {
      return Optional.empty();
    }
    String source = synthesizeArtifact();
    return Optional.of(new SourceArtifactImpl(artifactName(), source));
  }

  private String synthesizeArtifact() throws Exception {
    Template template = TEMPLATE_CACHE.computeIfAbsent(templateName(),
        Functions.wrapThrowingFunction(this::makeTemplate)
    );
    return template.resolve(templateVariables());
  }

  private Template makeTemplate(String templateName) throws Exception {
    String templateSource = ResourceFunctions.readResourceAsString(
        TemplatedGenerator.class, templateName()
    ).orElseThrow(() -> UnexpectedExceptions.withMessage(
        "Template for generate artifact is not found. Template name {0}", templateName())
    );
    return TemplateEngine.parseTemplate(templateSource);
  }
}
