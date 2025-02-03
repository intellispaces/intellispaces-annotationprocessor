package tech.intellispaces.commons.annotation.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Generated {

  /**
   * The source artifact.
   */
  String source();

  /**
   * The generation library name.
   */
  String library();

  /**
   * The generator name.
   */
  String generator();

  /**
   * The generation date.
   */
  String date();
}
