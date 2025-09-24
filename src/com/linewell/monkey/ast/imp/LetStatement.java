package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.token.Token;

public class LetStatement implements Statement {
    // let 关键字
    private Token token;

    // 变量名
    private Identifier name;

    // 变量的值
    private Expression expression;


    public LetStatement(Token token, Identifier name, Expression expression) {
        this.token = token;
        this.name = name;
        this.expression = expression;
    }

    public LetStatement() {
    }

    public LetStatement(Token token) {
        this.token = token;
    }

    @Override
    public Token getToken() {
        return token;
    }

    public Identifier getName() {
        return name;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public void setName(Identifier name) {
        this.name = name;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }


    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tokenLiteral()).append(" ");
        sb.append(name.toString());
        sb.append(" = ");
        if (expression != null) {
            sb.append(expression);
        }
        sb.append(";");
        return sb.toString();
    }
}
