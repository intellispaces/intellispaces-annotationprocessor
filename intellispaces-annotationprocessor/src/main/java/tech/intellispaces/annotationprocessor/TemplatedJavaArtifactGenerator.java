package tech.intellispaces.annotationprocessor;

import tech.intellispaces.general.exception.UnexpectedExceptions;
import tech.intellispaces.general.type.ClassFunctions;
import tech.intellispaces.general.type.ClassNameFunctions;
import tech.intellispaces.java.reflection.customtype.CustomType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public abstract class TemplatedJavaArtifactGenerator extends TemplatedArtifactGenerator {
  private final Set<String> staticImports = new HashSet<>();
  private final HashMap<String, Set<String>> imports = new HashMap<>();
  private final List<String> javaDocLines = new ArrayList<>();
  private final Map<String, Object> templateVariables = new HashMap<>();

  public TemplatedJavaArtifactGenerator(CustomType annotatedType) {
    super(annotatedType);
  }

  public void addImport(Class<?> aClass) {
    addImport(aClass.getName());
  }

  public void addImports(Collection<String> canonicalNames) {
    canonicalNames.forEach(this::addImport);
  }

  public void addImport(String canonicalName) {
    String simpleName = ClassNameFunctions.getSimpleName(canonicalName);
    imports.computeIfAbsent(simpleName, k -> new LinkedHashSet<>()).add(canonicalName);
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
    if (ClassFunctions.isStandardClass(canonicalName)) {
      return simpleName;
    }
    Set<String> set = imports.get(simpleName);
    if (set == null) {
      throw UnexpectedExceptions.withMessage("Class {0} is missing from list of imported classes",
          canonicalName);
    }
    if (canonicalName.equals(set.iterator().next())) {
      return simpleName;
    }
    return canonicalName;
  }

  public String addToImportAndGetSimpleName(String canonicalName) {
    if (!ClassFunctions.isStandardClass(canonicalName)) {
      addImport(canonicalName);
    }
    return simpleNameOf(canonicalName);
  }

  public String addToImportAndGetSimpleName(Class<?> aClass) {
    if (!ClassFunctions.isStandardClass(aClass)) {
      addImport(aClass);
    }
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
        .map(s -> s.iterator().next())
        .filter(className -> !ClassFunctions.isStandardClass(className))
        .filter(className -> !className.startsWith(generatedArtifactPackageName() + "."))
        .sorted()
        .toList();
  }

  private List<String> getStaticImports() {
    return staticImports.stream().sorted().toList();
  }
}
