package lab;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;

public class AnalyzerTest {

  @Test
  public void test() throws Exception {
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    final String testJarPath = Objects.requireNonNull(cl.getResource("asm-6.2.1.jar")).getPath();
    final String expectedPath = Objects.requireNonNull(cl.getResource("expected.txt")).getPath();
    final String expected = Files.readString(Path.of(expectedPath));

    try (ByteArrayOutputStream bas = new ByteArrayOutputStream()) {
      final PrintStream originalStdout = System.out;

      try (PrintStream newOut = new PrintStream(bas, true)) {
        System.setOut(newOut);
        Analyzer.main(new String[]{testJarPath});
      } finally {
        System.setOut(originalStdout);
      }

      final String actual = bas.toString();
      Assert.assertEquals(expected, actual);
    }
  }
}
