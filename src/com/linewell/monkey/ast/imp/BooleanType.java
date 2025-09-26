package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

/**
 * AST 节点：布尔类型表达式。
 *
 * <p>用于表示布尔字面量（true 或 false），
 * 常见于条件语句（if、while）等逻辑运算场景。</p>
 *
 * <p>核心职责：</p>
 * <ul>
 *     <li>保存布尔字面量对应的 Token 对象</li>
 *     <li>保存布尔值（true / false）</li>
 *     <li>提供 tokenLiteral() 和 toString() 方法，用于序列化和调试输出</li>
 * </ul>
 */
public class BooleanType implements Expression {

    // 布尔字面量对应的词法单元，例如 "true" 或 "false"
    private Token token;

    // 实际布尔值（true / false）
    private boolean value;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public BooleanType() {
    }

    public BooleanType(Token token, boolean value) {
        this.token = token;
        this.value = value;
    }

    /**
     * 返回当前节点的词法字面量（token 的原始字符串），
     * 用于调试或序列化。
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 将布尔表达式序列化为字符串。
     * 通常返回 "true" 或 "false"。
     */
    @Override
    public String toString() {
        return token.getLiteral();
    }
}
