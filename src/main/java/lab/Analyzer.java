package lab;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.StreamSupport;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;

public final class Analyzer {

  private static int classCount = 0;
  private static int methodCount = 0;
  private static int instructionCount = 0;
  // NOTE: since we are using a parallel stream to compute the following metrics,
  // we need to use java thread-safe utils.
  private static AtomicInteger methodInvocationCount = new AtomicInteger(0);
  private static AtomicInteger branchInstructionCount = new AtomicInteger(0);
  private static Map<Integer, Integer> opcodeCount = new ConcurrentHashMap<>();

  // range of conditional branches opcodes
  private final int[][] CBRANCH_RANGES = new int[][] {
    { Opcodes.IFEQ, Opcodes.IFLE }, // if<cond> (153 - 158)
    { Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPLE }, // if_icmp<cond> (159 - 164)
    { Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE }, // if_acmpeq + if_acmpne
    { Opcodes.TABLESWITCH, Opcodes.LOOKUPSWITCH }, // tableswitch + lookupswitch
    { Opcodes.IFNULL, Opcodes.IFNONNULL }, // ifnonnull + ifnull
  };

  // range of invoke opcodes
  private final int[] INVOKE_RANGE = new int[] { 182, 186 };

  public static void main(final String[] args) throws IOException {
    final String jarFileName = args[0];
    final String baseName = Path.of(jarFileName).getFileName().toString();
    printAnalyzedFileName(baseName);

    final JarFile jar = new JarFile(jarFileName);
    final Analyzer analyzer = new Analyzer();
    analyzer.analyzeJar(jar);

    printStatistics(
      Analyzer.classCount, // NOTE: number of classes
      Analyzer.methodCount, // NOTE: number of methods with code
      Analyzer.instructionCount, // NOTE: total number of instructions (actual instructions, not JVM instructions)
      Analyzer.methodInvocationCount.get(), // NOTE: number of method invocations
      Analyzer.branchInstructionCount.get() // NOTE: number of branch instructions
    );

    printInstructionsHeader();
    for (int opcode = 0; opcode < 256; opcode++) {
      // read textual name of current opcode
      final String name = opcode < Printer.OPCODES.length ? Printer.OPCODES[opcode] : "";
      // count for current opcode
      final int count = Analyzer.opcodeCount.getOrDefault(opcode, 0);
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

    // NOTE: count the classes + interfaces
    Analyzer.classCount++;

    final List<MethodNode> methods = classNode.methods;
    for (final MethodNode methodNode : methods) {
      analyzeMethod(methodNode);
    }
  }

  private void analyzeMethod(final MethodNode methodNode) {
    printAnalyzedMethod(methodNode.name, methodNode.desc);

    // NOTE: count the instructions

    // we need to skip ASM instructions!
    // In this stream we count:
    // - number of conditional branches
    // - number of method calls
    // - total number of instructions per opcode
    //
    // And we get as output a list of instructions!
    long totInstructions = StreamSupport.stream(methodNode.instructions.spliterator(), true)
      .filter(i -> i.getOpcode() != -1) // keep in consideration only actual instructions
      .peek(i -> {
        int opcode = i.getOpcode();

        // update counter for the current opcode
        Analyzer.opcodeCount.merge(opcode, 1, Integer::sum);

        // count number of method invoke
        if (
          opcode >= INVOKE_RANGE[0] && opcode <= INVOKE_RANGE[1]
        ) Analyzer.methodInvocationCount.incrementAndGet();
        else {
          // count number of conditional branches
          for (int[] range : CBRANCH_RANGES) {
            if (opcode >= range[0] && opcode <= range[1]) {
              Analyzer.branchInstructionCount.incrementAndGet();
              break;
            }
          }
        }
      })
      .count();

    // record total number of valid instructions
    Analyzer.instructionCount += (int) totInstructions;
    if (totInstructions > 0) Analyzer.methodCount++;
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

  private static void printStatistics(
    int numClasses,
    int numMethods,
    int numInstructions,
    int numMethodInvocations,
    int numConditionalBranches
  ) {
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
