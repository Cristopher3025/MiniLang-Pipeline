package com.minilang.parser;

import com.minilang.lexer.Token;
import com.minilang.lexer.TokenType;
import com.minilang.model.DataInstr;
import com.minilang.model.FilterInstr;
import com.minilang.model.Instruccion;
import com.minilang.model.MapInstr;
import com.minilang.model.PrintInstr;
import com.minilang.model.ReduceInstr;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
    }

    public List<Instruccion> parse() {

        List<Instruccion> instrucciones = new ArrayList<>();

        skipNewLines();

        if (!check(TokenType.DATA)) {
            Token token = peek();

            throw new ParserException(
                    "el programa debe iniciar con DATA; se encontro '"
                            + token.getLexeme() + "'",
                    token.getLine()
            );
        }

        instrucciones.add(parseData());

        skipNewLines();

        if (!isOperation(peek().getType())) {
            Token token = peek();

            throw new ParserException(
                    "se esperaba al menos una operacion FILTER, MAP o REDUCE",
                    token.getLine()
            );
        }

        while (isOperation(peek().getType())) {

            instrucciones.add(parseOperation());

            skipNewLines();
        }

        if (!check(TokenType.PRINT)) {
            Token token = peek();

            throw new ParserException(
                    "se esperaba PRINT; se encontro '"
                            + token.getLexeme() + "'",
                    token.getLine()
            );
        }

        Token printToken = advance();

        instrucciones.add(new PrintInstr());

        skipNewLines();

        if (!check(TokenType.EOF)) {
            Token token = peek();

            throw new ParserException(
                    "no se permiten instrucciones despues de PRINT",
                    token.getLine()
            );
        }

        return instrucciones;
    }

    private DataInstr parseData() {

        Token dataToken = consume(
                TokenType.DATA,
                "se esperaba DATA"
        );

        List<Integer> values = new ArrayList<>();

        if (!check(TokenType.NUMBER)) {
            throw new ParserException(
                    "DATA debe contener al menos un numero",
                    dataToken.getLine()
            );
        }

        while (check(TokenType.NUMBER)) {

            Token number = advance();

            values.add(parseInteger(number));
        }

        requireEndOfLine("DATA");

        return new DataInstr(values);
    }

    private Instruccion parseOperation() {

        if (check(TokenType.FILTER)) {
            return parseFilter();
        }

        if (check(TokenType.MAP)) {
            return parseMap();
        }

        if (check(TokenType.REDUCE)) {
            return parseReduce();
        }

        Token token = peek();

        throw new ParserException(
                "operacion no reconocida '" + token.getLexeme() + "'",
                token.getLine()
        );
    }

    private FilterInstr parseFilter() {

        Token filterToken = consume(
                TokenType.FILTER,
                "se esperaba FILTER"
        );

        if (!isComparator(peek().getType())) {

            Token token = peek();

            throw new ParserException(
                    "FILTER requiere un comparador valido",
                    token.getLine()
            );
        }

        Token comparator = advance();

        Token number = consume(
                TokenType.NUMBER,
                "FILTER requiere un numero"
        );

        requireEndOfLine("FILTER");

        return new FilterInstr(
                comparator.getLexeme(),
                parseInteger(number)
        );
    }

    private MapInstr parseMap() {

        Token mapToken = consume(
                TokenType.MAP,
                "se esperaba MAP"
        );

        if (!isArithmetic(peek().getType())) {

            Token token = peek();

            throw new ParserException(
                    "MAP requiere un operador +, - o *",
                    token.getLine()
            );
        }

        Token operator = advance();

        Token number = consume(
                TokenType.NUMBER,
                "MAP requiere un numero"
        );

        requireEndOfLine("MAP");

        return new MapInstr(
                operator.getLexeme(),
                parseInteger(number)
        );
    }

    private ReduceInstr parseReduce() {

        Token reduceToken = consume(
                TokenType.REDUCE,
                "se esperaba REDUCE"
        );

        if (!check(TokenType.SUM)
                && !check(TokenType.MAX)
                && !check(TokenType.MIN)) {

            Token token = peek();

            throw new ParserException(
                    "REDUCE requiere SUM, MAX o MIN",
                    token.getLine()
            );
        }

        Token operation = advance();

        requireEndOfLine("REDUCE");

        return new ReduceInstr(
                operation.getLexeme()
        );
    }

    private void requireEndOfLine(String instruction) {

        if (!check(TokenType.NEWLINE)
                && !check(TokenType.EOF)) {

            Token token = peek();

            throw new ParserException(
                    "contenido inesperado despues de "
                            + instruction + ": '"
                            + token.getLexeme() + "'",
                    token.getLine()
            );
        }
    }

    private boolean isOperation(TokenType type) {

        return type == TokenType.FILTER
                || type == TokenType.MAP
                || type == TokenType.REDUCE;
    }

    private boolean isComparator(TokenType type) {

        return type == TokenType.GREATER
                || type == TokenType.LESS
                || type == TokenType.GREATER_EQUAL
                || type == TokenType.LESS_EQUAL
                || type == TokenType.EQUAL;
    }

    private boolean isArithmetic(TokenType type) {

        return type == TokenType.PLUS
                || type == TokenType.MINUS
                || type == TokenType.MULTIPLY;
    }

    private int parseInteger(Token token) {

        try {
            return Integer.parseInt(token.getLexeme());

        } catch (NumberFormatException e) {

            throw new ParserException(
                    "numero fuera del rango permitido '"
                            + token.getLexeme() + "'",
                    token.getLine()
            );
        }
    }

    private Token consume(
            TokenType expected,
            String message) {

        if (check(expected)) {
            return advance();
        }

        Token token = peek();

        throw new ParserException(
                message + "; se encontro '"
                        + token.getLexeme() + "'",
                token.getLine()
        );
    }

    private void skipNewLines() {

        while (check(TokenType.NEWLINE)) {
            advance();
        }
    }

    private boolean check(TokenType type) {

        return peek().getType() == type;
    }

    private Token advance() {

        if (!isAtEnd()) {
            current++;
        }

        return previous();
    }

    private boolean isAtEnd() {

        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {

        return tokens.get(current);
    }

    private Token previous() {

        return tokens.get(current - 1);
    }
}