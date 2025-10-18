package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.Statement;

import java.util.List;
import java.util.Objects;

/**
 * 抽象语法树（AST）的根节点，表示完整的程序。
 *
 * <p>在 Monkey 语言中，程序由若干条 {@link Statement} 构成。
 * Program 节点是整个语法树的顶层节点，用于保存这些语句。</p>
 *
 * <p>注意：</p>
 * <ul>
 *     <li>Program 本身不负责解析或构建语法树，AST 的构建由 {@code Parser} 完成。</li>
 *     <li>Program 的主要职责是保存语法树的顶层语句，并提供访问和序列化方法。</li>
 * </ul>
 *
 * <p>职责：</p>
 * <ul>
 *     <li>存储程序中的所有语句（List&lt;Statement&gt;）</li>
 *     <li>提供 tokenLiteral() 方法，通常返回第一条语句的 token literal</li>
 *     <li>提供 toString() 方法，将整个程序序列化为字符串表示</li>
 * </ul>
 *
 * @author axin
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Program)) return false;
        Program program = (Program) o;
        return Objects.equals(statements, program.statements);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(statements);
    }
}
