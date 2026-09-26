# Pipeline Contract

## Stage 1: Java to IR

- Input: `data/input/programa.mini`
- Output: `data/output/programa.ir`
- The output is generated only when the source program is valid.
- Each syntax or lexical error must include its source line number.

## Stage 2: Python Execution

- Input: `data/output/programa.ir`
- Output: `data/output/resultado.txt`
- The executor must use functional operations for filtering and mapping.
- The result must include a short operation trace and the final result.

## Stage 3: MIPS Verification

- Input: values derived from `data/output/resultado.txt`
- Output: `data/output/firma.txt`
- The checksum must depend on the actual result and operation count.

Generated files must not be manually edited to simulate a successful execution.
