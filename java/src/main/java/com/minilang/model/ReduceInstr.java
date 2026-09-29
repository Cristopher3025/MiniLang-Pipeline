package com.minilang.model;

public class ReduceInstr extends Instruccion {

    private final String operation;

    public ReduceInstr(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String toIR() {
        return "REDUCE|" + operation;
    }
}