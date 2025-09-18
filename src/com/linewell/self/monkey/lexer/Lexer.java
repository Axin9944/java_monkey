package com.linewell.self.monkey.lexer;

import com.linewell.self.monkey.token.Token;
import com.linewell.self.monkey.token.TokenType;

public class Lexer {

    private String input;
    private int position;
    private int readPosition;
    private char ch;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.readPosition = 0;
        this.ch = '\0';  // 对应 Go 的 0（null 字符）
        readChar();      // 初始化时读取第一个字符
    }

    public void readChar() {
        if (readPosition >= input.length()) {
            ch = '\0';  // 到末尾，设为 null 字符
        } else {
            ch = input.charAt(readPosition);  // 获取当前字符
        }
        position = readPosition;
        readPosition++;
    }

    public Token nextToken() {
        Token tok;

        switch (ch) {
            case '=':
                tok = newToken(TokenType.ASSIGN, ch);
                break;
            case ';':
                tok = newToken(TokenType.SEMICOLON, ch);
                break;
            case '(':
                tok = newToken(TokenType.LPAREN, ch);
                break;
            case ')':
                tok = newToken(TokenType.RPAREN, ch);
                break;
            case ',':
                tok = newToken(TokenType.COMMA, ch);
                break;
            case '+':
                tok = newToken(TokenType.PLUS, ch);
                break;
            case '{':
                tok = newToken(TokenType.LBRACE, ch);
                break;
            case '}':
                tok = newToken(TokenType.RBRACE, ch);
                break;
            case '\0':  // 对应 Go 的 0
                tok = new Token(TokenType.EOF, "");
                break;
            default:
                // 目前没有处理非法字符，可以扩展
                tok = new Token(TokenType.ILLEGAL, String.valueOf(ch));
                break;
        }

        readChar();  // 移动到下一个字符
        return tok;
    }

    private Token newToken(TokenType tokenType, char ch) {
        return new Token(tokenType, String.valueOf(ch));
    }

}
