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

/**
 * Monkey 语言的语法解析器（Parser）。
 * <p>
 * 本解析器负责将 {@link com.linewell.monkey.lexer.Lexer} 输出的词法单元（{@link com.linewell.monkey.token.Token} 流）
 * 转换为对应的抽象语法树（AST, Abstract Syntax Tree）。
 * </p>
 *
 * <h2>核心职责：</h2>
 * <ul>
 *     <li>读取并管理词法分析器产生的 Token 流</li>
 *     <li>根据 Monkey 语言的语法规则构建表达式（Expression）与语句（Statement）节点</li>
 *     <li>为解释器或编译器阶段提供语法结构化的中间表示</li>
 * </ul>
 *
 * <h2>实现原理：</h2>
 * <p>
 * 该解析器采用了 “递归下降解析（Recursive Descent Parsing）” 技术，
 * 并引入了 “Pratt Parser” 机制（通过前缀 / 中缀函数注册系统动态控制优先级）。
 * </p>
 *
 * <h3>解析机制：</h3>
 * <ul>
 *     <li>维护两个指针：
 *         <ul>
 *             <li>{@code currentToken}：当前正在处理的 Token</li>
 *             <li>{@code peekToken}：向前看的下一个 Token，用于预测语法结构</li>
 *         </ul>
 *     </li>
 *     <li>通过注册表机制将 Token 类型映射到对应的解析函数：</li>
 *     <ul>
 *         <li><b>前缀解析函数（PrefixParseFn）</b>：用于解析以当前 token 开头的表达式，例如：
 *             <ul>
 *                 <li>{@code -5}</li>
 *                 <li>{@code !true}</li>
 *                 <li>{@code (1 + 2)}</li>
 *                 <li>{@code [1, 2, 3]}</li>
 *             </ul>
 *         </li>
 *         <li><b>中缀解析函数（InfixParseFn）</b>：用于解析需要左右操作数的表达式，例如：
 *             <ul>
 *                 <li>{@code 3 + 4}</li>
 *                 <li>{@code x == y}</li>
 *                 <li>{@code myArray[0]}</li>
 *             </ul>
 *         </li>
 *     </ul>
 *     <li>通过 {@link #PRECEDENCES} 表定义操作符优先级，实现正确的表达式结合顺序，例如：
 *         <pre>
 *             3 + 4 * 5  →  等价于  3 + (4 * 5)
 *         </pre>
 *     </li>
 * </ul>
 *
 * <h3>支持的语法结构：</h3>
 * <ul>
 *     <li><b>语句（Statement）：</b>
 *         <ul>
 *             <li>{@code let} 声明语句，例如：<code>let x = 5;</code></li>
 *             <li>{@code return} 返回语句，例如：<code>return x + y;</code></li>
 *             <li>表达式语句，例如：<code>x + 1;</code></li>
 *         </ul>
 *     </li>
 *     <li><b>表达式（Expression）：</b>
 *         <ul>
 *             <li>标识符（{@code x}）</li>
 *             <li>整数字面量（{@code 5}）</li>
 *             <li>布尔值（{@code true}, {@code false}）</li>
 *             <li>前缀表达式（{@code -x}, {@code !flag}）</li>
 *             <li>中缀表达式（{@code a + b}, {@code x == y}）</li>
 *             <li>分组表达式（{@code (x + y)}）</li>
 *             <li>条件表达式（{@code if (x < y) { ... } else { ... }})</li>
 *             <li>函数字面量（{@code fn(x, y) { return x + y; }})</li>
 *             <li>函数调用（{@code add(1, 2, 3)})</li>
 *             <li>字符串字面量（{@code "hello world"}）</li>
 *             <li>数组字面量（{@code [1, 2, 3]}）✅</li>
 *             <li>数组索引（{@code arr[0]}）✅</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <h3>新增特性：</h3>
 * <ul>
 *     <li>支持数组字面量解析（{@link #parseArrayLiteral()}）</li>
 *     <li>支持数组索引表达式解析（{@link #parseIndexExpression(Expression)}）</li>
 *     <li>在优先级映射 {@link #PRECEDENCES} 中引入 {@code INDEX} 层级，以保证 <code>array[index]</code> 的正确结合顺序</li>
 * </ul>
 *
 * <h3>示例：</h3>
 * <pre>
 * 输入：
 *   let arr = [1, 2, 3];
 *   arr[0];
 *
 * 输出 AST：
 *   Program
 *     ├── LetStatement(name="arr")
 *     │     └── ArrayLiteral(elements=[1, 2, 3])
 *     └── ExpressionStatement
 *           └── IndexExpression
 *                 ├── left: Identifier("arr")
 *                 └── index: IntegerLiteral(0)
 * </pre>
 *
 * <h3>主要扩展方法：</h3>
 * <ul>
 *     <li>{@link #parseExpressionList(TokenType)}：解析以逗号分隔的表达式序列</li>
 *     <li>{@link #parseArrayLiteral()}：解析数组字面量</li>
 *     <li>{@link #parseIndexExpression(Expression)}：解析数组索引表达式</li>
 * </ul>
 *
 * <p><b>设计模式：</b></p>
 * <ul>
 *     <li>使用函数式接口（Java 8 Lambda）模拟 Pratt Parser 的前缀/中缀回调注册机制</li>
 *     <li>使用优先级表控制表达式结合顺序</li>
 *     <li>通过错误列表（{@link #errors}）集中收集所有语法错误信息</li>
 * </ul>
 *
 * <p><b>示例用法：</b></p>
 * <pre>{@code
 * Lexer lexer = new Lexer("let arr = [1, 2, 3]; arr[0];");
 * Parser parser = new Parser(lexer);
 * Program program = parser.parseProgram();
 * System.out.println(program);
 * }</pre>
 *
 * @see com.linewell.monkey.lexer.Lexer
 * @see com.linewell.monkey.token.Token
 * @see com.linewell.monkey.ast.Expression
 * @see com.linewell.monkey.ast.Statement
 * @see com.linewell.monkey.ast.imp.ArrayLiteral
 * @see com.linewell.monkey.ast.imp.IndexExpression
 *
 * @author
 *   <a href="https://github.com/Axin9944">axin</a>（基于 Monkey 语言解释器扩展实现）
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
    private static final int INDEX = 7;         // array[index]

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
        PRECEDENCES.put(TokenType.LPAREN, CALL);
        PRECEDENCES.put(TokenType.LBRACKET, INDEX);
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
        // fun
        registerPrefix(TokenType.FUNCTION, this::parseFunctionLiteral);
        // String
        registerPrefix(TokenType.STRING, this::parseStringLiteral);
        // [
        registerPrefix(TokenType.LBRACKET, this::parseArrayLiteral);

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
        // (
        registerInfix(TokenType.LPAREN, this::parseCallExpression);
        // [
        registerInfix(TokenType.LBRACKET, this::parseIndexExpression);
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


        nextToken();

        stmt.setExpression(parseExpression(LOWEST));

        if (peekTokenIs(TokenType.SEMICOLON)) {
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

        stmt.setReturnValue(parseExpression(LOWEST));

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        /*while (!curTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }*/

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

    /**
     * 解析函数字面量 (FunctionLiteral)，例如：
     * <pre>
     * fn(x, y) { return x + y; }
     * </pre>
     *
     * @return FunctionLiteral 节点，若语法错误则返回 null
     */
    public Expression parseFunctionLiteral() {
        FunctionLiteral lit = new FunctionLiteral();
        // 记录 "fn" 的 token
        lit.setToken(currentToken);

        // 检查是否有左括号 "("
        if (!expectPeek(TokenType.LPAREN)) {
            return null;
        }

        // 解析参数列表
        lit.setParameters(parseFunctionParameters());

        // 检查是否有左大括号 "{"
        if (!expectPeek(TokenType.LBRACE)) {
            return null;
        }

        // 解析函数体
        lit.setBody(parseBlockStatement());

        return lit;
    }

    /**
     * 解析函数参数列表，例如：
     * <pre>
     * (x, y, z)
     * </pre>
     *
     * @return 参数标识符列表，若语法错误则返回 null
     */
    public List<Identifier> parseFunctionParameters() {
        List<Identifier> identifiers = new ArrayList<>();

        // 如果遇到右括号 ")"，表示无参数
        if (peekTokenIs(TokenType.RPAREN)) {
            nextToken();
            return identifiers;
        }

        // 读取第一个参数
        nextToken();
        Identifier ident = new Identifier(currentToken, currentToken.getLiteral());
        identifiers.add(ident);

        // 处理多个参数，用逗号分隔
        while(peekTokenIs(TokenType.COMMA)) {
            // 跳过逗号
            nextToken();
            // 移动到下一个参数
            nextToken();
            Identifier idt = new Identifier(currentToken, currentToken.getLiteral());
            identifiers.add(idt);
        }

        // 检查是否有右括号 ")"
        if (!expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return identifiers;
    }

    /**
     * 解析函数调用表达式，例如：
     * <pre>
     * add(1, 2, 3)
     * </pre>
     *
     * @param function 被调用的函数表达式
     * @return CallExpression 节点
     */
    public Expression parseCallExpression(Expression function) {
        CallExpression callExpression = new CallExpression();
        // 设置被调用的函数
        callExpression.setFunction(function);
        // 设置当前 token，即 "("
        callExpression.setToken(currentToken);
        // 解析实参列表
//        callExpression.setArguments(parseCallArguments());
        callExpression.setArguments(parseExpressionList(TokenType.RPAREN));
        return callExpression;
    }

    /**
     * 解析函数调用的参数列表，例如：
     * <pre>
     * (x, y + 1, foo())
     * </pre>
     *
     * @return 参数表达式列表，若语法错误则返回 null
     */
    public List<Expression> parseCallArguments() {
        List<Expression> args = new ArrayList<>();

        // 如果遇到右括号 ")"，表示没有参数
        if (peekTokenIs(TokenType.RPAREN)) {
            // 跳过 ")"
            nextToken();
            return args;
        }

        // 解析第一个参数
        nextToken();
        args.add(parseExpression(LOWEST));

        // 解析后续用逗号分隔的参数
        while(peekTokenIs(TokenType.COMMA)) {
            // 跳过逗号
            nextToken();
            // 移动到下一个参数
            nextToken();
            args.add(parseExpression(LOWEST));
        }

        // 检查是否有右括号 ")"，若没有则返回 null
        if (!expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return args;
    }

    /**
     * 解析字符串字面量（STRING）表达式节点。
     * <p>
     * 当语法分析器在读取到一个字符串类型的词法单元（通常对应 {@code TokenType.STRING}）时，
     * 调用该方法以构建相应的抽象语法树（AST）节点。
     * </p>
     *
     * <p>
     * 该方法不会尝试进一步解析复杂表达式，而是直接将当前词法单元封装为一个
     * {@link com.linewell.monkey.ast.imp.StringLiteral} 对象，并返回。
     * </p>
     *
     * <p><b>示例：</b></p>
     * <pre>
     * 输入词法单元序列：
     *   Token(type=STRING, literal="hello world")
     *
     * 调用结果：
     *   new StringLiteral(TokenType.STRING, "hello world")
     * </pre>
     *
     * @return 表示字符串字面量的 {@link com.linewell.monkey.ast.Expression} 节点实例
     */
    private Expression parseStringLiteral() {
        return new StringLiteral(currentToken.getType(), currentToken.getLiteral());
    }

    /**
     * 解析一组以逗号分隔的表达式列表（expression list），例如：
     * <pre>
     *     [1, 2, 3]
     *     foo(1, 2, bar)
     * </pre>
     *
     * <p>该方法常用于解析：
     * <ul>
     *     <li>数组字面量中的元素（配合 {@link #parseArrayLiteral()}）</li>
     *     <li>函数调用参数列表</li>
     * </ul>
     *
     * <p>解析规则：
     * <ol>
     *     <li>若下一个 token 是 <code>end</code>（例如 <code>]</code> 或 <code>)</code>），
     *         则表示空列表，直接返回空集合。</li>
     *     <li>否则，解析第一个表达式。</li>
     *     <li>若后续有逗号（<code>,</code>），则持续解析下一个表达式并加入列表。</li>
     *     <li>最后期待遇到 <code>end</code> 结束（若没有则返回 null 表示语法错误）。</li>
     * </ol>
     *
     * @param end 列表结束的 token 类型（如 {@code TokenType.RBRACKET} 或 {@code TokenType.RPAREN}）
     * @return 解析得到的表达式列表；若语法错误则返回 {@code null}
     */
    private List<Expression> parseExpressionList(TokenType end) {
        List<Expression> expressions = new ArrayList<>();

        if (peekTokenIs(end)) {
            nextToken();
            return expressions;
        }

        nextToken();

        expressions.add(parseExpression(LOWEST));

        while(peekTokenIs(TokenType.COMMA)) {
            nextToken();
            nextToken();
            expressions.add(parseExpression(LOWEST));
        }

        if(!expectPeek(end)) {
            return null;
        }

        return expressions;
    }

    /**
     * 解析数组索引表达式（Index Expression）。
     * <p>
     * 对应 Monkey 语法：
     * <pre>
     *     array[index]
     * </pre>
     *
     * 示例：
     * <pre>
     *     myArray[0]
     * </pre>
     *
     * 解析流程：
     * <ol>
     *     <li>当前 token 为左方括号（<code>[</code>）。</li>
     *     <li>调用 {@link #nextToken()} 进入索引表达式部分。</li>
     *     <li>解析索引表达式（调用 {@link #parseExpression(int)}）。</li>
     *     <li>期望下一个 token 为右方括号（<code>]</code>）。</li>
     * </ol>
     *
     * 若匹配失败（例如缺少右括号），返回 {@code null} 表示语法错误。
     *
     * @param left 被索引的表达式（通常为 {@code ArrayLiteral} 或标识符）
     * @return 解析得到的 {@link IndexExpression} 节点，或 {@code null}（语法错误）
     */
    private Expression parseIndexExpression(Expression left) {
        IndexExpression indexExpression = new IndexExpression();
        indexExpression.setLeft(left);
        indexExpression.setToken(currentToken);

        nextToken();
        indexExpression.setIndex(parseExpression(LOWEST));

        if (!expectPeek(TokenType.RBRACKET)) {
            return null;
        }

        return indexExpression;
    }

    /**
     * 解析数组字面量表达式（Array Literal）。
     * <p>
     * 对应 Monkey 语法：
     * <pre>
     *     [expr1, expr2, expr3, ...]
     * </pre>
     *
     * 示例：
     * <pre>
     *     let arr = [1, 2, 3];
     * </pre>
     *
     * 解析流程：
     * <ol>
     *     <li>当前 token 为左方括号（<code>[</code>）。</li>
     *     <li>调用 {@link #parseExpressionList(TokenType)} 解析元素列表，直到遇到右方括号（<code>]</code>）。</li>
     *     <li>将得到的表达式列表设置到 {@link ArrayLiteral} 节点中。</li>
     * </ol>
     *
     * @return 构建好的 {@link ArrayLiteral} 抽象语法树节点
     */
    private Expression parseArrayLiteral() {
        ArrayLiteral arrayLiteral = new ArrayLiteral();
        arrayLiteral.setToken(currentToken);

        arrayLiteral.setElements(parseExpressionList(TokenType.RBRACKET));

        return arrayLiteral;
    }
}
