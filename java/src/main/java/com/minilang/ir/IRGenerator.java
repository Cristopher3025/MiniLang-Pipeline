package com.minilang.ir;

import com.minilang.model.Instruccion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IRGenerator {

    public void generate(
            List<Instruccion> instrucciones,
            Path outputPath) throws IOException {

        // Convertimos cada instruccion a su representacion IR.
        // Aqui se evidencia el polimorfismo:
        // cada objeto ejecuta su propia version de toIR().
        List<String> lines = instrucciones.stream()
                .map(Instruccion::toIR)
                .toList();

        // Creamos la carpeta de salida si no existe.
        Path parent = outputPath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        // Escribimos el archivo programa.ir.
        Files.write(outputPath, lines);
    }
}