from functools import reduce


def execute(instructions: list[list[str]]) -> tuple[list[str], int, int]:
    """
    Ejecuta las instrucciones provenientes de programa.ir.

    Retorna:
        trace      -> lineas que se escribiran en resultado.txt
        result     -> resultado numerico final
        operations -> cantidad de FILTER, MAP y REDUCE ejecutados
    """

    data = []
    trace = []
    operations = 0
    result = None

    for instruction in instructions:

        command = instruction[0]

        if command == "DATA":

            data = list(
                map(
                    int,
                    instruction[1].split(",")
                )
            )

        elif command == "FILTER":

            comparator = instruction[1]
            value = int(instruction[2])

            data = apply_filter(
                data,
                comparator,
                value
            )

            operations += 1

            trace.append(
                f"FILTER {comparator} {value} => {data}"
            )

        elif command == "MAP":

            operator = instruction[1]
            value = int(instruction[2])

            data = apply_map(
                data,
                operator,
                value
            )

            operations += 1

            trace.append(
                f"MAP {operator} {value} => {data}"
            )

        elif command == "REDUCE":

            operation = instruction[1]

            result = apply_reduce(
                data,
                operation
            )

            operations += 1

            trace.append(
                f"REDUCE {operation} => {result}"
            )

        elif command == "PRINT":
            pass

        else:
            raise ValueError(
                f"Instruccion IR desconocida: {command}"
            )

    if result is None:
        raise ValueError(
            "El programa no produjo un resultado REDUCE."
        )

    return trace, result, operations


def apply_filter(
        data: list[int],
        comparator: str,
        value: int
) -> list[int]:

    comparators = {
        ">": lambda x: x > value,
        "<": lambda x: x < value,
        ">=": lambda x: x >= value,
        "<=": lambda x: x <= value,
        "==": lambda x: x == value
    }

    if comparator not in comparators:
        raise ValueError(
            f"Comparador no valido: {comparator}"
        )

    return list(
        filter(
            comparators[comparator],
            data
        )
    )


def apply_map(
        data: list[int],
        operator: str,
        value: int
) -> list[int]:

    operations = {
        "+": lambda x: x + value,
        "-": lambda x: x - value,
        "*": lambda x: x * value
    }

    if operator not in operations:
        raise ValueError(
            f"Operador MAP no valido: {operator}"
        )

    return list(
        map(
            operations[operator],
            data
        )
    )


def apply_reduce(
        data: list[int],
        operation: str
) -> int:

    if not data:
        raise ValueError(
            "REDUCE no puede ejecutarse sobre una lista vacia."
        )

    if operation == "SUM":

        return reduce(
            lambda accumulator, value:
                accumulator + value,
            data
        )

    if operation == "MAX":

        return reduce(
            lambda a, b: a if a > b else b,
            data
        )

    if operation == "MIN":

        return reduce(
            lambda a, b: a if a < b else b,
            data
        )

    raise ValueError(
        f"Operacion REDUCE no valida: {operation}"
    )