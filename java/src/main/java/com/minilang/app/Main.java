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

                // Java runs from the java directory, so ".." resolves to the project root.
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

            System.out.println(
                    "[1/4] Leyendo programa.mini..."
            );

            String source = Files.readString(
                    inputPath
            );

            System.out.println(
                    "[2/4] Ejecutando analisis lexico..."
            );

            Lexer lexer = new Lexer(source);

            List<Token> tokens =
                    lexer.tokenize();

            System.out.println(
                    "[3/4] Ejecutando analisis sintactico..."
            );

            Parser parser =
                    new Parser(tokens);

            List<Instruccion> instrucciones =
                    parser.parse();

            System.out.println(
                    "[4/4] Generando programa.ir..."
            );

            IRGenerator generator =
                    new IRGenerator();

            generator.generate(
                    instrucciones,
                    outputPath
            );

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

            System.err.println();

            System.err.println(
                    "El programa MiniLang no es valido."
            );

            System.err.println(
                    e.getMessage()
            );

            // Prevent downstream stages from consuming stale IR after a failed run.
            try {

                Files.deleteIfExists(
                        outputPath
                );

            } catch (IOException ignored) {
            }

            System.exit(1);

        } catch (IOException e) {

            System.err.println();

            System.err.println(
                    "Error al leer o escribir archivos:"
            );

            System.err.println(
                    e.getMessage()
            );

            // Prevent downstream stages from consuming stale IR after a failed run.
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