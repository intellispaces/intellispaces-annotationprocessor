package tech.intellispaces.annotationprocessor.validator;

import tech.intellispaces.java.reflection.customtype.CustomType;

public interface AnnotatedTypeValidator {

  void validate(CustomType annotatedType);
}
