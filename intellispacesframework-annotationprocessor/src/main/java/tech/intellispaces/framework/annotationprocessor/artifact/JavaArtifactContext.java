package tech.intellispaces.framework.annotationprocessor.artifact;

import tech.intellispaces.framework.commons.exception.UnexpectedViolationException;
import tech.intellispaces.framework.commons.type.TypeFunctions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class JavaArtifactContext {
  private String generatedClassCanonicalName;
  private final HashMap<String, Set<String>> imports = new HashMap<>();
  private final Set<String> staticImports = new HashSet<>();
  private final List<String> javaDocLines = new ArrayList<>();

  public void generatedClassCanonicalName(String canonicalName) {
    this.generatedClassCanonicalName = canonicalName;
  }

  public void addImport(Class<?> aClass) {
    addImport(aClass.getName());
  }

  public void addImports(Collection<String> canonicalNames) {
    canonicalNames.forEach(this::addImport);
  }

  public void addImport(String canonicalName) {
    String simpleName = TypeFunctions.getSimpleName(canonicalName);
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

  public String generatedClassCanonicalName() {
    return generatedClassCanonicalName;
  }

  public String generatedClassSimpleName() {
    return TypeFunctions.getSimpleName(generatedClassCanonicalName);
  }

  public String packageName() {
    return TypeFunctions.getPackageName(generatedClassCanonicalName);
  }

  public String simpleNameOf(Class<?> aClass) {
    return simpleNameOf(aClass.getCanonicalName());
  }

  public String simpleNameOf(String canonicalName) {
    String simpleName = TypeFunctions.getSimpleName(canonicalName);
    if (canonicalName.startsWith("java.lang.")) {
      return simpleName;
    }
    Set<String> set = imports.get(simpleName);
    if (set == null) {
      throw UnexpectedViolationException.withMessage("Class {} is missing from list of imported classes",
          canonicalName);
    }
    if (canonicalName.equals(set.iterator().next())) {
      return simpleName;
    }
    return canonicalName;
  }

  public String addToImportAndGetSimpleName(String canonicalName) {
    addImport(canonicalName);
    return simpleNameOf(canonicalName);
  }

  public List<String> getImports() {
    return imports.values().stream()
        .map(s -> s.iterator().next())
        .filter(className -> !className.startsWith("java.lang."))
        .filter(className -> !className.equals(generatedClassCanonicalName))
        .sorted()
        .toList();
  }

  public List<String> getStaticImports() {
    return staticImports.stream().sorted().toList();
  }
}
