package com.linewell.monkey.lexer;

import com.linewell.monkey.token.Token;
import com.linewell.monkey.token.TokenType;

/**
 * 词法分析器（Lexer），将 Monkey 语言源码字符串拆分为 Token。
 *
 * <p>通过维护当前位置、读取位置以及当前字符，提供逐字符扫描和 Token 生成功能。
 * 支持标识符、数字、运算符、分隔符等基本元素。
 *
 * @author axin
 */
public class Lexer {

    // 输入的源码字符串
    private String input;
    // 当前处理的字符位置（上一个读取的字符位置）
    private int position;
    // 下一个要读取的字符位置
    private int readPosition;
    // 当前正在处理的字符
    private char ch;

    /**
     * 构造 Lexer 并初始化。
     *
     * @param input 要进行词法分析的源码字符串
     */
    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.readPosition = 0;
        this.ch = '\0';  // 对应 Go 的 0（null 字符）
        readChar();      // 初始化时读取第一个字符
    }

    /**
     * 读取下一个字符并更新 position 与 readPosition。
     * 如果已到输入末尾，将 ch 设为 '\0'。
     */
    public void readChar() {
        if (readPosition >= input.length()) {
            ch = '\0';  // 到末尾，设为 null 字符
        } else {
            ch = input.charAt(readPosition);  // 获取当前字符
        }
        position = readPosition;
        readPosition++;
    }

    /**
     * 获取下一个 Token。
     *
     * <p>会跳过空白字符，根据当前字符生成对应 Token。
     * 支持标识符、数字、运算符、分隔符等。
     *
     * @return 下一个词法单元 Token
     */
    public Token nextToken() {
        Token tok;

        skipWhitespace();

        switch (ch) {
            case '=':
                if (peekChar() == '=') {
                    char start = ch;
                    readChar();
                    String literal = String.valueOf(start) + ch;
                    tok = newToken(TokenType.EQ, literal);
                } else {
                    tok = newToken(TokenType.ASSIGN, ch);
                }
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
            case '-':
                tok = newToken(TokenType.MINUS, ch);
                break;
            case '*':
                tok = newToken(TokenType.MULT, ch);
                break;
            case '/':
                tok = newToken(TokenType.SLASH, ch);
                break;
            case '>':
                tok = newToken(TokenType.GT, ch);
                break;
            case '<':
                tok = newToken(TokenType.LT, ch);
                break;
            case '!':
                if (peekChar() == '=') {
                    char start = ch;
                    readChar();
                    String literal = String.valueOf(start) + ch;
                    tok = newToken(TokenType.NOT_EQ, literal);
                } else {
                    tok = newToken(TokenType.BANG, ch);
                }
                break;
            case '{':
                tok = newToken(TokenType.LBRACE, ch);
                break;
            case '}':
                tok = newToken(TokenType.RBRACE, ch);
                break;
            case '"':
                tok = newToken(TokenType.STRING, readString());
                break;
            case '[':
                tok = newToken(TokenType.LBRACKET, ch);
                break;
            case ']':
                tok = newToken(TokenType.RBRACKET, ch);
                break;
            case ':':
                tok = newToken(TokenType.COLON, ch);
                break;
            case '\0':  // 对应 Go 的 0
                tok = new Token(TokenType.EOF, "");
                break;
            default:
                if (isLetter(ch)) {
                    String literal = readIdentifier();
                    tok = newToken(TokenType.loopupIdent(literal),
                            literal);
                    return tok;
                } else if(isDigit(ch)) {
                    tok = newToken(TokenType.INT, readNumber());
                    return tok;
                } else {
                    tok = newToken(TokenType.ILLEGAL, ch);
                }
        }

        readChar();  // 移动到下一个字符
        return tok;
    }

    /**
     * 创建一个新的 Token（字符版本）。
     *
     * @param tokenType Token 类型
     * @param ch        单个字符
     * @return Token 对象
     */
    private Token newToken(TokenType tokenType, char ch) {
        return new Token(tokenType, String.valueOf(ch));
    }

    /**
     * 创建一个新的 Token（字符串版本）。
     *
     * @param tokenType Token 类型
     * @param ch        字符串字面量
     * @return Token 对象
     */
    private Token newToken(TokenType tokenType, String ch) {
        return new Token(tokenType, ch);
    }

    /**
     * 判断字符是否为字母（包括下划线）。
     *
     * @param ch 待判断字符
     * @return true 表示是字母或下划线
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

    /**
     * 跳过空白字符（空格、制表符、换行、回车）
     * */
    public void skipWhitespace() {
        while ((ch == ' ')
                || (ch == '\t')
                || (ch == '\n')
                || (ch == '\r')) {
            readChar();
        }
    }

    /***
     *  判断输入的字符是否为数字
     * @param ch 要进行判断的字符
     * @return true 表示输入的字符为数字，false 为不是
     */
    public boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    /***
     * 读取连续的数字字符，直至遇见非数字字符为止
     * @return 数字字符串，如 "123"
     */
    public String readNumber() {
        int start = position;
        while (isDigit(ch)) {
            readChar();
        }
        return input.substring(start, position);
    }

    /***
     * 查看输入中的下一个字符（不移动读取位置）
     * @return  返回下一个字符，如果已到末尾则返回 '\0'
     */
    public char peekChar() {
        if (readPosition >= input.length()) {
            return '\0';
        }
        return input.charAt(readPosition);
    }

    /**
     * 读取并返回源代码中的字符串常量（STRING 词法单元）。
     * <p>
     * 该方法假定当前读取位置（{@code position}）指向字符串起始引号（`"`）之前的一个字符，
     * 会在内部移动读取指针，逐字符读取直到遇到下一个引号（`"`) 或文件结尾符（`\0`）。
     * 最终返回引号之间的字符串内容。
     * </p>
     *
     * <p><b>注意：</b> 该方法不会处理转义字符（例如 `\"`），
     * 若字符串中包含此类转义序列，需要在调用前或之后额外处理。</p>
     *
     * @return 字符串字面量的内容（不包含首尾引号）；若未找到结束引号，则返回至文件末尾的内容。
     */
    public String readString() {
        int pos = position + 1;

        while (true) {
            readChar();
            if ((ch == '"') || ch == '\0') {
                break;
            }
        }

        return input.substring(pos, position);
    }
}
