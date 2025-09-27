package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AST 节点：函数调用表达式 (Call Expression)
 * 对应 Go 语言中的结构：
 * <pre>
 * type CallExpression struct {
 *     Token     token.Token   // '(' 词法单元
 *     Function  Expression    // 标识符或函数字面量
 *     Arguments []Expression  // 实参列表
 * }
 * </pre>
 */
public class CallExpression implements Expression {

    // '(' 词法单元
    private Token token;

    // 被调用的函数，可以是标识符或函数字面量
    private Expression function;

    // 参数列表
    private List<Expression> arguments;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Expression getFunction() {
        return function;
    }

    public void setFunction(Expression function) {
        this.function = function;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public void setArguments(List<Expression> arguments) {
        this.arguments = arguments;
    }

    public CallExpression() {
    }

    public CallExpression(Token token, Expression function, List<Expression> arguments) {
        this.token = token;
        this.function = function;
        this.arguments = arguments;
    }

    /**
     * 返回词法单元的字面值（一般是 "("）
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 构造函数调用表达式的字符串形式，例如：
     * <pre>
     * add(1, 2)
     * </pre>
    */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // 写入函数部分
        sb.append(function.toString());
        // 写入左括号
        sb.append("(");
        /*for (int i = 0; i < arguments.size(); i++) {
            sb.append(arguments.get(i).toString());
            if (i < arguments.size() - 1) {
                sb.append(", ");
            }
        }*/

        // 写入参数列表，用逗号+空格分隔
        if (arguments != null && !arguments.isEmpty()) {
            String argument = arguments.stream()
                    .map(Expression::toString)
                    .collect(Collectors.joining(", "));
            sb.append(argument);
        }

        // 写入右括号
        sb.append(")");

        return sb.toString();
    }
}
