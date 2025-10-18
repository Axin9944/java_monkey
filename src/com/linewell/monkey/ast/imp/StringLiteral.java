package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;
import com.linewell.monkey.token.TokenType;

import java.util.Objects;

/**
 * 表示字符串字面量（STRING）的抽象语法树（AST）节点。
 * <p>
 * 在 Monkey 语言或类似语言的语法分析阶段中，该节点用于封装源代码中出现的字符串常量。
 * 例如，当解析到语句：
 * <pre>
 * let s = "hello world";
 * </pre>
 * 时，字符串部分 {@code "hello world"} 会被构造成一个 {@code StringLiteral} 节点。
 * </p>
 *
 * <p>
 * 该节点实现了 {@link Expression} 接口，属于表达式类型节点。
 * 其核心属性包括：
 * <ul>
 *   <li>{@code token}：词法分析阶段识别出的字符串类型标记（通常对应 {@code TokenType.STRING}）。</li>
 *   <li>{@code value}：去除引号后的实际字符串内容。</li>
 * </ul>
 * </p>
 *
 * @see com.linewell.monkey.ast.Expression
 * @see com.linewell.monkey.token.Token
 *
 * @author axin
 */
public class StringLiteral implements Expression {

    /** 对应的字符串词法单元类型（通常为 {@code TokenType.STRING}）。 */
    private Token token;

    /** 字符串字面量的实际内容（不包含引号）。 */
    private String value;


    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public StringLiteral() {
    }

    /**
     * 使用给定的词法单元和字符串内容创建字符串字面量节点。
     *
     * @param token 对应的字符串词法单元
     * @param value 字符串的实际内容（不含引号）
     */
    public StringLiteral(Token token, String value) {
        this.token = token;
        this.value = value;
    }

    /**
     * 返回该节点的词法字面量表示。
     *
     * @return 字面量形式（通常为字符串的源代码表现，如 {@code "hello"}）
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 返回节点的字符串表示形式。
     * <p>
     * 默认返回 {@link Token#getLiteral()} 的结果。
     * </p>
     *
     * @return 该字符串字面量节点的字符串形式
     */
    @Override
    public String toString() {
        return token.getLiteral();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StringLiteral)) return false;
        StringLiteral that = (StringLiteral) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
