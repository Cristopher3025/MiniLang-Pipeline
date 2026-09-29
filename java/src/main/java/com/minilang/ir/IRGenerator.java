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

        List<String> lines = instrucciones.stream()
                .map(Instruccion::toIR)
                .toList();

        Path parent = outputPath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.write(outputPath, lines);
    }
}