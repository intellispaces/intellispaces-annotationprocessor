package intellispaces.annotationprocessor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import intellispaces.commons.function.ResourceFunctions;
import org.junit.Test;

import javax.tools.JavaFileObject;

import java.nio.charset.StandardCharsets;

import static com.google.testing.compile.CompilationSubject.assertThat;

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
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("intellispaces.annotationprocessor.GeneratedSample")
        .contentsAsString(StandardCharsets.UTF_8)
        .isEqualTo(ResourceFunctions.readResourceAsStringSilently(AnnotationProcessorTest.class, "/GeneratedSample.java").orElseThrow());
  }
}
