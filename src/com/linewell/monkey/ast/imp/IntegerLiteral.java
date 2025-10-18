package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

import java.util.Objects;

/**
 * 表示一个整数类型的字面量表达式节点。
 * 例如：
 * 5;
 * 100;
 * -10;
 *
 * <p>该节点只存储整数值和对应的词法单元（Token），不包含运算逻辑。
 *
 */
public class IntegerLiteral implements Expression {

    /**
     * 该整数字面量对应的词法单元（Token）。
     * 包含类型（如 INT）和原始文本（如 "123"）
     */
    private Token token;

    /**
     * 整数字面量的实际值，使用 long 类型以匹配 Go 版本的 int64
     */
    private long value;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public IntegerLiteral() {
    }

    /**
     * 返回该节点第一个词法单元的字面量（文本）
     * 通常就是整数的字符串表示
     *
     * @return 词法单元的字面量，若 token 为 null 则返回空字符串
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 返回该整数字面量的字符串表示，用于调试和 AST 打印
     * 格式为原始词法文本（如 "123"）
     *
     * @return 整数的字符串形式
     */
    @Override
    public String toString() {
        return token.getLiteral();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IntegerLiteral)) return false;
        IntegerLiteral that = (IntegerLiteral) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
