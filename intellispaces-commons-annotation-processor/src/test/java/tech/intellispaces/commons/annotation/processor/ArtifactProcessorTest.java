package tech.intellispaces.commons.annotation.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import tech.intellispaces.commons.resource.ResourceFunctions;

import javax.tools.JavaFileObject;
import java.nio.charset.StandardCharsets;

/**
 * Tests for {@link ArtifactProcessor}.
 */
public class ArtifactProcessorTest {

  @Test
  public void test() {
    // Given
    var annotationProcessor = new ArtifactProcessorSample();

    // When
    Compiler compiler = Compiler.javac().withProcessors(annotationProcessor);
    JavaFileObject sourceFile = JavaFileObjects.forResource("SourceSample.java");
    Compilation compilation = compiler.compile(sourceFile);

    // Then
    CompilationSubject.assertThat(compilation).succeeded();
    CompilationSubject.assertThat(compilation)
        .generatedSourceFile("intellispaces.framework.annotationprocessor.GeneratedSample")
        .contentsAsString(StandardCharsets.UTF_8)
        .isEqualTo(ResourceFunctions.readResourceAsStringForce(ArtifactProcessorTest.class, "/GeneratedSample.java"));
  }
}
