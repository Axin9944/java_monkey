package com.linewell.monkey.token;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {
    ILLEGAL("ILLEGAL"),
    EOF("EOF"),

    // 标识符 + 字面量
    IDENT("IDENT"),
    INT("INT"),

    ASSIGN("="),
    PLUS("+"),
    MINUS("-"),
    MULT("*"),
    SLASH("/"),
    BANG("!"),

    LT("<"),
    GT(">"),

    COMMA(","),
    SEMICOLON(";"),

    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),

    FUNCTION("FUNCTION"),
    LET("LET"),
    TRUE("TRUE"),
    FALSE("FALSE"),
    IF("IF"),
    ELSE("ELSE"),
    RETURN("RETURN");


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

    // 关键字映射表
    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("fn", FUNCTION);
        keywords.put("let", LET);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("return", RETURN);
    }

    /***
     * 查找标识符是否为关键字
     * @param ident 标识符名称
     * @return  对应的 TokenType, 如果不是关键字，返回 Token.IDENT
     */
    public static TokenType loopupIdent(String ident) {
        TokenType tokenType = keywords.get(ident);
        return tokenType != null ? tokenType : IDENT;
    }
}
