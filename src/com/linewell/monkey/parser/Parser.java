package com.linewell.monkey.parser;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.*;
import com.linewell.monkey.lexer.Lexer;
import com.linewell.monkey.token.Token;
import com.linewell.monkey.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * monkey 语言的语法解析器（Parser）
 * 负责将词法分析器（Lexer）输出的 Token 流解析为抽象语法树（AST）
 *
 * 使用“递归下降解析”（Recursive Descent Parsing）技术
 * 维护两个 Token：当前 Token 和向前看 Token（peek），用于预测下一步语法结构
 * <p>核心设计思想：
 * <ul>
 *     <li>通过“注册表”机制动态绑定 Token 类型与解析函数</li>
 *     <li>使用“前缀/中缀”分类处理表达式（如：-5 是前缀，3+4 是中缀）</li>
 *     <li>通过优先级控制运算符结合顺序（如：3+4*5 中 * 优先于 +）</li>
 * </ul>
 *
 */
public class Parser {

    // 优先级常量（对应 Go 的 iota）
    private static final int LOWEST = 0;        // 最低优先级
    private static final int EQUALS = 1;        // == !=
    private static final int LESSGREATER = 2;   // > <
    private static final int SUM = 3;           // + -
    private static final int PRODUCT = 4;       // * /
    private static final int PREFIX = 5;        // -x !x
    private static final int CALL = 6;          // function(x)

    // 优先级映射表（TokenType -> 优先级数值）
    private static final Map<TokenType, Integer> PRECEDENCES = new HashMap<>();

    static {
        PRECEDENCES.put(TokenType.EQ, EQUALS);
        PRECEDENCES.put(TokenType.NOT_EQ, EQUALS);
        PRECEDENCES.put(TokenType.LT, LESSGREATER);
        PRECEDENCES.put(TokenType.GT, LESSGREATER);
        PRECEDENCES.put(TokenType.PLUS, SUM);
        PRECEDENCES.put(TokenType.MINUS, SUM);
        PRECEDENCES.put(TokenType.SLASH, PRODUCT);
        PRECEDENCES.put(TokenType.MULT, PRODUCT);
    }

    // 词法分析器，提供 token 流
    private Lexer lexer;

    // 当前读取的 token
    private Token currentToken;

    // 向前看一个 token（预读）
    private Token peekToken;

    // 存储解析过程中遇到的错误
    private List<String> errors = new ArrayList<String>();

    // 前缀解析函数映射：TokenType -> 解析函数
    // 例如：IDENT → parseIdentifier(), BANG → parsePrefixExpression()
    private Map<TokenType, PrefixParseFn> prefixParseFns;

    // 中缀解析函数映射：TokenType -> 解析函数
    // 例如：PLUS → parseInfixExpression(), EQ → parseInfixExpression()
    private Map<TokenType, InfixParseFn> infixParseFns;

    /**
     * 函数式接口：前缀表达式解析函数
     * 用于解析以某个 token 开头的表达式，如：!true, -5, x
     */
    @FunctionalInterface
    public interface PrefixParseFn {
        Expression parse();
    }

    /**
     * 函数式接口：中缀表达式解析函数
     * 用于解析需要左右操作数的表达式，如：3 + 4, x == y
     */
    @FunctionalInterface
    public interface InfixParseFn {
        // 接收左操作数，返回完整表达式
        Expression parse(Expression left);
    }


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

    /**
     * 构造解析器（Parser），初始化词法单元并注册所有前缀/中缀解析函数。
     *
     * @param lexer 词法分析器
     */
    public Parser(final Lexer lexer) {
        this.lexer = lexer;
        errors = new ArrayList<String>();
        prefixParseFns = new HashMap<>();
        infixParseFns = new HashMap<>();

        // 读取两个词法单元，已设置curtoken 和 peekToken
        nextToken();
        nextToken();

        // 注册前缀解析函数
        // 标识符 x
        registerPrefix(TokenType.IDENT, this::parseIdentifier);
        // 整数 5
        registerPrefix(TokenType.INT, this::parseIntegerLiteral);
        // !true
        registerPrefix(TokenType.BANG, this::parsePrefixExpression);
        // -5
        registerPrefix(TokenType.MINUS, this::parsePrefixExpression);
        // true
        registerPrefix(TokenType.TRUE, this::parseBoolean);
        // false
        registerPrefix(TokenType.FALSE, this::parseBoolean);
        // ()
        registerPrefix(TokenType.LPAREN, this::parseGroupedExpression);
        // if
        registerPrefix(TokenType.IF, this::parseIfExpression);

        // 注册中缀解析函数
        // +
        registerInfix(TokenType.PLUS, this::parseInfixExpression);
        // -
        registerInfix(TokenType.MINUS, this::parseInfixExpression);
        // /
        registerInfix(TokenType.SLASH, this::parseInfixExpression);
        // *
        registerInfix(TokenType.MULT, this::parseInfixExpression);
        // ==
        registerInfix(TokenType.EQ, this::parseInfixExpression);
        // !=
        registerInfix(TokenType.NOT_EQ, this::parseInfixExpression);
        // <
        registerInfix(TokenType.LT, this::parseInfixExpression);
        // >
        registerInfix(TokenType.GT, this::parseInfixExpression);
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
            case RETURN:
                return parseReturnStatement();
            default:
                return parseExpressionStatement();
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

    /**
     *  解析 return 语句
     *  该方法负责解析形如以下的语句：
     *   return 5;
     *
     * @return 解析得到的 {@link ReturnStatement} 节点，永远不会为 {@code null}
     */
    public ReturnStatement  parseReturnStatement() {
        ReturnStatement stmt = new ReturnStatement(currentToken);

        nextToken();

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

    /**
     * 注册前缀解析函数
     *
     * @param tokenType 词法单元类型（如 IDENT）
     * @param fn        解析函数（如 this::parseIdentifier）
     */
    public void registerPrefix(TokenType tokenType, PrefixParseFn fn) {
        prefixParseFns.put(tokenType, fn);
    }

    /**
     *  注册中缀解析函数
     *
     * @param tokenType 词法单元类型（如 PLUS）
     * @param fn        解析函数（如 this::parseInfixExpression）
     */
    public void registerInfix(TokenType tokenType, InfixParseFn fn) {
        infixParseFns.put(tokenType, fn);
    }

    /**
     * 解析表达式语句，例如：x + 5;
     *
     * @return 表达式语句节点
     */
    public ExpressionStatement parseExpressionStatement() {
        ExpressionStatement exStmt = new ExpressionStatement();
        exStmt.setToken(currentToken);
        // 解析表达式，从最低优先级开始
        exStmt.setExpression(parseExpression(LOWEST));

        // 如果下一个是分号，消耗它
        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return exStmt;
    }

    /**
     * 解析表达式，使用优先级控制结合顺序
     *
     * @param precedence 外层调用的优先级
     * @return 解析出的表达式
     */
    public Expression parseExpression(int precedence) {
        // 1. 获取当前 token 的前缀解析器
        PrefixParseFn prefix = prefixParseFns.get(currentToken.getType());
        if (prefix == null) {
            noPrefixParseFnError(currentToken.getType());
            return null;
        }
        // 2. 调用前缀解析器，得到左操作数
        Expression leftExp = prefix.parse();

        // 3. 循环处理中缀操作符（如 +, -, ==）
        while (!peekTokenIs(TokenType.SEMICOLON) && (precedence < peekPrecedence())) {
            InfixParseFn infixParseFn = infixParseFns.get(peekToken.getType());
            if (infixParseFn == null) {
                // 没有中缀解析器，直接返回
                return leftExp;
            }
            // 移动到中缀操作符 token
            nextToken();
            // 调用中缀解析器，更新 leftExp
            leftExp = infixParseFn.parse(leftExp);
        }

        return leftExp;
    }

    /**
     * 解析标识符表达式, 如: x
     *
     * @return 标识符 Identifier 节点
     */
    public Expression parseIdentifier() {
        return new Identifier(currentToken, currentToken.getLiteral());
    }

    /**
     * 解析整数字面量表达式, 如: 5
     *
     * @return 整数字面量 IntegerLiteral  节点
     */
    public Expression parseIntegerLiteral() {
        IntegerLiteral integerLiteral = new IntegerLiteral();
        integerLiteral.setToken(currentToken);

        try{
            long value = Long.parseLong(currentToken.getLiteral());
            integerLiteral.setValue(value);
        }catch (NumberFormatException e) {
            String msg = String.format("Could not parse '%s' as integer",
                    currentToken.getLiteral());
            errors.add(msg);
            return null;
        }

        return integerLiteral;
    }

    /**
     * 当没有找到前缀解析函数时，记录错误
     *
     * @param tokenType 当前词法单元类型
     */
    public void noPrefixParseFnError(TokenType tokenType) {
        String msg = String.format("no prefix parse function for %s found", tokenType);
        errors.add(msg);
    }

    /**
     * 解析前缀表达式，如 !true, -5
     *
     * @return 前缀表达式 PrefixExpression 节点
     */
    public Expression parsePrefixExpression() {
        PrefixExpression prefixExpression = new PrefixExpression();
        prefixExpression.setToken(currentToken);
        prefixExpression.setOperator(currentToken.getLiteral());

        nextToken();

        // 递归解析右操作数
        prefixExpression.setRight(parseExpression(PREFIX));

        return prefixExpression;
    }

    /**
     * 获取下一个词法单元的优先级
     *
     * @return 优先级数值
     */
    public int peekPrecedence() {
        return PRECEDENCES.getOrDefault(peekToken.getType(), LOWEST);
    }

    /**
     * 获取当前词法单元的优先级
     *
     * @return 优先级数值
     */
    public int curPrecedence() {
        return PRECEDENCES.getOrDefault(currentToken.getType(), LOWEST);
    }

    /**
     * 解析中缀表达式，如 5 + 5, x == y
     *
     * @param left 左侧表达式
     * @return 中缀表达式节点
     */
    public Expression parseInfixExpression(Expression left) {
        InfixExpression infixExpression = new InfixExpression();
        infixExpression.setToken(currentToken);
        infixExpression.setOperator(currentToken.getLiteral());
        infixExpression.setLeft(left);

        int precedence = curPrecedence();
        nextToken();
        infixExpression.setRight(parseExpression(precedence));
        return infixExpression;
    }

    /**
     * 解析布尔字面量（true/false）。
     *
     * @return 布尔表达式节点
     */
    public Expression parseBoolean() {
        return new BooleanType(currentToken, curTokenIs(TokenType.TRUE));
    }

    /**
     * 解析括号包裹的表达式，例如 (x + y)。
     *
     * @return 表达式节点
     */
    public Expression parseGroupedExpression() {
        nextToken();

        Expression expression = parseExpression(LOWEST);

        if (!expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return expression;
    }

    /**
     * 解析 if 表达式。
     * 语法形式：
     * if (condition) consequence [else alternative]
     *
     * @return IfExpression 节点
     */
    public Expression parseIfExpression() {
        IfExpression ifExpression = new IfExpression();
        ifExpression.setToken(currentToken);

        if (!expectPeek(TokenType.LPAREN)) {
            return null;
        }

        nextToken();
        ifExpression.setCondition(parseExpression(LOWEST));

        if (!expectPeek(TokenType.RPAREN)) {
            return null;
        }

        if (!expectPeek(TokenType.LBRACE)) {
            return null;
        }

        ifExpression.setConsequence(parseBlockStatement());

        if (peekTokenIs(TokenType.ELSE)) {
            nextToken();

            if (!expectPeek(TokenType.LBRACE)) {
                return null;
            }
            ifExpression.setAlternative(parseBlockStatement());
        }

        return ifExpression;
    }

    /**
     * 解析代码块语句（BlockStatement）。
     *
     * @return BlockStatement 节点
     */
    public BlockStatement parseBlockStatement() {
        BlockStatement blockStatement = new BlockStatement();
        blockStatement.setStatements(new ArrayList<>());

        nextToken();

        while (!curTokenIs(TokenType.RBRACE) && !curTokenIs(TokenType.EOF)) {
            Statement statement = parseStatement();
            if (statement != null) {
                blockStatement.getStatements().add(statement);
            }
            nextToken();
        }

        return blockStatement;
    }



}
