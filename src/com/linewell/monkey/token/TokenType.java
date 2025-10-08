package com.linewell.monkey.token;

import java.util.HashMap;
import java.util.Map;

/**
 * 表示 Monkey 语言中的词法单元（Token）类型枚举。
 * <p>
 * 在词法分析阶段，源代码会被拆分为一系列最小的语法单元（Token），
 * 每个 Token 对应一个 {@code TokenType}，用于指明其语义类别。
 * </p>
 *
 * <p>例如：</p>
 * <pre>
 * let x = "hello";
 * </pre>
 * 对应的词法单元序列为：
 * <pre>
 * TokenType.LET      -> "let"
 * TokenType.IDENT    -> "x"
 * TokenType.ASSIGN   -> "="
 * TokenType.STRING   -> "\"hello\""
 * TokenType.SEMICOLON -> ";"
 * </pre>
 *
 * <p>
 * 本枚举同时维护了一个关键字映射表（{@link #keywords}），
 * 用于在识别标识符时判断其是否属于语言关键字（如 {@code fn}, {@code let}, {@code return} 等）。
 * </p>
 *
 * @see com.linewell.monkey.token.Token
 *
 * @author axin
 */
public enum TokenType {

    // 特殊控制类型
    /** 非法或无法识别的字符 */
    ILLEGAL("ILLEGAL"),
    /** 文件结束标记 */
    EOF("EOF"),

    // 标识符与字面量
    /** 标识符（变量名、函数名等） */
    IDENT("IDENT"),
    /** 整数字面量 */
    INT("INT"),

    // 运算符
    /** 赋值运算符 '=' */
    ASSIGN("="),
    /** 加号运算符 '+' */
    PLUS("+"),
    /** 减号运算符 '-' */
    MINUS("-"),
    /** 乘号运算符 '*' */
    MULT("*"),
    /** 除号运算符 '/' */
    SLASH("/"),
    /** 逻辑非 '!' */
    BANG("!"),

    /** 小于运算符 '<' */
    LT("<"),
    /** 大于运算符 '>' */
    GT(">"),

    /** 等于运算符 '==' */
    EQ("=="),
    /** 不等于运算符 '!=' */
    NOT_EQ("!="),

    // 分隔符
    /** 逗号 ',' */
    COMMA(","),
    /** 分号 ';' */
    SEMICOLON(";"),

    /** 左括号 '(' */
    LPAREN("("),
    /** 右括号 ')' */
    RPAREN(")"),
    /** 左花括号 '{' */
    LBRACE("{"),
    /** 右花括号 '}' */
    RBRACE("}"),

    // 关键字
    /** 函数定义关键字 'fn' */
    FUNCTION("FUNCTION"),
    /** 变量声明关键字 'let' */
    LET("LET"),
    /** 布尔字面量 'true' */
    TRUE("TRUE"),
    /** 布尔字面量 'false' */
    FALSE("FALSE"),
    /** 条件语句关键字 'if' */
    IF("IF"),
    /** 条件语句关键字 'else' */
    ELSE("ELSE"),
    /** 字符串字面量 */
    STRING("STRING"),
    /** 返回语句关键字 'return' */
    RETURN("RETURN");

    /** 词法单元对应的字符串字面量表示 */
    private final String literal;

    TokenType(String literal) {
        this.literal = literal;
    }

    /**
     * 获取当前枚举值对应的字符串字面量。
     *
     * @return 与该 TokenType 对应的原始文本值
     */
    public String getLiteral() {
        return literal;
    }

    @Override
    public String toString() {
        return literal;
    }

    /** 关键字映射表：用于判断标识符是否为语言关键字 */
    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("fn", FUNCTION);
        keywords.put("let", LET);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("return", RETURN);
    }

    /**
     * 根据标识符文本判断其是否为关键字。
     * <p>
     * 若 {@code ident} 存在于关键字映射表中，则返回对应的关键字类型；
     * 否则返回 {@link #IDENT}，表示普通标识符。
     * </p>
     *
     * @param ident 源代码中的标识符名称
     * @return 对应的 {@link TokenType}；若非关键字，返回 {@link #IDENT}
     */
    public static TokenType loopupIdent(String ident) {
        TokenType tokenType = keywords.get(ident);
        return tokenType != null ? tokenType : IDENT;
    }
}
