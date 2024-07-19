package tech.intellispaces.framework.annotationprocessor.validator;

import tech.intellispaces.framework.javastatements.statement.custom.CustomType;

public interface AnnotatedTypeValidator {

  void validate(CustomType annotatedType);
}
