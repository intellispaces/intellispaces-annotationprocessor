package tech.intellispaces.annotationprocessor;

import tech.intellispaces.java.reflection.customtype.CustomType;

import java.lang.annotation.Annotation;

/**
 * The task to run the artifact generator.
 *
 * @param generator the generator that needs to be run.
 * @param context the generator context.
 * @param source the source artifact related to this task.
 * @param annotation the annotation of the source artifact related to this task.
 */
record Task(
    ArtifactGenerator generator,
    ArtifactGeneratorContext context,
    CustomType source,
    Class<? extends Annotation> annotation
) {
}
