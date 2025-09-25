package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.token.Token;

/**
 * LetStatement AST 节点。
 *
 * <p>表示 monkey 语言中的 {@code let} 语句，例如：</p>
 * <pre>{@code
 * let x = 5;
 * let y = foo;
 * }</pre>
 *
 * <p>其结构包含：</p>
 * <ul>
 *   <li>{@link Token} token：关键字 {@code let} 对应的词法单元</li>
 *   <li>{@link Identifier} name：变量名（标识符）</li>
 *   <li>{@link Expression} expression：赋值的表达式（右值）</li>
 * </ul>
 *
 * <p>实现了 {@link Statement} 接口。</p>
 */
public class LetStatement implements Statement {
    // let 关键字对应的 token（类型为 LET）
    private Token token;

    // 变量名，例如 "x"
    private Identifier name;

    // 变量的值表达式，例如整数字面量、函数调用等
    private Expression expression;


    public LetStatement(Token token, Identifier name, Expression expression) {
        this.token = token;
        this.name = name;
        this.expression = expression;
    }

    public LetStatement() {
    }

    public LetStatement(Token token) {
        this.token = token;
    }

    @Override
    public Token getToken() {
        return token;
    }

    public Identifier getName() {
        return name;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public void setName(Identifier name) {
        this.name = name;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * 返回 token 的字面量（通常为 "let"）。
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 返回该语句的字符串形式。
     * <p>例如：{@code let x = 5;}</p>
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tokenLiteral()).append(" ");
        sb.append(name.toString());
        sb.append(" = ");
        if (expression != null) {
            sb.append(expression);
        }
        sb.append(";");
        return sb.toString();
    }
}
