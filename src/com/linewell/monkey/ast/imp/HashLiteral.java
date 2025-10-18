package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

import java.util.Map;
import java.util.Objects;

/**
 * 表示 Monkey 语言中的哈希字面量（Hash Literal）语法节点。
 * <p>
 * 语法结构示例：
 * <pre>
 * {
 *     "name": "Monkey",
 *     "age": 3,
 *     "isCool": true
 * }
 * </pre>
 *
 * <p>该节点在抽象语法树（AST）中用于保存哈希表达式的键值对映射关系，
 * 其中每个键和值本身也都是 {@link Expression} 类型的子节点。
 *
 * <p>示例：
 * <ul>
 *   <li>源代码：<code>{"one": 1, "two": 2}</code></li>
 *   <li>解析结果：<code>HashLiteral</code> 节点，其中 pairs 记录了两个键值对表达式。</li>
 * </ul>
 *
 * @author axin
 * @see com.linewell.monkey.ast.Expression
 * @see com.linewell.monkey.token.Token
 */
public class HashLiteral implements Expression {

    /** 当前哈希字面量的起始词法单元（通常为 '{'）。 */
    private Token token;

    /**
     * 哈希表的键值对映射。
     * <p>键和值均为 {@link Expression} 类型，以支持任意可求值表达式作为键和值。
     */
    private Map<Expression, Expression> pairs;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Map<Expression, Expression> getPairs() {
        return pairs;
    }

    public void setPairs(Map<Expression, Expression> pairs) {
        this.pairs = pairs;
    }

    public HashLiteral() {
    }

    /**
     * 使用起始 Token 初始化哈希字面量节点。
     *
     * @param token 表示哈希表达式起始位置的词法单元
     */
    public HashLiteral(Token token) {
        this.token = token;
    }

    /**
     * 返回与该表达式关联的词法单元字面值。
     * <p>通常用于调试或 AST 可视化。
     *
     * @return 当前节点起始 Token 的字面量
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 将哈希字面量转换为 Monkey 语言源代码的字符串形式。
     * <p>例如：
     * <pre>
     * {"one": 1, "two": 2}
     * </pre>
     *
     * @return 哈希字面量的可读字符串形式
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb .append("{");
        if (pairs != null) {
            int len = pairs.size();
            int count = 0;
            for (Map.Entry<Expression, Expression> entry : pairs.entrySet()) {
                sb.append(entry.getKey().toString()).append(" : ")
                        .append(entry.getValue().toString());
                if (count < len) {
                    sb.append(", ");
                    count++;
                }
            }
        }
        sb.append("}");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HashLiteral)) return false;
        HashLiteral that = (HashLiteral) o;
        return Objects.equals(pairs, that.pairs);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pairs);
    }
}
