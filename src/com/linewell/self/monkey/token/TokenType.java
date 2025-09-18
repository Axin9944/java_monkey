package com.linewell.self.monkey.token;

public enum TokenType {
    ILLEGAL("ILLEGAL"),
    EOF("EOF"),

    // 标识符 + 字面量
    IDENT("IDENT"),
    INT("INT"),

    ASSIGN("="),
    PLUS("+"),

    COMMA(","),
    SEMICOLON(";"),

    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),

    FUNCTION("FUNCTION"),
    LET("LET");


    private final String literal;

    TokenType(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public String toString() {
        return literal;
    }
}
