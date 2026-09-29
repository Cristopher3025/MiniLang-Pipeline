from pathlib import Path


def read_ir(file_path: Path) -> list[list[str]]:
    """
    Lee programa.ir y separa cada instruccion
    utilizando | como contrato entre Java y Python.
    """

    if not file_path.exists():
        raise FileNotFoundError(
            f"No existe el archivo IR: {file_path}"
        )

    lines = file_path.read_text(
        encoding="utf-8"
    ).splitlines()

    instructions = list(
        map(
            lambda line: line.strip().split("|"),
            filter(
                lambda line: line.strip() != "",
                lines
            )
        )
    )

    if not instructions:
        raise ValueError(
            "El archivo programa.ir esta vacio."
        )

    return instructions