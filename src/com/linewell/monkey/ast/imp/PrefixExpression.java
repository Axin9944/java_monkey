package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

import java.util.Objects;

/**
 *  表示一个前缀表达式节点。
 *  <p>前缀表达式由一个操作符和一个右操作数构成，操作符位于操作数之前。例如：
 *  <pre>
 *      !true     // 逻辑非
 *      -5        // 负号
 *      ~x        // 按位取反
 *  </pre>
 *  <p>结构：{Operator}{Right}
 *
 */
public class PrefixExpression implements Expression {

    /**
     * 表示该前缀表达式的词法单元（如 "!" 或 "-"）
     */
    private Token token;

    /**
     * 前缀操作符，如 "!", "-", "~"
     */
    private String operator;

    /**
     * 右侧的操作数（必须是 Expression 类型）
     * 例如：在 "-5" 中，5 是 right 表达式
     */
    private Expression right;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
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

    public PrefixExpression() {
    }

    public PrefixExpression(Token token, String operator, Expression right) {
        this.token = token;
        this.operator = operator;
        this.right = right;
    }

    public PrefixExpression(String operator, Expression right) {
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
     * 返回该前缀表达式的字符串表示，用于调试
     * 格式为：({operator}{right})
     * 例如：(!true), (-5)
     *
     * @return 前缀表达式的字符串形式
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(operator);
        if (right != null) {
            sb.append(right.toString());
        }
        sb.append(")");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PrefixExpression)) return false;
        PrefixExpression that = (PrefixExpression) o;
        return Objects.equals(operator, that.operator) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, right);
    }
}
