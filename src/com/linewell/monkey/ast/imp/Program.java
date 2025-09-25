package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.Statement;

import java.util.List;

/**
 * 抽象语法树（AST）的根节点，表示完整的程序。
 *
 * <p>在 monkey 语言中，程序由若干条 {@link Statement} 构成，
 * 因此 Program 节点是所有语法树的入口点。</p>
 *
 * <p>职责：</p>
 * <ul>
 *     <li>保存程序中的所有语句</li>
 *     <li>提供 tokenLiteral() 方法（通常返回第一条语句的 token 文字）</li>
 *     <li>提供 toString() 方法，将整个程序序列化为字符串</li>
 * </ul>
 */
public class Program implements Node {

    // 程序中包含的语句列表
    private List<Statement> statements;

    public Program(List<Statement> statements) {
        this.statements = statements;
    }

    public Program() {
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    /**
     * 获取程序的 token literal。
     *
     * <p>通常返回第一条语句的 token literal；如果没有语句，则返回空字符串。</p>
     *
     * @return token literal 或空字符串
     */
    @Override
    public String tokenLiteral() {
        if (statements != null && !statements.isEmpty()) {
            return statements.get(0).tokenLiteral();
        } else {
            return "";
        }
    }

    /**
     * 将整个 Program 序列化为字符串。
     *
     * <p>通过调用每条语句的 toString() 方法，并将它们依次拼接起来。</p>
     *
     * @return 程序的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Statement stmt : statements) {
            sb.append(stmt.toString());
        }
        return sb.toString();
    }

}
