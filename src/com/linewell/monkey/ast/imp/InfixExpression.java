package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

import java.util.Objects;

/**
 * 表示一个中缀表达式节点。
 * <p>中缀表达式由左操作数、操作符和右操作数构成，操作符位于中间。例如：
 *      <pre>
 *          5 + 5
 *          x == y
 *          a * b - c
 *      </pre>
 * </p> 结构：{Left} {Operator} {Right}
 *
 */
public class InfixExpression implements Expression {

    /**
     * 表示该中缀表达式操作符的词法单元（如 "+" 或 "==")
     */
    private Token token;

    /**
     * 左侧的操作数表达式
     * 例如：在 "5 + 3" 中，5 是 left 表达式
     */
    private Expression left;

    /**
     * 中缀操作符，如 "+", "-", "==", "!=", "*", "/"
     */
    private String operator;

    /**
     * 右侧的操作数表达式
     * 例如：在 "5 + 3" 中，3 是 right 表达式
     */
    private Expression right;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public InfixExpression() {
    }

    public InfixExpression(Token token, Expression left, String operator, Expression right) {
        this.token = token;
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public InfixExpression(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * 返回该节点的词法单元字面量（即操作符的文本）
     *
     * @return 操作符的原始文本，若 token 为 null 则返回空字符串
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 返回该中缀表达式的字符串表示，用于调试和 AST 打印
     * 格式为：({left} {operator} {right})
     * 例如：(5 + 3), (x == y)
     *
     * @return 中缀表达式的字符串形式
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (left != null) {
            sb.append(left.toString());
        }
        sb.append(" ").append(operator != null ? operator : "").append(" ");
        if (right != null) {
            sb.append(right.toString());
        }
        sb.append(")");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InfixExpression)) return false;
        InfixExpression that = (InfixExpression) o;
        return Objects.equals(left, that.left) && Objects.equals(operator, that.operator) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, operator, right);
    }
}
