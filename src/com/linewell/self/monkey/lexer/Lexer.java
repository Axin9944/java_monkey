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

        skipWhitespace();

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
                if (isLetter(ch)) {
                    String literal = readIdentifier();
                    tok = newToken(TokenType.loopupIdent(literal),
                            literal);
                } else {
                    tok = newToken(TokenType.ILLEGAL, ch);
                }
        }

        readChar();  // 移动到下一个字符
        return tok;
    }

    private Token newToken(TokenType tokenType, char ch) {
        return new Token(tokenType, String.valueOf(ch));
    }

    private Token newToken(TokenType tokenType, String ch) {
        return new Token(tokenType, ch);
    }

    /***
     *  判断字符是否为字母
     * @param ch
     * @return
     */
    public boolean isLetter(char ch) {
        return  ('a' <= ch && ch <= 'z') ||
                ('A' <= ch && ch <= 'Z') ||
                (ch == '_');
    }

    /***
     *  读取标识符（连续的字母、数字、下划线）
     *  从当前位置开始，直到遇到非字母字符为止
     * @return 标识符字符串
     */
    public String readIdentifier() {
        // 记录起始为止
        int start = position;

        while (isLetter(ch)) {
            // 不断前移，直至遇见非字母
            readChar();
        }
        // 截取 input 中 [start, position) 的子字符串
        return input.substring(start, position);
    }

    /***
     *  跳过空白字符
     */
    public void skipWhitespace() {
        while ((ch == ' ')
                || (ch == '\t')
                || (ch == '\n')
                || (ch == '\r')) {
            readChar();
        }
    }
}
