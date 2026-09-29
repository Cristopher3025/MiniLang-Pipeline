package com.minilang.model;

public class MapInstr extends Instruccion {

    private final String operator;
    private final int value;

    public MapInstr(String operator, int value) {
        this.operator = operator;
        this.value = value;
    }

    public String getOperator() {
        return operator;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toIR() {
        return "MAP|" + operator + "|" + value;
    }
}