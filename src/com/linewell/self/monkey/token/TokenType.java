package com.linewell.self.monkey.token;

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

    // 关键字映射表
    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("fn", FUNCTION);
        keywords.put("let", LET);
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
