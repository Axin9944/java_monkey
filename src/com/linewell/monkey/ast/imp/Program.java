package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.Statement;

import java.util.List;

public class Program implements Node {

    private final List<Statement> statements;

    public Program(List<Statement> statements) {
        this.statements = statements;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public String tokenLiteral() {
        if (statements != null && !statements.isEmpty()) {
            return statements.get(0).tokenLiteral();
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Statement stmt : statements) {
            sb.append(stmt.toString());
        }
        return sb.toString();
    }
}
