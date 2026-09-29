@echo off
setlocal

cd /d "%~dp0.."

echo.
echo ============================================================
echo                 MINILANG PIPELINE
echo ============================================================
echo.

echo [1/4] Compilando etapa Java...
echo.

cd java

if not exist "out" mkdir out

javac -d out src\main\java\com\minilang\lexer\*.java src\main\java\com\minilang\model\*.java src\main\java\com\minilang\parser\*.java src\main\java\com\minilang\ir\*.java src\main\java\com\minilang\app\*.java

if errorlevel 1 (
    echo.
    echo ============================================================
    echo ERROR: La compilacion de Java fallo.
    echo Pipeline detenido.
    echo ============================================================
    echo.
    cd ..
    pause
    exit /b 1
)

echo.
echo Compilacion Java completada.
echo ------------------------------------------------------------
echo.

echo [2/4] Ejecutando etapa Java...
echo.

java -cp out com.minilang.app.Main

if errorlevel 1 (
    echo.
    echo ============================================================
    echo ERROR: La etapa Java fallo.
    echo Pipeline detenido.
    echo ============================================================
    echo.
    cd ..
    pause
    exit /b 1
)

cd ..

if not exist "data\output\programa.ir" (
    echo.
    echo ============================================================
    echo ERROR: Java no genero programa.ir.
    echo Pipeline detenido.
    echo ============================================================
    echo.
    pause
    exit /b 1
)

echo.
echo Etapa Java completada.
echo ------------------------------------------------------------
echo.

echo [3/4] Ejecutando etapa Python...
echo.

python python\main.py

if errorlevel 1 (
    echo.
    echo ============================================================
    echo ERROR: La etapa Python fallo.
    echo Pipeline detenido.
    echo ============================================================
    echo.
    pause
    exit /b 1
)

if not exist "data\output\resultado.txt" (
    echo.
    echo ============================================================
    echo ERROR: Python no genero resultado.txt.
    echo Pipeline detenido.
    echo ============================================================
    echo.
    pause
    exit /b 1
)

echo.
echo Etapa Python completada.
echo ------------------------------------------------------------
echo.

echo [4/4] Ejecutando etapa MIPS con MARS 4.5...
echo.

if not exist "Mars4_5.jar" (
    echo.
    echo ============================================================
    echo ERROR: No se encontro Mars4_5.jar en la raiz.
    echo Pipeline detenido.
    echo ============================================================
    echo.
    pause
    exit /b 1
)

java -jar Mars4_5.jar nc ae1 se1 sm mips\src\checksum.asm

if errorlevel 1 (
    echo.
    echo ============================================================
    echo ERROR: La etapa MIPS fallo.
    echo Pipeline detenido.
    echo ============================================================
    echo.
    pause
    exit /b 1
)

if not exist "data\output\firma.txt" (
    echo.
    echo ============================================================
    echo ERROR: MIPS no genero firma.txt.
    echo Pipeline detenido.
    echo ============================================================
    echo.
    pause
    exit /b 1
)

echo.
echo Etapa MIPS completada.
echo ------------------------------------------------------------
echo.

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
echo.
type data\output\firma.txt

echo.
echo ============================================================
echo.
echo El pipeline ha finalizado.
echo Presione una tecla para cerrar esta ventana...
echo.

pause >nul

exit /b 0