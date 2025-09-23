package lab;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public final class Analyzer {

  public static void main(final String[] args) throws IOException {
    final String jarFileName = args[0];
    final String baseName = Path.of(jarFileName).getFileName().toString();
    printAnalyzedFileName(baseName);

    final JarFile jar = new JarFile(jarFileName);
    final Analyzer analyzer = new Analyzer();
    analyzer.analyzeJar(jar);

    printStatistics(
        0, // TODO: number of classes
        0, // TODO: number of methods with code
        0, // TODO: total number of instructions
        0, // TODO: number of method invocations
        0 // TODO: number of branch instructions
    );

    printInstructionsHeader();
    for (int opcode = 0; opcode < 256; opcode++) {
      final String name = ""; // TODO: appropriate name for the current opcode
      final int count = 0; // TODO: appropriate count for the current opcode
      printInstruction(opcode, name, count);
    }
  }

  private void analyzeJar(final JarFile jar) throws IOException {
    final Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      final JarEntry entry = entries.nextElement();
      analyzeJarEntry(entry, jar);
    }
  }

  private void analyzeJarEntry(final JarEntry entry, final JarFile jar) throws IOException {
    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
      printAnalyzedJarEntry(entry.getName());

      final InputStream is = jar.getInputStream(entry);
      final ClassReader classReader = new ClassReader(is);
      final ClassNode classNode = new ClassNode();
      classReader.accept(classNode, ClassReader.SKIP_FRAMES);
      analyzeClass(classNode);
    }
  }

  private void analyzeClass(final ClassNode classNode) {
    printAnalyzedClass(classNode.name);

    // TODO: count the classes

    final List<MethodNode> methods = classNode.methods;
    for (final MethodNode methodNode : methods) {
      analyzeMethod(methodNode);
    }
  }

  private void analyzeMethod(final MethodNode methodNode) {
    printAnalyzedMethod(methodNode.name, methodNode.desc);

    // TODO: count the instructions
  }

  private static void printAnalyzedFileName(String fileName) {
    System.out.println("Analyzing " + fileName);
  }

  private static void printAnalyzedJarEntry(String entryName) {
    System.out.println("\tFile " + entryName);
  }

  private static void printAnalyzedClass(String className) {
    System.out.println("\t\tClass " + className);
  }

  private static void printAnalyzedMethod(String name, String descriptor) {
    System.out.println("\t\t\tMethod " + name + descriptor);
  }

  private static void printStatistics(int numClasses,
      int numMethods,
      int numInstructions,
      int numMethodInvocations,
      int numConditionalBranches) {
    System.out.println("==== STATISTICS ====");
    System.out.println("classes:\t" + numClasses);
    System.out.println("methods:\t" + numMethods);
    System.out.println("instructions:\t" + numInstructions);
    System.out.println("invoke instructions:\t" + numMethodInvocations);
    System.out.println("branch instructions:\t" + numConditionalBranches);
  }

  private static void printInstructionsHeader() {
    System.out.println("OPCODE\tMNEMONIC\tCOUNT");
  }

  private static void printInstruction(int opcode, String mnemonic, int count) {
    System.out.println(opcode + "\t" + mnemonic + "\t" + count);
  }
}
