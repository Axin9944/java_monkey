package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

import java.util.Objects;

/**
 * 标识符（Identifier）AST 节点。
 *
 * <p>在 monkey 语言中，标识符用于表示变量名或函数名，
 * 比如表达式中的 {@code x}、{@code foobar}。</p>
 *
 * <p>结构上，它包含：
 * <ul>
 *   <li>{@link Token} token：词法单元，本质上是一个 IDENT 类型的 token</li>
 *   <li>{@link String} value：标识符的具体名字（例如 "x" 或 "foobar"）</li>
 * </ul>
 *
 * <p>实现了 {@link Expression} 接口，因此可作为表达式使用。</p>
 */
public class Identifier implements Expression {

    // 词法单元（通常是 IDENT 类型），例如标识符 "foobar" 对应的 token
    private final Token token;

    // 标识符的实际名字，例如 "foobar"
    private final String value;

    public Identifier(Token token, String val) {
        this.token = token;
        value = val;
    }

    public Token getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    /**
     * 返回该节点的 token 文本，用于调试和打印。
     *
     * @return token 的原始字面量（literal）
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 返回该节点的字符串表示（即标识符的名字）。
     * 在 AST 打印或调试时使用。
     *
     * @return 标识符字符串
     */
    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Identifier)) return false;
        Identifier that = (Identifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
