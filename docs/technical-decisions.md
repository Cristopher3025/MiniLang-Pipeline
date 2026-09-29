# Decisiones Técnicas --- MiniLang Pipeline

## 1. Propósito

Este documento registra las principales decisiones de diseño tomadas
durante la implementación de **MiniLang Pipeline**, incluyendo la
selección de paradigmas, la representación intermedia, el manejo de
errores y la integración entre Java, Python y MIPS.

------------------------------------------------------------------------

## 2. Arquitectura general

La solución se divide en tres etapas:

``` text
Java
  ↓
Análisis léxico + análisis sintáctico + OOP
  ↓
programa.ir
  ↓
Python
  ↓
Ejecución funcional
  ↓
resultado.txt
  ↓
MIPS
  ↓
Verificación / checksum
  ↓
firma.txt
```

La comunicación se realiza mediante archivos definidos explícitamente.
Cada programa conoce el formato que recibe y el formato que debe
producir, pero no necesita conocer la implementación interna de las
otras etapas.

------------------------------------------------------------------------

## 3. Decisiones de la etapa Java

### 3.1 Separación entre Lexer y Parser

Se decidió separar el análisis léxico del análisis sintáctico.

El `Lexer` convierte el texto de `programa.mini` en tokens. Entre ellos:

``` text
DATA
FILTER
MAP
REDUCE
PRINT
SUM
MAX
MIN
>
<
>=
<=
==
+
-
*
NUMBER
```

El `Parser` trabaja posteriormente con esos tokens y comprueba que su
orden corresponda con la gramática MiniLang.

Esta separación permite distinguir claramente dos tipos de errores:

``` text
Error léxico
→ el símbolo o palabra no pertenece al vocabulario.

Error sintáctico
→ los tokens existen, pero están organizados incorrectamente.
```

### 3.2 Conservación del número de línea

Cada objeto `Token` almacena:

``` text
tipo
lexema
línea
```

Esto permite producir errores más útiles.

Ejemplo comprobado:

``` text
Error lexico en linea 3: simbolo no reconocido '/'
```

También se comprobó:

``` text
Error sintactico en linea 1:
el programa debe iniciar con DATA; se encontro 'FILTER'
```

### 3.3 Jerarquía orientada a objetos

Se definió:

``` java
public abstract class Instruccion {
    public abstract String toIR();
}
```

y las clases:

``` text
DataInstr
FilterInstr
MapInstr
ReduceInstr
PrintInstr
```

Cada subclase contiene los datos correspondientes a su instrucción y
sobrescribe `toIR()`.

### 3.4 Uso real de polimorfismo

`IRGenerator` trabaja con:

``` java
List<Instruccion>
```

y puede ejecutar:

``` java
.map(Instruccion::toIR)
```

sin preguntar si cada objeto es `DataInstr`, `MapInstr`, `FilterInstr`,
etc.

Por tanto:

-   **herencia:** las instrucciones concretas extienden `Instruccion`;
-   **sobrescritura:** cada subclase implementa su propio `toIR()`;
-   **polimorfismo:** `IRGenerator` utiliza objetos `Instruccion` y el
    método correcto se resuelve según el objeto concreto.

### 3.5 Representación intermedia textual

Se eligió un formato separado por `|`:

``` text
DATA|3,8,5,10,12
FILTER|>|5
MAP|*|2
REDUCE|SUM
PRINT
```

La razón es que el formato es:

-   pequeño;
-   legible;
-   sencillo de generar en Java;
-   sencillo de separar en Python;
-   independiente de las clases internas de Java.

------------------------------------------------------------------------

## 4. Decisiones de la etapa Python

### 4.1 Estilo funcional

Las operaciones principales se implementan mediante funciones de orden
superior.

`FILTER` utiliza:

``` python
filter(...)
```

`MAP` utiliza:

``` python
map(...)
```

`REDUCE` utiliza:

``` python
functools.reduce(...)
```

Los comparadores y operadores se representan mediante funciones lambda.

Esto permite describir principalmente la transformación que se desea
realizar en lugar de controlar manualmente cada iteración.

### 4.2 FILTER

Los comparadores soportados son:

``` text
>
<
>=
<=
==
```

Cada comparador corresponde a una función que se aplica mediante
`filter()`.

### 4.3 MAP

Los operadores soportados son:

``` text
+
-
*
```

Cada operador se transforma en una función y se aplica a todos los
elementos mediante `map()`.

### 4.4 REDUCE

Las reducciones disponibles son:

``` text
SUM
MAX
MIN
```

Estas operaciones producen el resultado escalar que posteriormente
consume MIPS.

### 4.5 Traza de ejecución

`resultado.txt` no contiene únicamente el valor final. También conserva
una traza legible.

Ejemplo:

``` text
FILTER > 5 => [8, 10, 12]
MAP * 2 => [16, 20, 24]
REDUCE SUM => 60
RESULT=60
OPERATIONS=3
```

Esto facilita verificar la ejecución y explicar el proyecto durante la
defensa.

### 4.6 Decisión sobre listas vacías

Se decidió que cualquier `REDUCE` sobre una lista vacía produzca un
error controlado.

Ejemplo:

``` text
DATA 1 2 3 4 5
FILTER > 100
REDUCE SUM
PRINT
```

`FILTER > 100` produce:

``` text
[]
```

y posteriormente Python genera:

``` text
REDUCE no puede ejecutarse sobre una lista vacia.
```

La decisión mantiene una política uniforme para `SUM`, `MAX` y `MIN`.

------------------------------------------------------------------------

## 5. Resultado escalar y REDUCE

La gramática permite que una operación sea:

``` text
FILTER
MAP
REDUCE
```

Sin embargo, la integración completa necesita producir:

``` text
RESULT=<entero>
```

para que MIPS pueda realizar la verificación.

En la implementación actual, `RESULT` es producido por `REDUCE`.

Por esta razón, una entrada que tenga únicamente `FILTER` o `MAP` puede
ser aceptada sintácticamente por Java, pero la etapa Python no considera
completa la ejecución si nunca se obtiene un resultado de reducción.

Esta condición se considera una **restricción semántica del pipeline**,
no una regla léxica.

------------------------------------------------------------------------

## 6. Decisiones de la etapa MIPS

### 6.1 Consumo de datos reales

MIPS no utiliza constantes para representar el resultado.

Lee directamente:

``` text
data/output/resultado.txt
```

y busca:

``` text
RESULT=
OPERATIONS=
```

De esta manera la tercera etapa depende realmente de la ejecución de
Python.

### 6.2 Búsqueda en memoria

El contenido del archivo se carga en un buffer.

La rutina:

``` text
find_key
```

recorre ese buffer para localizar las cadenas requeridas.

Esto permite demostrar:

-   registros;
-   acceso a memoria;
-   recorridos;
-   comparaciones;
-   saltos condicionales.

### 6.3 Conversión ASCII

La rutina:

``` text
ascii_to_integer
```

convierte los caracteres encontrados en el archivo a un valor numérico
que puede utilizarse en registros.

### 6.4 Checksum

La fórmula implementada es:

``` text
SIGNATURE = (RESULT XOR OPERATIONS) + 17
```

Para el programa principal:

``` text
RESULT=60
OPERATIONS=3

60 XOR 3 = 63
63 + 17 = 80
```

Resultado:

``` text
SIGNATURE=80
```

El uso de `XOR` permite incorporar explícitamente una operación lógica.

### 6.5 Escritura de la firma

Después del cálculo, MIPS convierte el valor a texto y genera:

``` text
data/output/firma.txt
```

con el formato:

``` text
SIGNATURE=<valor>
```

------------------------------------------------------------------------

## 7. Automatización del pipeline

Se creó:

``` text
scripts/run_pipeline.bat
```

El script ejecuta:

``` text
1. Java
2. Verificación de programa.ir
3. Python
4. Verificación de resultado.txt
5. MARS / MIPS
6. Verificación de firma.txt
```

Además utiliza:

``` bat
cd /d "%~dp0.."
```

para posicionarse automáticamente en la raíz del proyecto aunque el
script sea ejecutado desde `scripts`.

------------------------------------------------------------------------

## 8. Política fail-fast

Se decidió que un error detenga inmediatamente el pipeline.

Ejemplo:

``` text
programa.mini inválido
        ↓
Java detecta el error
        ↓
elimina programa.ir anterior
        ↓
System.exit(1)
        ↓
run_pipeline.bat detecta el error
        ↓
Python NO se ejecuta
```

Python aplica una estrategia equivalente eliminando `resultado.txt`
cuando su ejecución falla.

Esto evita utilizar accidentalmente archivos generados por una ejecución
anterior.

------------------------------------------------------------------------

## 9. Contratos entre lenguajes

La integración no depende de compartir clases, objetos o memoria entre
lenguajes.

Los contratos son:

``` text
Java
  ↓
programa.ir
  ↓
Python
  ↓
resultado.txt
  ↓
MIPS
  ↓
firma.txt
```

Esta arquitectura reduce el acoplamiento: cada etapa necesita conocer
únicamente el formato de entrada y salida.

------------------------------------------------------------------------

## 10. Reflexión: ¿por qué Java para análisis y modelado?

Java permite representar cada instrucción mediante una clase concreta y
una abstracción común.

Su sistema de tipos facilita distinguir tokens, instrucciones y
responsabilidades. Además, la herencia y el polimorfismo permiten
modelar el lenguaje sin depender de grandes cadenas de `if/else`.

En este proyecto, `Instruccion::toIR()` demuestra directamente esa
ventaja.

------------------------------------------------------------------------

## 11. Reflexión: imperativo frente a funcional

En un enfoque imperativo se describe principalmente **cómo** realizar el
recorrido:

``` text
crear variable
recorrer colección
evaluar condición
actualizar estado
```

En el enfoque funcional utilizado en Python se describe principalmente
**qué transformación** se desea:

``` text
filter → seleccionar
map    → transformar
reduce → combinar
```

La iteración continúa existiendo internamente, pero el código de la
aplicación delega el recorrido a abstracciones funcionales.

------------------------------------------------------------------------

## 12. Reflexión: información conservada y perdida en el IR

Al convertir `programa.mini` a `programa.ir` se conserva la información
necesaria para ejecutar el programa:

-   datos;
-   orden de operaciones;
-   comparadores;
-   operadores;
-   operandos;
-   reducción;
-   `PRINT`.

Por ejemplo:

``` text
FILTER > 5
```

se normaliza como:

``` text
FILTER|>|5
```

En cambio, se pierde información superficial que no es necesaria para
Python, como espacios utilizados para presentación y la representación
interna de objetos Java.

El IR conserva la intención operacional, pero desacopla a Python del
Lexer, Parser y modelo interno de Java.

------------------------------------------------------------------------

## 13. Reflexión: relación del IR con un compilador

Una representación intermedia permite transformar el programa fuente a
una forma más conveniente para una etapa posterior.

MiniLang realiza:

``` text
Fuente MiniLang
      ↓
Lexer / Parser
      ↓
Representación intermedia
      ↓
Ejecución posterior
```

Python no necesita analizar nuevamente la sintaxis original. Solo
interpreta el contrato normalizado de `programa.ir`.

Por ello, `programa.ir` cumple un papel comparable al de una
representación intermedia dentro de un compilador.

------------------------------------------------------------------------

## 14. Reflexión: ventajas y costos de integrar tres lenguajes

### Ventajas

Cada lenguaje permite demostrar un paradigma o nivel de abstracción
diferente:

``` text
Java   → orientación a objetos y análisis
Python → estilo funcional
MIPS   → programación de bajo nivel
```

Los contratos también permiten separar responsabilidades y reducir el
conocimiento interno entre componentes.

### Costos

La integración introduce:

-   varios entornos de ejecución;
-   más puntos posibles de fallo;
-   necesidad de mantener formatos compatibles;
-   propagación de errores entre etapas;
-   mayor complejidad de despliegue y pruebas.

El proyecto reduce estos costos mediante contratos documentados,
validaciones y `run_pipeline.bat`.

Por tanto, utilizar tres lenguajes no se considera automáticamente mejor
que utilizar uno. En este proyecto se justifica porque permite aplicar y
demostrar explícitamente diferentes paradigmas dentro de una misma
solución.

------------------------------------------------------------------------

## 15. Organización y mantenibilidad

Java está dividido por responsabilidades:

``` text
lexer/
parser/
model/
ir/
app/
```

Python separa:

``` text
ir_reader.py
executor.py
```

y MIPS concentra la verificación final en:

``` text
checksum.asm
```

Esta organización facilita localizar errores y explicar cada componente
durante la defensa.

------------------------------------------------------------------------

## 16. Limitaciones conocidas

La implementación actual está orientada al contrato requerido por el
proyecto.

La ejecución completa necesita un resultado escalar producido por
`REDUCE`.

Además, si se extiende MiniLang con nuevas instrucciones u operadores,
deben actualizarse de forma coordinada:

``` text
Lexer
Parser
Modelo OOP
Contrato IR
Ejecutor Python
y, cuando corresponda, contrato MIPS
```

------------------------------------------------------------------------

## 17. Resumen

  Área             Decisión
  ---------------- -------------------------------------
  Análisis         Lexer y Parser separados
  Errores          Línea + detención del pipeline
  OOP              `Instruccion` abstracta y subclases
  Polimorfismo     `toIR()` sobrescrito
  IR               Texto delimitado por `|`
  Python           `filter`, `map`, `reduce`
  Lista vacía      REDUCE genera error controlado
  Resultado        REDUCE produce el escalar para MIPS
  MIPS             Consume datos reales de Python
  Checksum         `(RESULT XOR OPERATIONS) + 17`
  Integración      Contratos mediante archivos
  Automatización   `run_pipeline.bat`
  Fallos           Política fail-fast

------------------------------------------------------------------------

## 18. Autores

**Justin Rojas Jarquin**\
**Cristopher Ureña Valverde**

EIF400 --- Paradigmas de Programación
