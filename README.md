# MiniLang Pipeline

A three-stage programming paradigms project for the EIF400 practical challenge.

## Pipeline

```text
programa.mini -> Java lexer/parser/OOP -> programa.ir
programa.ir   -> Python functional executor -> resultado.txt
resultado.txt -> MIPS checksum stage -> firma.txt
```

## Repository Structure

- `java/`: Java lexer, parser, instruction hierarchy, validation, and IR generation.
- `python/`: Python functional executor for `FILTER`, `MAP`, and `REDUCE`.
- `mips/`: MIPS checksum and verification stage.
- `examples/`: Valid and invalid MiniLang programs used for demonstrations.
- `tests/`: Cross-stage test cases and expected behavior.
- `docs/`: Pipeline contract, design decisions, and test evidence.
- `scripts/`: Commands that run the complete pipeline.
- `data/`: Runtime input and generated artifacts.

## Planned Execution

The complete pipeline will be documented here once the three stages are implemented.

## Team Workflow

1. Keep each stage independent and communicate only through the documented files.
2. Add or update tests with every parser or execution feature.
3. Do not manually edit generated files under `data/output/`.
4. Document any language extension in `docs/` before implementing it.
5. Use feature branches and review changes before merging into `main`.

## Requirements

The project will require a JDK, Python, and a MIPS simulator such as MARS or QtSPIM.
Exact versions and commands will be added after the implementation setup is agreed upon.
