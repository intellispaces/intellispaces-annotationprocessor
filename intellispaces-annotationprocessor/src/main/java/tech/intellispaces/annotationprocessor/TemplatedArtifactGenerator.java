package tech.intellispaces.annotationprocessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.intellispaces.commons.exception.UnexpectedExceptions;
import tech.intellispaces.commons.function.Functions;
import tech.intellispaces.commons.resource.ResourceFunctions;
import tech.intellispaces.jstatements.customtype.CustomType;
import tech.intellispaces.templateengine.TemplateEngine;
import tech.intellispaces.templateengine.template.Template;

/**
 * The template based artifact generator.
 */
public abstract class TemplatedArtifactGenerator implements ArtifactGenerator {
  private final CustomType sourceArtifact;

  private static final Map<String, Template> TEMPLATE_CACHE = new HashMap<>();
  private static final Logger LOG = LoggerFactory.getLogger(TemplatedArtifactGenerator.class);

  public TemplatedArtifactGenerator(CustomType sourceArtifact) {
    this.sourceArtifact = sourceArtifact;
  }

  /**
   * The source artifact.
   */
  public CustomType sourceArtifact() {
    return sourceArtifact;
  }

  /**
   * The generated artifact kind.
   */
  protected abstract ArtifactKind generatedArtifactKind();

  /**
   * The template name.
   */
  protected abstract String templateName();

  /**
   * Template variables.
   */
  protected abstract Map<String, Object> templateVariables();

  /**
   * Analyzes source artifact and returns <code>true</code> if generated artifact should be created
   * or <code>false</code> otherwise.
   */
  protected abstract boolean analyzeSourceArtifact(ArtifactGeneratorContext context);

  @Override
  public Optional<Artifact> generate(ArtifactGeneratorContext context) throws Exception {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Process class {} to generate class {}. Annotation processor generator {}",
          sourceArtifact.canonicalName(), generatedArtifactName(), this.getClass().getSimpleName()
      );
    }
    if (!analyzeSourceArtifact(context)) {
      return Optional.empty();
    }
    char[] source = synthesizeArtifact();
    return Optional.of(new ArtifactImpl(generatedArtifactKind(), generatedArtifactName(), source));
  }

  private char[] synthesizeArtifact() throws Exception {
    Template template = TEMPLATE_CACHE.computeIfAbsent(templateName(),
        Functions.wrapThrowingFunction(this::makeTemplate)
    );
    return template.resolve(templateVariables()).toCharArray();
  }

  private Template makeTemplate(String templateName) throws Exception {
    String templateSource = ResourceFunctions.readResourceAsString(
        TemplatedArtifactGenerator.class, templateName()
    ).orElseThrow(() -> UnexpectedExceptions.withMessage("Artifact template {0} is not found", templateName));
    return TemplateEngine.parseTemplate(templateSource);
  }
}
