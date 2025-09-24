package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.token.Token;

public class ReturnStatement implements Statement {
    // return 关键字
    private Token token;

    // return 要返回的值
    private Expression returnValue;

    public void setToken(Token token) {
        this.token = token;
    }

    public Expression getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Expression returnValue) {
        this.returnValue = returnValue;
    }

    public ReturnStatement(Token token, Expression returnValue) {
        this.token = token;
        this.returnValue = returnValue;
    }

    public ReturnStatement() {
    }

    public ReturnStatement(Token token) {
        this.token = token;
    }


    @Override
    public Token getToken() {
        return token;
    }

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tokenLiteral()).append(" ");
        if (returnValue != null) {
            sb.append(returnValue);
        }
        sb.append(";");
        return sb.toString();
    }
}
