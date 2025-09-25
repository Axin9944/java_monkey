package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.token.Token;

/**
 * 抽象语法树（AST）中的返回语句节点。
 *
 * <p>在 Monkey 语言中，返回语句的语法为：</p>
 * <pre>
 *     return &lt;expression&gt;;
 * </pre>
 *
 * <p>该类用于表示这种语句，其中：</p>
 * <ul>
 *     <li>{@link #token} 保存 "return" 关键字的 token</li>
 *     <li>{@link #returnValue} 保存返回的表达式</li>
 * </ul>
 */
public class ReturnStatement implements Statement {
    // return 关键字
    private Token token;

    // return 要返回的值
    private Expression returnValue;

    public void setToken(Token token) {
        this.token = token;
    }

    public Expression getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Expression returnValue) {
        this.returnValue = returnValue;
    }

    public ReturnStatement(Token token, Expression returnValue) {
        this.token = token;
        this.returnValue = returnValue;
    }

    public ReturnStatement() {
    }

    public ReturnStatement(Token token) {
        this.token = token;
    }


    @Override
    public Token getToken() {
        return token;
    }

    /**
     * 获取该语句的 token literal。
     *
     * <p>对于 return 语句，总是返回 "return"。</p>
     *
     * @return token 的字面量
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 将 return 语句序列化为字符串。
     *
     * <p>格式为：</p>
     * <pre>
     *     return &lt;expression&gt;;
     * </pre>
     *
     * @return 语句的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tokenLiteral()).append(" ");
        if (returnValue != null) {
            sb.append(returnValue);
        }
        sb.append(";");
        return sb.toString();
    }
}
