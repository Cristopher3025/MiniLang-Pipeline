package com.minilang.model;

public class PrintInstr extends Instruccion {

    @Override
    public String toIR() {
        return "PRINT";
    }
}