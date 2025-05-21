package tech.intellispaces.annotationprocessor;

import tech.intellispaces.commons.type.ClassFunctions;
import tech.intellispaces.commons.type.ClassNameFunctions;
import tech.intellispaces.javareflection.customtype.CustomType;
import tech.intellispaces.javareflection.dependencies.DependencySets;
import tech.intellispaces.javareflection.dependencies.MutableDependencySet;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public abstract class TemplatedJavaArtifactGenerator extends TemplatedArtifactGenerator {
  private final Set<String> staticImports = new HashSet<>();
  private final MutableDependencySet imports = DependencySets.get(this::generatedArtifactName);
  private final List<String> javaDocLines = new ArrayList<>();
  private final Map<String, Object> templateVariables = new HashMap<>();

  public TemplatedJavaArtifactGenerator(CustomType annotatedType) {
    super(annotatedType);
    addImport(Generated.class);
  }

  @Override
  protected ArtifactKind generatedArtifactKind() {
    return ArtifactKinds.JavaFile;
  }

  public void addImport(Class<?> aClass) {
    imports.add(aClass);
  }

  public void addImport(String canonicalName) {
    imports.add(canonicalName);
  }

  public void addImports(Class<?>... classes) {
    imports.add(classes);
  }

  public Consumer<String> getImportConsumer() {
    return this::addImport;
  }

  public String addImportAndGetSimpleName(Class<?> aClass) {
    return imports.addAndGetSimpleName(aClass);
  }

  public String addImportAndGetSimpleName(String canonicalName) {
    return imports.addAndGetSimpleName(canonicalName);
  }

  public void addStaticImport(String canonicalName) {
    staticImports.add(canonicalName);
  }

  public void addJavaDocLine(String line) {
    javaDocLines.add(line);
  }

  public String simpleNameOf(Class<?> aClass) {
    return imports.simpleNameOf(aClass);
  }

  public String simpleNameOf(String canonicalName) {
    return imports.simpleNameOf(canonicalName);
  }

  public void addVariable(String name, Object value) {
    templateVariables.put(name, value);
  }

  @Override
  protected Map<String, Object> templateVariables() {
    templateVariables.put("sourceArtifactName", sourceArtifactName());
    templateVariables.put("sourceArtifactSimpleName", sourceArtifactSimpleName());
    templateVariables.put("sourceArtifactPackageName", sourceArtifactPackageName());

    templateVariables.put("generatedArtifactName", generatedArtifactName());
    templateVariables.put("generatedArtifactSimpleName", generatedArtifactSimpleName());
    templateVariables.put("generatedArtifactPackageName", generatedArtifactPackageName());

    templateVariables.put("importedClasses", getImports());

    templateVariables.put("generatedAnnotation", buildGeneratedAnnotation());
    return templateVariables;
  }

  public String sourceArtifactName() {
    return sourceArtifact().canonicalName();
  }

  public String sourceArtifactSimpleName() {
    if (sourceArtifact().isNested()) {
      return simpleNameOf(sourceArtifactName());
    }
    return sourceArtifact().simpleName();
  }

  public String sourceArtifactPackageName() {
    return sourceArtifact().packageName();
  }

  public String generatedArtifactSimpleName() {
    return ClassNameFunctions.getSimpleName(generatedArtifactName());
  }

  public String generatedArtifactPackageName() {
    return ClassNameFunctions.getPackageName(generatedArtifactName());
  }

  private List<String> getImports() {
    return imports.getImports();
  }

  private List<String> getStaticImports() {
    return staticImports.stream().sorted().toList();
  }

  private String buildGeneratedAnnotation() {
    return """
      @%s(
        source = "%s",
        library = "%s",
        generator = "%s",
        date = "%s"
      )""".formatted(
        simpleNameOf(Generated.class),
        sourceArtifact().canonicalName(),
        ClassFunctions.getJavaLibraryName(this.getClass()).orElse("<Unknown>"),
        this.getClass().getCanonicalName(),
        ZonedDateTime.now().format(ISO_OFFSET_DATE_TIME)
    );
  }
}
