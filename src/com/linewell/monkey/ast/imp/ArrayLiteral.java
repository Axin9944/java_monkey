package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 表示 Monkey 语言中的数组字面量表达式（Array Literal）。
 * <p>
 * 对应语法形式：
 * <pre>
 *     [expr1, expr2, expr3, ...]
 * </pre>
 *
 * 示例：
 * <pre>
 *     let arr = [1, 2, 3];
 * </pre>
 *
 * 该节点在 AST 中保存了：
 * <ul>
 *     <li>触发该节点的 token（通常为 "["）</li>
 *     <li>数组元素组成的表达式列表（elements）</li>
 * </ul>
 *
 * @author axin
 */
public class ArrayLiteral implements Expression {

    /** 触发该数组字面量的词法单元（通常是 '[' ） */
    private Token token;

    /** 数组中的元素表达式列表 */
    private List<Expression> elements;

    public ArrayLiteral() {
    }

    public ArrayLiteral(Token token, List<Expression> elements) {
        this.token = token;
        this.elements = elements;
    }

    public ArrayLiteral(List<Expression> elements) {
        this.elements = elements;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public List<Expression> getElements() {
        return elements;
    }

    public void setElements(List<Expression> elements) {
        this.elements = elements;
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
     * 以 Monkey 语言的语法格式返回数组的字符串表示。
     *
     * @return 数组表达式字符串，例如 "[1, 2, 3]"
     */
    @Override
    public String toString() {
        StringBuilder al = new StringBuilder();

        al.append("[");
        if (elements != null) {
            String ele = elements.stream()
                    .map(Expression::toString)
                    .collect(Collectors.joining(", "));
            al.append(ele);
        }
        al.append("]");

        return al.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArrayLiteral)) return false;
        ArrayLiteral that = (ArrayLiteral) o;
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(elements);
    }
}
