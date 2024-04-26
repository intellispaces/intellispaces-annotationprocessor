package intellispaces.annotationprocessor.artifact.sketch;

import intellispaces.commons.classes.ClassFunctions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaArtifactSketch {
  private final String canonicalName;
  private final Set<String> imports = new HashSet<>();
  private final Set<String> staticImports = new HashSet<>();
  private final List<String> javaDocLines = new ArrayList<>();

  public JavaArtifactSketch(String canonicalName) {
    this.canonicalName = canonicalName;
  }

  public void addImport(Class<?> aClass) {
    imports.add(aClass.getName());
  }

  public void addImport(String canonicalName) {
    imports.add(canonicalName);
  }

  public void addImports(Collection<String> canonicalNames) {
    imports.addAll(canonicalNames);
  }

  public void addStaticImport(String canonicalName) {
    staticImports.add(canonicalName);
  }

  public void addJavaDocLine(String line) {
    javaDocLines.add(line);
  }

  public List<String> getImports() {
    return imports.stream()
        .filter(className -> !className.startsWith("java.lang."))
        .filter(className -> !className.equals(canonicalName))
        .sorted()
        .toList();
  }

  public List<String> getStaticImports() {
    return staticImports.stream().sorted().toList();
  }

  public boolean isDuplicated(String canonicalName) {
    String simpleName = ClassFunctions.getSimpleName(canonicalName);
    return imports.stream()
        .anyMatch(importedName -> importedName.endsWith("." + simpleName) && !importedName.equals(canonicalName));
  }
}
