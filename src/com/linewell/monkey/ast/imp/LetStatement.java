package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.token.Token;

public class LetStatement implements Statement {
    private final Token token;
    private final Identifier name;
    private final Expression expression;


    public LetStatement(Token token, Identifier name, Expression expression) {
        this.token = token;
        this.name = name;
        this.expression = expression;
    }

    public Token getToken() {
        return token;
    }

    public Identifier getName() {
        return name;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void statementNode() {
        // 标记方法，空实现
    }

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }
}
