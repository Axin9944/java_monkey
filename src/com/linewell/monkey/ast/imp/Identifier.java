package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

public class Identifier implements Expression {

    private final Token token;

    private final String value;

    public Identifier(Token token, String val) {
        this.token = token;
        value = val;
    }

    public Token getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void expressionNode() {
        // 标记方法，空实现
    }

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public String toString() {
        return value;
    }
}
