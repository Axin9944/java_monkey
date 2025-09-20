package com.linewell.monkey.parser;

import com.linewell.monkey.ast.imp.Program;
import com.linewell.monkey.lexer.Lexer;
import com.linewell.monkey.token.Token;

public class Parser {

    private Lexer lexer;

    private Token currentToken;

    private Token peekToken;

    public Parser(final Lexer lexer) {
        this.lexer = lexer;

        // 读取两个词法单元，已设置curtoken 和 peekToken
        nextToken();
        nextToken();
    }

    public void nextToken() {
        currentToken = peekToken;
        peekToken = lexer.nextToken();
    }

    public Program ParseProgram() {
        return null;
    }
}
