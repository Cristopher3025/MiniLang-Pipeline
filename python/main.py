from pathlib import Path
import sys

# Agregamos /src para poder importar nuestro paquete minilang.
CURRENT_DIR = Path(__file__).resolve().parent
SRC_DIR = CURRENT_DIR / "src"

sys.path.insert(0, str(SRC_DIR))

from minilang.ir_reader import read_ir
from minilang.executor import execute


def main():

    print("=== MiniLang - Etapa Python ===")
    print()

    # python/ está dentro de la raíz MiniLang-Pipeline.
    project_root = CURRENT_DIR.parent

    input_path = (
        project_root
        / "data"
        / "output"
        / "programa.ir"
    )

    output_path = (
        project_root
        / "data"
        / "output"
        / "resultado.txt"
    )

    try:

        # 1. Leer programa.ir
        print("[1/3] Leyendo programa.ir...")

        instructions = read_ir(input_path)

        # 2. Ejecutar las transformaciones
        print("[2/3] Ejecutando transformaciones...")

        trace, result, operations = execute(
            instructions
        )

        # 3. Generar resultado.txt
        print("[3/3] Generando resultado.txt...")

        output_path.parent.mkdir(
            parents=True,
            exist_ok=True
        )

        lines = (
            trace
            + [
                f"RESULT={result}",
                f"OPERATIONS={operations}"
            ]
        )

        output_path.write_text(
            "\n".join(lines) + "\n",
            encoding="utf-8"
        )

        print()
        print("Ejecucion funcional completada.")

        print(
            f"Resultado final: {result}"
        )

        print(
            f"Operaciones ejecutadas: {operations}"
        )

        print(
            "resultado.txt generado correctamente en:"
        )

        print(
            output_path.resolve()
        )

    except (
        FileNotFoundError,
        ValueError,
        IndexError
    ) as error:

        print()
        print(
            "Error durante la etapa Python:",
            file=sys.stderr
        )

        print(
            error,
            file=sys.stderr
        )

        # Evitamos conservar un resultado viejo
        # si la ejecución actual falla.
        try:
            output_path.unlink(
                missing_ok=True
            )
        except OSError:
            pass

        sys.exit(1)


if __name__ == "__main__":
    main()