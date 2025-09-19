package com.linewell.monkey.token;

public class Token {
    public TokenType type;
    public String literal;

    public Token(TokenType type, String literal) {
        this.type = type;
        this.literal = literal;
    }

    public TokenType getType() {
        return type;
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public String toString() {
        return String.format("Token{type='%s', literal='%s'}", type, literal);
    }

}
