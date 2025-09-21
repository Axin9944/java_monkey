package com.linewell.monkey.parser;

import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.Identifier;
import com.linewell.monkey.ast.imp.LetStatement;
import com.linewell.monkey.ast.imp.Program;
import com.linewell.monkey.lexer.Lexer;
import com.linewell.monkey.token.Token;
import com.linewell.monkey.token.TokenType;

import java.util.ArrayList;
import java.util.List;

/***
 * Monkey 语言的语法解析器（Parser）
 * 负责将词法分析器（Lexer）输出的 Token 流解析为抽象语法树（AST）
 *
 * 使用“递归下降解析”（Recursive Descent Parsing）技术
 * 维护两个 Token：当前 Token 和向前看 Token（peek），用于预测下一步语法结构
 */
public class Parser {

    private Lexer lexer;

    private Token currentToken;

    private Token peekToken;

    private List<String> errors = new ArrayList<String>();

    public Lexer getLexer() {
        return lexer;
    }

    public void setLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    public Token getCurrentToken() {
        return currentToken;
    }

    public void setCurrentToken(Token currentToken) {
        this.currentToken = currentToken;
    }

    public Token getPeekToken() {
        return peekToken;
    }

    public void setPeekToken(Token peekToken) {
        this.peekToken = peekToken;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addErrors(String errors) {
        this.errors.add(errors);
    }

    public Parser(final Lexer lexer) {
        this.lexer = lexer;
        errors = new ArrayList<String>();

        // 读取两个词法单元，已设置curtoken 和 peekToken
        nextToken();
        nextToken();
    }

    public void nextToken() {
        currentToken = peekToken;
        peekToken = lexer.nextToken();
    }

    /***
     * 解析整个程序
     * 程序由多个语句（Statement）组成，直到遇到 EOF（文件结束）
     *
     * @return 构建好的程序 AST 根节点
     */
    public Program parseProgram() {
        Program program = new Program(new ArrayList<>());

        while (currentToken.getType() != TokenType.EOF) {
            Statement stmt = parseStatement();
            if (stmt != null) {
                program.getStatements().add(stmt);
            }
            nextToken();
        }

        return program;
    }

    /***
     * 解析语句（Statement）
     * 根据当前 Token 的类型，分发到不同的语句解析方法
     *
     * @return 解析出的语句节点，失败返回 null
     */
    public Statement parseStatement() {
        switch (currentToken.getType()){
            // 如果当前是 let 关键字，调用 let 语句解析器
            case LET:
                return  parseLetStatement();
            default:
                return null;
        }
    }

    /***
     * 解析 let 语句
     * 语法格式：let <标识符> = <表达式>;
     * 例如：let x = 5;
     *
     * @return 解析出的 LetStatement 节点，失败返回 null
     */
    public LetStatement parseLetStatement() {
        LetStatement stmt = new LetStatement(currentToken);

        // 检查下一个 Token 是否是标识符（如变量名 x）
        if (!expectPeek(TokenType.IDENT)) {
            return null;
        }

        // 创建标识符节点，如 'x'
        Identifier identifier = new Identifier(currentToken, currentToken.getLiteral());
        stmt.setName(identifier);

        // 检查下一个 Token 是否是赋值符号 '='
        if (!expectPeek(TokenType.ASSIGN)) {
            // 缺少 '='，语法错误
            return null;
        }

        // TODO 跳过对表达式的处理，直到遇见分号
        // 目前简单处理：一直推进 Token，直到遇到分号 ';'
        while (!curTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return stmt;
    }

    /***
     * 预期下一个 Token 是指定类型
     * 如果是，则推进 Token 并返回 true
     * 如果不是，记录错误并返回 false
     *
     * @param type 期望的 Token 类型
     * @return 是否符合预期
     */
    public boolean expectPeek(TokenType type) {
        if (peekTokenIs(type)) {
            nextToken();
            return true;
        } else {
            peekError(type);
            return false;
        }
    }

    /***
     * 检查 peekToken（下一个 Token）是否是指定类型
     *
     * @param type 要检查的 Token 类型
     * @return 是否匹配
     */
    public boolean peekTokenIs(TokenType type) {
        return peekToken.getType() == type;
    }

    /***
     * 检查 currentToken（当前 Token）是否是指定类型
     *
     * @param type 要检查的 Token 类型
     * @return 是否匹配
     */
    public boolean curTokenIs(TokenType type) {
        return currentToken.getType() == type;
    }

    /***
     * 当预期 Token 失败时，记录错误信息
     *
     * @param type 期望的 Token 类型
     */
    public void peekError(TokenType type) {
        String msg = "Expected next token to be " + type + ", got " +
                peekToken.getType() + " instead";
        errors.add(msg);
    }

}
