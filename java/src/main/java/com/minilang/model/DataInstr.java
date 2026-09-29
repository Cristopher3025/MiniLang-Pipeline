package com.minilang.model;

import java.util.List;
import java.util.stream.Collectors;

public class DataInstr extends Instruccion {

    private final List<Integer> values;

    public DataInstr(List<Integer> values) {
        this.values = values;
    }

    public List<Integer> getValues() {
        return values;
    }

    @Override
    public String toIR() {

        String numbers = values.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return "DATA|" + numbers;
    }
}