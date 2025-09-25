# Java Bytecode Analysis Tool

A static analysis tool that examines Java bytecode within JAR files and generates comprehensive statistics about instruction usage, method invocations, and control flow patterns.

## Overview

This tool uses the ASM framework to parse JAR files and analyze their bytecode content. It provides detailed metrics on classes, methods, and individual JVM instructions, with particular focus on method invocation patterns and conditional branching behavior.

## Technical Stack

- **Java 21** with toolchain support
- **ASM Framework 9.7** for bytecode manipulation and analysis
- **Gradle** for build automation
- **JUnit 4.13.2** for testing

## Project Structure

```
├── src/main/java/lab/Analyzer.java    # Main analysis engine
├── src/test/resources/                # Test JAR files and expected outputs
├── build.gradle                       # Build configuration
└── run.sh                            # Convenience execution script
```

## Usage

### Build and Run

```bash
# Build the project
./gradlew build

# Analyze a JAR file
./gradlew run --args "/path/to/file.jar"

# Or use the convenience script
./run.sh
```

### Output

The tool generates statistics in the following format:

```
Analyzing example.jar
    File com/example/Class.class
        Class com/example/Class
            Method methodName()V

==== STATISTICS ====
classes:             45
methods:             312
instructions:        2150
invoke instructions: 485
branch instructions: 127

OPCODE  MNEMONIC        COUNT
0       nop             0
1       aconst_null     15
...
```

## Analysis Metrics

The analyzer tracks several categories of bytecode instructions:

### Method Invocations (opcodes 182-186)
- `invokevirtual`: Instance method dispatch
- `invokespecial`: Constructor, private, and super method calls
- `invokestatic`: Static method invocations
- `invokeinterface`: Interface method calls
- `invokedynamic`: Dynamic method invocation

### Conditional Branches
- Zero comparisons: `ifeq`, `ifne`, `iflt`, `ifge`, `ifgt`, `ifle` (153-158)
- Integer comparisons: `if_icmpeq`, `if_icmpne`, `if_icmplt`, `if_icmpge`, `if_icmpgt`, `if_icmple` (159-164)
- Reference comparisons: `if_acmpeq`, `if_acmpne` (165-166)
- Multi-way branches: `tableswitch`, `lookupswitch` (170-171)
- Null checks: `ifnull`, `ifnonnull` (198-199)

## Implementation Notes

- Uses parallel streams for efficient processing of large JAR files
- Thread-safe data structures (`AtomicInteger`, `ConcurrentHashMap`) enable concurrent analysis
- Filters out ASM pseudo-instructions (opcode -1) to count only actual JVM bytecode
- Processes only methods containing executable code

## Testing

```bash
./gradlew test
```

Tests validate analysis results against expected outputs using the included ASM library JAR file.

## References

- [ASM Framework](https://asm.ow2.io/)
- [JVM Specification](https://docs.oracle.com/javase/specs/jvms/se21/html/)