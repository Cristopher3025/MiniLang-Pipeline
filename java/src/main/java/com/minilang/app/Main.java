package com.minilang.app;

import com.minilang.ir.IRGenerator;
import com.minilang.lexer.Lexer;
import com.minilang.lexer.LexerException;
import com.minilang.lexer.Token;
import com.minilang.model.Instruccion;
import com.minilang.parser.Parser;
import com.minilang.parser.ParserException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        /*
         * Ejecutamos Java desde la carpeta /java.
         *
         * Por eso ".." nos devuelve a la raiz:
         *
         * MiniLang-Pipeline/
         */
        Path inputPath = Path.of(
                "..",
                "data",
                "input",
                "programa.mini"
        );

        Path outputPath = Path.of(
                "..",
                "data",
                "output",
                "programa.ir"
        );

        try {

            System.out.println("=== MiniLang - Etapa Java ===");
            System.out.println();

            // =================================================
            // 1. LEER programa.mini
            // =================================================

            System.out.println(
                    "[1/4] Leyendo programa.mini..."
            );

            String source = Files.readString(
                    inputPath
            );

            // =================================================
            // 2. ANALISIS LEXICO
            // =================================================

            System.out.println(
                    "[2/4] Ejecutando analisis lexico..."
            );

            Lexer lexer = new Lexer(source);

            List<Token> tokens =
                    lexer.tokenize();

            // =================================================
            // 3. ANALISIS SINTACTICO
            // =================================================

            System.out.println(
                    "[3/4] Ejecutando analisis sintactico..."
            );

            Parser parser =
                    new Parser(tokens);

            List<Instruccion> instrucciones =
                    parser.parse();

            // =================================================
            // 4. GENERACION DEL IR
            // =================================================

            System.out.println(
                    "[4/4] Generando programa.ir..."
            );

            IRGenerator generator =
                    new IRGenerator();

            generator.generate(
                    instrucciones,
                    outputPath
            );

            // =================================================
            // EJECUCION CORRECTA
            // =================================================

            System.out.println();

            System.out.println(
                    "Programa MiniLang valido."
            );

            System.out.println(
                    "IR generado correctamente en:"
            );

            System.out.println(
                    outputPath
                            .toAbsolutePath()
                            .normalize()
            );

        } catch (LexerException | ParserException e) {

            // =================================================
            // ERROR LEXICO O SINTACTICO
            // =================================================

            System.err.println();

            System.err.println(
                    "El programa MiniLang no es valido."
            );

            System.err.println(
                    e.getMessage()
            );

            /*
             * Eliminamos un programa.ir anterior.
             *
             * Esto evita que Python utilice accidentalmente
             * un IR viejo cuando la ejecucion actual fallo.
             */
            try {

                Files.deleteIfExists(
                        outputPath
                );

            } catch (IOException ignored) {
            }

            /*
             * Indicamos al sistema operativo que Java fallo.
             *
             * Esto permite que run_pipeline.bat detecte
             * el error y detenga todo el pipeline.
             */
            System.exit(1);

        } catch (IOException e) {

            // =================================================
            // ERROR DE ARCHIVOS
            // =================================================

            System.err.println();

            System.err.println(
                    "Error al leer o escribir archivos:"
            );

            System.err.println(
                    e.getMessage()
            );

            /*
             * Tambien eliminamos cualquier IR anterior
             * para evitar resultados inconsistentes.
             */
            try {

                Files.deleteIfExists(
                        outputPath
                );

            } catch (IOException ignored) {
            }

            System.exit(1);
        }
    }
}