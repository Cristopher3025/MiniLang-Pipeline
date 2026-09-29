# MiniLang Pipeline

> **EIF400 --- Paradigmas de Programación**\
> **Parte B --- Reto práctico en parejas**\
> Pipeline políglota: **MiniLang → Java → IR → Python → MIPS → firma**

------------------------------------------------------------------------

## 1. Descripción general

**MiniLang Pipeline** es una solución políglota que procesa un pequeño
lenguaje de transformación de datos. El sistema recibe un archivo
`programa.mini`, valida su estructura en Java, genera una representación
intermedia, ejecuta las transformaciones mediante un enfoque funcional
en Python y finalmente calcula una firma de verificación utilizando
MIPS.

El flujo completo es:

``` text
programa.mini
      |
      v
+-----------------------+
| JAVA                  |
| Lexer + Parser + OOP  |
+-----------------------+
      |
      v
programa.ir
      |
      v
+-----------------------+
| PYTHON                |
| FILTER / MAP / REDUCE |
+-----------------------+
      |
      v
resultado.txt
      |
      v
+-----------------------+
| MIPS / MARS 4.5       |
| Checksum              |
+-----------------------+
      |
      v
firma.txt
```

Las etapas se comunican mediante archivos reales. No se modifican
manualmente `programa.ir` ni `resultado.txt` para simular una ejecución.

------------------------------------------------------------------------

## 2. Integrantes

-   **Justin Rojas Jarquin**
-   **Cristopher Ureña Valverde**

Curso: **EIF400 Paradigmas de Programación**

------------------------------------------------------------------------

## 3. Tecnologías utilizadas

  Tecnología         Responsabilidad
  ------------------ -------------------------------------------------
  Java               Lexer, parser, modelo OOP y generación de IR
  Python 3           Ejecución funcional de FILTER, MAP y REDUCE
  MIPS               Lectura del resultado y generación del checksum
  MARS 4.5           Ensamblado y ejecución de MIPS
  Batch de Windows   Automatización del pipeline

------------------------------------------------------------------------

## 4. Estructura del proyecto

``` text
MiniLang-Pipeline/
├── java/
│   └── src/main/java/com/minilang/
│       ├── app/
│       │   └── Main.java
│       ├── ir/
│       │   └── IRGenerator.java
│       ├── lexer/
│       │   ├── Lexer.java
│       │   ├── LexerException.java
│       │   ├── Token.java
│       │   └── TokenType.java
│       ├── model/
│       │   ├── Instruccion.java
│       │   ├── DataInstr.java
│       │   ├── FilterInstr.java
│       │   ├── MapInstr.java
│       │   ├── ReduceInstr.java
│       │   └── PrintInstr.java
│       └── parser/
│           ├── Parser.java
│           └── ParserException.java
├── python/
│   ├── main.py
│   └── src/minilang/
│       ├── ir_reader.py
│       └── executor.py
├── mips/
│   └── src/
│       └── checksum.asm
├── data/
│   ├── input/
│   │   └── programa.mini
│   └── output/
│       ├── programa.ir
│       ├── resultado.txt
│       └── firma.txt
├── examples/
│   ├── test01_valid.mini
│   ├── test02_invalid_operator.mini
│   ├── test03_missing_data.mini
│   ├── test04_reduce_max.mini
│   ├── test05_empty_filter.mini
│   └── test06_consecutive_operations.mini
├── docs/
│   ├── pipeline-contract.md
│   ├── technical-decisions.md
│   └── test-evidence.md
├── scripts/
│   └── run_pipeline.bat
├── Mars4_5.jar
├── README.md
└── .gitignore
```

------------------------------------------------------------------------

## 5. Gramática MiniLang

``` bnf
<programa> ::= <data> <operacion> { <operacion> } "PRINT"

<data> ::= "DATA" <numero> { <numero> }

<operacion> ::= <filter> | <map> | <reduce>

<filter> ::= "FILTER" <comparador> <numero>

<map> ::= "MAP" <aritmetico> <numero>

<reduce> ::= "REDUCE" ("SUM" | "MAX" | "MIN")

<comparador> ::= ">" | "<" | ">=" | "<=" | "=="

<aritmetico> ::= "+" | "-" | "*"

<numero> ::= entero no negativo
```

### Ejemplo

``` text
DATA 3 8 5 10 12
FILTER > 5
MAP * 2
REDUCE SUM
PRINT
```

------------------------------------------------------------------------

## 6. Etapa Java

Java es responsable de leer `programa.mini`, realizar el análisis
léxico, validar la gramática y generar `programa.ir`.

### Lexer

El `Lexer` reconoce:

-   palabras reservadas (`DATA`, `FILTER`, `MAP`, `REDUCE`, `PRINT`);
-   reducciones (`SUM`, `MAX`, `MIN`);
-   comparadores (`>`, `<`, `>=`, `<=`, `==`);
-   operadores aritméticos (`+`, `-`, `*`);
-   números enteros no negativos;
-   saltos de línea y fin de archivo.

Cada `Token` conserva su número de línea para producir errores claros.

Ejemplo:

``` text
Error lexico en linea 3: simbolo no reconocido '/'
```

### Parser

El `Parser` comprueba, entre otras reglas:

-   que el programa comience con `DATA`;
-   que `DATA` tenga al menos un número;
-   que exista al menos una operación;
-   que `FILTER` tenga comparador y número;
-   que `MAP` utilice `+`, `-` o `*`;
-   que `REDUCE` utilice `SUM`, `MAX` o `MIN`;
-   que el programa termine con `PRINT`;
-   que no existan instrucciones después de `PRINT`.

### Herencia y polimorfismo

La jerarquía principal es:

``` text
Instruccion (abstracta)
├── DataInstr
├── FilterInstr
├── MapInstr
├── ReduceInstr
└── PrintInstr
```

La clase base define:

``` java
public abstract String toIR();
```

Cada subclase sobrescribe `toIR()`. `IRGenerator` trabaja con objetos de
tipo `Instruccion` y llama polimórficamente al método correspondiente.

### Representación intermedia

Para el ejemplo principal:

``` text
DATA|3,8,5,10,12
FILTER|>|5
MAP|*|2
REDUCE|SUM
PRINT
```

Java genera este archivo únicamente cuando la entrada es válida.

------------------------------------------------------------------------

## 7. Etapa Python

Python lee `data/output/programa.ir` y ejecuta las instrucciones en
orden.

Las transformaciones principales utilizan:

``` python
filter(...)
map(...)
reduce(...)
```

Por ejemplo:

``` text
FILTER > 5 => [8, 10, 12]
MAP * 2 => [16, 20, 24]
REDUCE SUM => 60
RESULT=60
OPERATIONS=3
```

`OPERATIONS` contabiliza las operaciones `FILTER`, `MAP` y `REDUCE`.
`DATA` y `PRINT` no incrementan el contador.

### Lista vacía

Si una operación `FILTER` deja una lista vacía y posteriormente se
intenta ejecutar `REDUCE`, el proyecto genera un error controlado:

``` text
REDUCE no puede ejecutarse sobre una lista vacia.
```

En ese caso el pipeline se detiene antes de MIPS.

### Resultado escalar

Para completar la etapa MIPS, el pipeline necesita un `RESULT` escalar.
En la implementación actual este resultado es producido por `REDUCE`.
Por ello, aunque la gramática permite una secuencia de operaciones sin
`REDUCE`, la ejecución completa requiere que exista una reducción antes
de `PRINT`.

------------------------------------------------------------------------

## 8. Etapa MIPS

MIPS lee `resultado.txt` y busca:

``` text
RESULT=
OPERATIONS=
```

Luego calcula:

``` text
SIGNATURE = (RESULT XOR OPERATIONS) + 17
```

Para el caso principal:

``` text
RESULT=60
OPERATIONS=3

60 XOR 3 = 63
63 + 17 = 80
```

Salida:

``` text
SIGNATURE=80
```

La implementación utiliza registros, memoria, recorridos, operaciones
aritméticas, `xor`, saltos condicionales y syscalls de archivos.

------------------------------------------------------------------------

## 9. Contratos entre etapas

``` text
Java   -- programa.ir --> Python
Python -- resultado.txt --> MIPS
MIPS   -- firma.txt --> salida final
```

Los contratos detallados se encuentran en:

``` text
docs/pipeline-contract.md
```

------------------------------------------------------------------------

## 10. Requisitos de ejecución

Se requiere:

-   Windows;
-   JDK compatible con el proyecto;
-   Python 3;
-   `Mars4_5.jar` en la raíz del proyecto.

Comprobar instalaciones:

``` cmd
java -version
javac -version
python --version
```

------------------------------------------------------------------------

## 11. Compilar Java

Desde la raíz del repositorio:

``` cmd
cd java
javac -d out src\main\java\com\minilang\lexer\*.java src\main\java\com\minilang\model\*.java src\main\java\com\minilang\parser\*.java src\main\java\com\minilang\ir\*.java src\main\java\com\minilang\app\*.java
cd ..
```

Debe recompilarse cuando se modifique el código Java.

------------------------------------------------------------------------

## 12. Ejecución automática

Desde la raíz:

``` cmd
scripts\run_pipeline.bat
```

También puede ejecutarse desde la carpeta `scripts`:

``` cmd
cd scripts
run_pipeline.bat
```

El script ejecuta:

``` text
Java
  ↓
Python
  ↓
MARS / MIPS
```

Para el programa principal, el resultado esperado es:

``` text
RESULT=60
OPERATIONS=3
SIGNATURE=80
```

------------------------------------------------------------------------

## 13. Ejecución manual

### Java

Desde `java/`:

``` cmd
java -cp out com.minilang.app.Main
```

### Python

Desde la raíz:

``` cmd
python python\main.py
```

### MARS

``` cmd
java -jar Mars4_5.jar
```

La ejecución normal recomendada es mediante `run_pipeline.bat`.

------------------------------------------------------------------------

## 14. Manejo de errores

Se aplica una política **fail-fast**:

``` text
Error Java
    ↓
Pipeline detenido

Error Python
    ↓
Pipeline detenido

Error MIPS
    ↓
No se declara ejecución completa
```

Java elimina un `programa.ir` anterior cuando la entrada es inválida.
Python elimina `resultado.txt` cuando su etapa falla.

Esto evita que una etapa posterior utilice resultados obsoletos.

------------------------------------------------------------------------

## 15. Pruebas realizadas

    \# Caso                                   Resultado
  ---- -------------------------------------- ----------------------------
     1 FILTER + MAP + REDUCE SUM + PRINT      `RESULT=60`, firma `80`
     2 Operador `/` inválido                  Error léxico línea 3
     3 Programa sin `DATA`                    Error sintáctico línea 1
     4 `REDUCE MAX`                           `RESULT=24`, firma `44`
     5 FILTER deja lista vacía                Error controlado en Python
     6 FILTER/FILTER y MAP/MAP consecutivos   `RESULT=72`, firma `94`

La evidencia detallada se encuentra en:

``` text
docs/test-evidence.md
```

------------------------------------------------------------------------

## 16. Archivos generados

Una ejecución válida produce:

``` text
data/output/programa.ir
data/output/resultado.txt
data/output/firma.txt
```

Estos archivos son generados por las etapas correspondientes y no deben
modificarse manualmente para simular una ejecución.

------------------------------------------------------------------------

## 17. Documentación

``` text
docs/pipeline-contract.md
```

Describe los contratos de datos entre las tres etapas.

``` text
docs/technical-decisions.md
```

Explica las decisiones de diseño, paradigmas y justificaciones técnicas.

``` text
docs/test-evidence.md
```

Registra las pruebas realizadas y sus resultados.

------------------------------------------------------------------------

## 18. Demostración recomendada

Para una defensa rápida:

1.  Mostrar `data/input/programa.mini`.
2.  Ejecutar `scripts\run_pipeline.bat`.
3.  Mostrar `programa.ir`.
4.  Mostrar `resultado.txt`.
5.  Mostrar `firma.txt`.
6.  Ejecutar un caso inválido y demostrar que el pipeline se detiene.
7.  Explicar la jerarquía `Instruccion`.
8.  Explicar el uso de `filter`, `map` y `reduce`.
9.  Explicar el checksum `(RESULT XOR OPERATIONS) + 17`.

------------------------------------------------------------------------

## 19. Resultado principal comprobado

``` text
DATA 3 8 5 10 12
FILTER > 5
MAP * 2
REDUCE SUM
PRINT
```

produce:

``` text
RESULT=60
OPERATIONS=3
SIGNATURE=80
```

------------------------------------------------------------------------

## 20. Autores

**Justin Rojas Jarquin**\
**Cristopher Ureña Valverde**

Universidad Nacional\
EIF400 --- Paradigmas de Programación
