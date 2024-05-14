package tech.intellispacesframework.annotationprocessor.artifact.sketch;

import tech.intellispacesframework.commons.type.TypeFunctions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaArtifactSketch {
  private String canonicalName;
  private final Set<String> imports = new HashSet<>();
  private final Set<String> staticImports = new HashSet<>();
  private final List<String> javaDocLines = new ArrayList<>();


  public void canonicalName(String canonicalName) {
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

  public String canonicalName() {
    return canonicalName;
  }

  public String simpleName() {
    return TypeFunctions.getSimpleName(canonicalName);
  }

  public String packageName() {
    return TypeFunctions.getPackageName(canonicalName);
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
    String simpleName = TypeFunctions.getSimpleName(canonicalName);
    return imports.stream()
        .anyMatch(importedName -> importedName.endsWith("." + simpleName) && !importedName.equals(canonicalName));
  }
}
