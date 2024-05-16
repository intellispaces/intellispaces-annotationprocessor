package tech.intellispacesframework.annotationprocessor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import tech.intellispacesframework.commons.resource.ResourceFunctions;

import javax.tools.JavaFileObject;
import java.nio.charset.StandardCharsets;

/**
 * Tests for {@link AnnotatedTypeProcessor}.
 */
public class AnnotationProcessorTest {

  @Test
  public void test() {
    // Given
    var annotationProcessor = new AnnotatedTypeProcessorSample();

    // When
    Compiler compiler = Compiler.javac().withProcessors(annotationProcessor);
    JavaFileObject sourceFile = JavaFileObjects.forResource("SourceSample.java");
    Compilation compilation = compiler.compile(sourceFile);

    // Then
    CompilationSubject.assertThat(compilation).succeeded();
    CompilationSubject.assertThat(compilation)
        .generatedSourceFile("intellispaces.annotationprocessor.GeneratedSample")
        .contentsAsString(StandardCharsets.UTF_8)
        .isEqualTo(ResourceFunctions.readResourceAsStringForce(AnnotationProcessorTest.class, "/GeneratedSample.java"));
  }
}
