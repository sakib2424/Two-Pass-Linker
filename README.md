# Two-Pass-Linker
The target machine is word addressable and has a memory of 300 words, each consisting of 4 decimal digits. The first
(leftmost) digit is the opcode, which is unchanged by the linker. The remaining three digits (called the address field) form
either
* An immediate operand, which is unchanged.
* An absolute address, which is unchanged.
* A relative address, which is relocated.
* An external address, which is resolved.

Relocating relative addresses and resolving external references were discussed in class and are in the notes. The input
consists of a series of object modules, each of which contains three parts: definition list, use list, and program text.

The linker processes the input twice (that is why it is called two-pass). Pass one determines the base address for each
module and the absolute address for each external symbol, storing the later in the symbol table it produces. The first
module has base address zero; the base address for module I + 1 is equal to the base address of module I plus the length
of module I. The absolute address for a symbol S defined in module M is the base address of M plus the relative address
of S within M. Pass two uses the base addresses and the symbol table computed in pass one to generate the actual output
by relocating relative addresses and resolving external references.

The definition list is a count ND followed by ND pairs (S, R) where S is the symbol being defined and R is the relative
address to which the symbol refers. Pass one relocates R forming the absolute address A and stores the pair (S, A) in the
symbol table.

The use list is a count NU followed by the NU “pairs”. The first entry in the pair is an external symbol used in the
module. The second entry is a list of relative addresses in the module in which the symbol is used. The list is terminated
by a sentinel of -1. For example, a use list of “2 f 3 1 4 -1 xyg 0 -1” signifies that the symbol f is used in instructions 1, 3, and 4, and the symbol xyg is used in instruction 0.
The program text consists of a count NT followed by NT 5-digit numbers. NT is the length of the module. The left four
digits of each number form the instruction as described above. The last (rightmost) digit specifies the address type: 1
signifies “immediate”, 2 “absolute”, 3 “relative”, and 4 “external”.

## Other requirements: Error detection, arbitrary limits, et al.
Your program must check the input for the errors listed below. All error messages produced must be informative, e.g.,
“Error: The symbol ‘diagonal’ was used but not defined. It has been given the value 111”.
* If a symbol is multiply defined, print an error message and use the value given in the last definition.
* If a symbol is used but not defined, print an error message and use the value 111.
* If a symbol is defined but not used, print a warning message and continue.
* If an absolute address exceeds the size of the machine, print an error message and use the largest legal value.
* If multiple symbols are listed as used in the same instruction, print an error message and ignore all but the last usage
given.
* If an address appearing in a definition exceeds the size of the module, print an error message and treat the address given as the last word in the module.
