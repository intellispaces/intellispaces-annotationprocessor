package tech.intellispaces.annotationprocessor;

import java.lang.annotation.Annotation;

import tech.intellispaces.javareflection.customtype.CustomType;

/**
 * The task to generate new artifact.
 *
 * @param source the source artifact related to this task.
 * @param annotation the annotation of the source artifact related to this task.
 * @param generator the generator that needs to be run.
 * @param context the generator context.
 */
record GenerationTask(
    CustomType source,
    Class<? extends Annotation> annotation,
    ArtifactGenerator generator,
    ArtifactGeneratorContext context
) {
}
