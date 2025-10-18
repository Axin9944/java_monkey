package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

import java.util.Objects;

/**
 * 表示 Monkey 语言中的数组索引表达式（Index Expression）。
 * <p>
 * 对应语法形式：
 * <pre>
 *     array[index]
 * </pre>
 *
 * 示例：
 * <pre>
 *     myArray[0]
 * </pre>
 *
 * 该节点在 AST 中保存了：
 * <ul>
 *     <li>触发该节点的 token（通常为 "["）</li>
 *     <li>被索引的左侧表达式（left）</li>
 *     <li>索引表达式（index）</li>
 * </ul>
 */
public class IndexExpression implements Expression {

    /** 触发该索引操作的词法单元（通常为 '[' ） */
    private Token token;

    /** 被索引的左侧表达式，例如数组或哈希表 */
    private Expression left;

    /** 索引表达式 */
    private Expression index;

    public IndexExpression() {
    }

    public IndexExpression(Token token, Expression left, Expression index) {
        this.token = token;
        this.left = left;
        this.index = index;
    }

    public IndexExpression(Expression left, Expression index) {
        this.left = left;
        this.index = index;
    }

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

    public Expression getIndex() {
        return index;
    }

    public void setIndex(Expression index) {
        this.index = index;
    }

    /**
     * 返回该表达式对应的词法字面值。
     *
     * @return token 的字面值
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 以 Monkey 语言的语法格式返回索引表达式的字符串表示。
     *
     * @return 索引表达式字符串，例如 "(myArray[0])"
     */
    @Override
    public String toString() {
        StringBuilder ie = new StringBuilder();

        ie.append("(");
        if (left != null) {
            ie.append(left);
        }
        ie.append("[");
        if (index != null) {
            ie.append(index);
        }
        ie.append("])");

        return ie.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IndexExpression)) return false;
        IndexExpression that = (IndexExpression) o;
        return Objects.equals(left, that.left) && Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, index);
    }
}
