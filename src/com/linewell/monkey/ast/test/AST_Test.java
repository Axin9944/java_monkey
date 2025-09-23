package com.linewell.monkey.ast.test;

import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.Identifier;
import com.linewell.monkey.ast.imp.LetStatement;
import com.linewell.monkey.ast.imp.Program;
import com.linewell.monkey.token.Token;
import com.linewell.monkey.token.TokenType;

import java.util.Arrays;
import java.util.List;

public class AST_Test {

    public static void main(String[] args) {
        testString();
    }

    public static void testString() {
        Token letToken = new Token(TokenType.LET, "let");
        Token varToken = new Token(TokenType.IDENT, "myVar");
        Token varToken2 = new Token(TokenType.IDENT, "anotherVar");
        Identifier name = new Identifier(varToken, "myVar");
        Identifier expression = new Identifier(varToken2, "anotherVar");
        LetStatement letStatement = new LetStatement(letToken, name, expression);
        List<Statement> stmtList = Arrays.asList(letStatement);
        Program program = new Program(stmtList);

        System.out.println(program);
        if (!program.toString().equals("let myVar = anotherVar;")) {
            System.err.println("program.String() wrong. got=" + program.toString());
        }
    }
}
