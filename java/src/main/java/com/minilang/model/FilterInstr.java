package com.minilang.model;

public class FilterInstr extends Instruccion {

    private final String comparator;
    private final int value;

    public FilterInstr(String comparator, int value) {
        this.comparator = comparator;
        this.value = value;
    }

    public String getComparator() {
        return comparator;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toIR() {
        return "FILTER|" + comparator + "|" + value;
    }
}