package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.token.Token;

import java.util.List;

/**
 * AST 节点：代码块语句（BlockStatement）。
 *
 * <p>代码块语句由一组 {@link Statement} 组成，
 * 通常出现在函数体、条件分支、循环体等场景中。</p>
 *
 * <p>核心职责：</p>
 * <ul>
 *     <li>保存起始的 {@link Token}（通常是 "{"）</li>
 *     <li>保存代码块内部的语句列表</li>
 *     <li>实现 {@link #toString()} 方法，将所有子语句拼接为源码字符串</li>
 * </ul>
 */
public class BlockStatement implements Statement {

    // 代码块起始的词法单元（通常是 "{"）
    private Token token;

    // 代码块中的语句列表
    private List<Statement> statements;

    public void setToken(Token token) {
        this.token = token;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    public BlockStatement() {
    }

    public BlockStatement(Token token, List<Statement> statements) {
        this.token = token;
        this.statements = statements;
    }

    /**
     * 返回代码块的起始 Token。
     * 例如在 "{ let x = 1; }" 中返回的是 "{"。
     */
    @Override
    public Token getToken() {
        return token;
    }

    /**
     * 返回该节点的 Token 字面量。
     * 一般与 getToken().getLiteral() 等价。
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 将代码块序列化为字符串。
     * 遍历所有子语句，拼接成源码形式。
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // 逐个拼接子语句的字符串表示
        for (Statement statement : statements) {
            sb.append(statement.toString());
        }

        return sb.toString();
    }
}
