package tech.intellispaces.java.annotation.validator;

import tech.intellispaces.java.reflection.customtype.CustomType;

public interface AnnotatedTypeValidator {

  void validate(CustomType annotatedType);
}
