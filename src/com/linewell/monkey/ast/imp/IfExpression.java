package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

/**
 * AST 节点：if 表达式（IfExpression）。
 *
 * <p>用于表示条件判断语句，例如：</p>
 * <pre>
 * if (x < y) { return x; } else { return y; }
 * </pre>
 *
 * <p>核心职责：</p>
 * <ul>
 *     <li>保存起始 {@link Token}（"if"）</li>
 *     <li>保存条件表达式 {@link #condition}</li>
 *     <li>保存条件为真时的语句块 {@link #consequence}</li>
 *     <li>保存条件为假时的语句块 {@link #alternative}（可选）</li>
 *     <li>实现 {@link #toString()} 方法，将 if 表达式序列化为源码风格字符串</li>
 * </ul>
 */
public class IfExpression implements Expression {

    // if 关键字对应的词法单元
    private Token token;

    // 条件表达式
    private Expression condition;

    // 条件为真时执行的语句块
    private BlockStatement consequence;

    // 条件为假时执行的语句块（可选）
    private BlockStatement alternative;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public BlockStatement getAlternative() {
        return alternative;
    }

    public void setAlternative(BlockStatement alternative) {
        this.alternative = alternative;
    }

    public BlockStatement getConsequence() {
        return consequence;
    }

    public void setConsequence(BlockStatement consequence) {
        this.consequence = consequence;
    }

    public IfExpression() {
    }

    public IfExpression(Token token, Expression condition, BlockStatement consequence, BlockStatement alternative) {
        this.token = token;
        this.condition = condition;
        this.consequence = consequence;
        this.alternative = alternative;
    }

    /**
     * 将 if 表达式序列化为字符串。
     * 格式类似于：
     * <pre>
     * if (condition) consequence else alternative
     * </pre>
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("if");
        sb.append(condition.toString());
        sb.append(" ");
        sb.append(consequence.toString());
        if (alternative != null) {
            sb.append("else ");
            sb.append(alternative.toString());
        }

        return sb.toString();
    }

    /**
     * 返回 if 表达式的起始 token 字面量。
     * 通常就是 "if"。
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }
}
