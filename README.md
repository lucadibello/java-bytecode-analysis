[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/EBZFtET_)
# Software Performance Lab 2

## Mnemonics of method invocation instructions

- `invokedynamic` (186) - Invoke a dynamic method
- `invokeinterface` (185) - Invoke interface method
- `invokestatic` (184) - Invoke a class (static) method
- `invokespecial` (183) - Invoke instance method; special handling for superclass, private, and instance initialization method invocations
- `invokevirtual` (182) - Invoke instance method; dispatch based on class

## Mnemonics of conditional branch instructions

### Branch if int comparison with zero succeeds

- `ifeq` (153)
- `ifne` (154)
- `iflt` (155)
- `ifge` (156)
- `ifgt` (157)
- `ifle` (158)

### Branch if int comparison succeeds

- `if_icmpeq` (159)
- `if_icmpne` (160)
- `if_icmplt` (161)
- `if_icmpge` (162)
- `if_icmpgt` (163)
- `if_icmple` (164)

### Branch if reference comparison succeeds

- `if_acmpeq` (165)
- `if_acmpne` (166)

### Branch switch

- `tableswitch` (170)
- `lookupswitch` (171)

### Branch if reference is null or not null

- `ifnonnull` (199)
- `ifnull` (198)
