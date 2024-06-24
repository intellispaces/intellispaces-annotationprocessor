package tech.intellispaces.framework.annotationprocessor;

import tech.intellispaces.framework.javastatements.statement.custom.CustomType;

public interface AnnotatedTypeValidator {

  void validate(CustomType annotatedType);
}
