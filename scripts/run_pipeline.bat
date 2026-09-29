@echo off
setlocal

REM ============================================================
REM MiniLang Pipeline
REM Java -> Python -> MIPS
REM ============================================================

REM Nos movemos a la raiz del proyecto sin importar
REM desde donde se haya ejecutado este .bat.
cd /d "%~dp0.."

echo.
echo ============================================================
echo               MINILANG PIPELINE
echo ============================================================
echo.

REM ============================================================
REM 1. JAVA
REM ============================================================

echo [1/3] Ejecutando etapa Java...
echo.

cd java

java -cp out com.minilang.app.Main

if errorlevel 1 (
    echo.
    echo ERROR: La etapa Java fallo.
    echo Pipeline detenido.
    cd ..
    exit /b 1
)

cd ..

REM Verificamos que Java realmente haya producido programa.ir.
if not exist "data\output\programa.ir" (
    echo.
    echo ERROR: Java no genero programa.ir.
    echo Pipeline detenido.
    exit /b 1
)

echo.
echo Etapa Java completada.
echo ------------------------------------------------------------
echo.

REM ============================================================
REM 2. PYTHON
REM ============================================================

echo [2/3] Ejecutando etapa Python...
echo.

python python\main.py

if errorlevel 1 (
    echo.
    echo ERROR: La etapa Python fallo.
    echo Pipeline detenido.
    exit /b 1
)

REM Verificamos que Python haya producido resultado.txt.
if not exist "data\output\resultado.txt" (
    echo.
    echo ERROR: Python no genero resultado.txt.
    echo Pipeline detenido.
    exit /b 1
)

echo.
echo Etapa Python completada.
echo ------------------------------------------------------------
echo.

REM ============================================================
REM 3. MIPS / MARS
REM ============================================================

echo [3/3] Ejecutando etapa MIPS con MARS 4.5...
echo.

if not exist "Mars4_5.jar" (
    echo ERROR: No se encontro Mars4_5.jar en la raiz.
    echo Pipeline detenido.
    exit /b 1
)

java -jar Mars4_5.jar nc ae1 se1 sm mips\src\checksum.asm

if errorlevel 1 (
    echo.
    echo ERROR: La etapa MIPS fallo.
    echo Pipeline detenido.
    exit /b 1
)

REM Verificamos que MIPS haya producido firma.txt.
if not exist "data\output\firma.txt" (
    echo.
    echo ERROR: MIPS no genero firma.txt.
    echo Pipeline detenido.
    exit /b 1
)

echo.
echo Etapa MIPS completada.
echo ------------------------------------------------------------
echo.

REM ============================================================
REM PIPELINE COMPLETADO
REM ============================================================

echo ============================================================
echo           PIPELINE COMPLETADO CORRECTAMENTE
echo ============================================================
echo.
echo Archivos generados:
echo.
echo   data\output\programa.ir
echo   data\output\resultado.txt
echo   data\output\firma.txt
echo.

echo Firma generada:
type data\output\firma.txt

echo.
echo ============================================================

exit /b 0