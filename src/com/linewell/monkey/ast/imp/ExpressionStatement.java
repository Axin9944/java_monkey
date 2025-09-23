package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.token.Token;

/**
 * 表达式语句（Expression Statement）的 AST 节点。
 * 例如，以下代码中的每一行都是一个 {@code ExpressionStatement}：
 * 5;
 * x + 1;
 *
 * @see Statement 语句接口
 * @see Expression 被包装的表达式接口
 * @see Program 程序节点，包含多个 Statement
 */
public class ExpressionStatement implements Statement {
    private Token token;

    private Expression expression;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void statementNode() {

    }

    /**
     * 返回当前语句第一个 Token 的字面量。
     * <p>
     * 例如，对于表达式语句 {@code 5;}，返回 "5"；
     * 对于 {@code x + 1;}，返回 "x"。
     *
     * @return Token 字面量字符串
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 返回该表达式语句的字符串表示形式。
     * <p>
     * 如果包含表达式，则返回表达式的字符串形式（通常为源码风格）；
     * 否则返回空字符串。
     * <p>
     * 示例：
     * <ul>
     *   <li>{@code 5;} → "5;"</li>
     *   <li>{@code x + 1;} → "x + 1;"</li>
     * </ul>
     *
     * @return 源码风格的字符串表示
     */
    @Override
    public String toString() {
        if (expression != null) {
            return expression.toString();
        }
        return "";
    }
}
