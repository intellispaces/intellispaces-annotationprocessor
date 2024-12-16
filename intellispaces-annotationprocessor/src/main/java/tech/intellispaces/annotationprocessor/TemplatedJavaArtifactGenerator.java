package tech.intellispaces.annotationprocessor;

import tech.intellispaces.general.exception.UnexpectedExceptions;
import tech.intellispaces.general.type.ClassFunctions;
import tech.intellispaces.general.type.ClassNameFunctions;
import tech.intellispaces.general.type.PrimitiveFunctions;
import tech.intellispaces.java.reflection.customtype.CustomType;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public abstract class TemplatedJavaArtifactGenerator extends TemplatedArtifactGenerator {
  private final Set<String> staticImports = new HashSet<>();
  private final HashMap<String, String> imports = new HashMap<>();
  private final List<String> javaDocLines = new ArrayList<>();
  private final Map<String, Object> templateVariables = new HashMap<>();

  public TemplatedJavaArtifactGenerator(CustomType annotatedType) {
    super(annotatedType);
    addImport(Generated.class);
  }

  public void addImport(Class<?> aClass) {
    addImport(aClass.getName());
  }

  public void addImports(Collection<String> canonicalNames) {
    canonicalNames.forEach(this::addImport);
  }

  public void addImport(String canonicalName) {
    if (ClassFunctions.isLanguageClass(canonicalName)) {
      return;
    }
    String simpleName = ClassNameFunctions.getSimpleName(canonicalName);
    imports.putIfAbsent(simpleName, canonicalName);
  }

  public Consumer<String> getImportConsumer() {
    return this::addImport;
  }

  public void addStaticImport(String canonicalName) {
    staticImports.add(canonicalName);
  }

  public void addJavaDocLine(String line) {
    javaDocLines.add(line);
  }

  public String simpleNameOf(Class<?> aClass) {
    return simpleNameOf(aClass.getCanonicalName());
  }

  public String simpleNameOf(String canonicalName) {
    String simpleName = ClassNameFunctions.getSimpleName(canonicalName);
    if (ClassFunctions.isLanguageClass(canonicalName)) {
      return simpleName;
    }
    if (simpleName.equals(generatedArtifactSimpleName())) {
      return canonicalName;
    }

    String importedCanonicalName = imports.get(simpleName);
    if (importedCanonicalName == null) {
      throw UnexpectedExceptions.withMessage("Class {0} is missing from list of imported classes",
          canonicalName);
    }
    if (canonicalName.equals(importedCanonicalName)) {
      return simpleName;
    }
    return canonicalName;
  }

  public String addToImportAndGetSimpleName(String canonicalName) {
    addImport(canonicalName);
    return simpleNameOf(canonicalName);
  }

  public String addToImportAndGetSimpleName(Class<?> aClass) {
    addImport(aClass);
    return simpleNameOf(aClass);
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
    return imports.values().stream()
        .filter(className -> !ClassNameFunctions.getSimpleName(className).equals(generatedArtifactSimpleName()))
        .filter(className -> !ClassNameFunctions.getPackageName(className).equals(generatedArtifactPackageName()))
        .filter(className -> !ClassFunctions.isLanguageClass(className))
        .filter(className -> !PrimitiveFunctions.isPrimitiveTypename(className))
        .sorted()
        .toList();
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
        ClassFunctions.getJavaLibraryName(this.getClass()),
        this.getClass().getCanonicalName(),
        ZonedDateTime.now().format(ISO_OFFSET_DATE_TIME)
    );
  }
}
