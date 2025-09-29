package com.linewell.monkey.parser;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.*;
import com.linewell.monkey.lexer.Lexer;
import com.linewell.monkey.lexer.Lexer_Test;

import java.util.Arrays;
import java.util.List;

public class Parser_Test {

    public static void main(String[] args) {
        System.out.println("Running Parser Tests...\n");
        testLetStatements();
        testReturnStatements();
        testIdentifierExpressions();
        testIntegerLiteralExpression();
        testParsingPrefixExpressions();
        testParsingInfixExpressions();
        testOperatorPrecedenceParsing();
        testIfExpression();
        testIfElseExpression();
        testFunctionLiteralExpression();
        testCallExpressionParsing();
    }

    /**
     * 测试 let 语句的解析（包括错误处理）。
     *
     * 对应 Go 代码中的 TestLetStatements。
     * 测试内容：
     * 1. let 语句是否能被正确解析为 AST 节点；
     * 2. 标识符是否正确；
     * 3. 赋值的表达式是否正确。
     */
    public static void testLetStatements() {
        /*String input = "\"" +
                "let x = 5;" +
                "let = 10;" +
                "let foobar 838383;" +
                "\"";*/
        // 构造测试用例，每个用例包含：源码字符串、预期的标识符、预期的值
        List<LetTestCase> testCases = Arrays.asList(
                new LetTestCase("let x = 5;", "x", 5),
                new LetTestCase("let y = true;", "y", true),
                new LetTestCase("let foobar = y;", "foobar", "y"));

        for (LetTestCase testCase : testCases) {
            // 1. 词法分析
            Lexer lexer = new Lexer(testCase.input);
            // 2. 语法分析
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            // 3. 检查解析器是否有错误
            checkParserErrors(parser);

            if (program.getStatements().size() != 1) {
                System.err.println("program.Statements does not contain 1 statement. got=" +
                        program.getStatements().size());
            }

            Statement statement = program.getStatements().get(0);

            if (!testLetStatement(statement, testCase.expectedIdentifier)) {
                return;
            }

            LetStatement letStatement = (LetStatement) statement;
            if (!testLiteralExpression(letStatement.getExpression(), testCase.expectedValue)) {
                return;
            }

            /*// 解析整个程序
            Program program = parser.parseProgram();

            // 检查解析器是否有错误
            checkParserErrors(parser);

            if (program == null) {
                System.err.println("FAILED: ParseProgram() returned null");
                return;
            }

            List<Statement> statements = program.getStatements();
            if (statements.size() != 3) {
                System.err.println("FAILED: program.Statements should have 3 statements, but got " + statements.size());
                return;
            } else {
                System.out.println("PASSED: program has 3 statements");
            }

            // 预期的变量名
            String[] expectedIdentifiers = {"x", "y", "foobar"};

            boolean allPassed = true;
            for (int i = 0; i < expectedIdentifiers.length; i++) {
                Statement stmt = statements.get(i);
                if (!testLetStatement(stmt, expectedIdentifiers[i])) {
                    System.err.println("FAILED: testLetStatement failed for expected identifier: " + expectedIdentifiers[i]);
                    allPassed = false;
                }
            }

            if (allPassed) {
                System.out.println("✅ ALL TESTS PASSED");
            } else {
                System.out.println("❌ SOME TESTS FAILED");
            }*/

        }
    }

    /**
     * 测试解析器对 return 语句的处理是否正确。
     * <p>
     * 本测试会针对几种不同形式的 return 语句（返回整数、布尔值、标识符），
     * 检查：
     * <ul>
     *   <li>是否被正确解析为 {@link ReturnStatement}</li>
     *   <li>tokenLiteral 是否为 "return"</li>
     *   <li>返回值表达式是否与预期一致</li>
     * </ul>
     */
    public static void testReturnStatements(){

        List<ReturnTestCase> testCases = Arrays.asList(
                new ReturnTestCase("return 5;", 5),
                new ReturnTestCase("return true;", true),
                new ReturnTestCase("return foobar;", "foobar")
        );

        for (ReturnTestCase testCase : testCases) {
            Lexer lexer = new Lexer(testCase.input);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            List<Statement> statements = program.getStatements();

            if (statements.size() != 1) {
                System.err.println("program.Statements does not contain 1 statements. got=" +
                        statements.size());
                return;
            }

            Statement statement = statements.get(0);

            // 类型检查
            if (!(statement instanceof ReturnStatement)) {
                System.err.println("stmt not ReturnStatement. got=" +
                        statement.getClass().getSimpleName());
                return;
            }

            ReturnStatement returnStmt = (ReturnStatement) statement;

            if (!returnStmt.tokenLiteral().equals("return")) {
                System.err.println("returnStmt.TokenLiteral not 'return'. got= " +
                        returnStmt.tokenLiteral());
            }

            if (testLiteralExpression(returnStmt.getReturnValue(), testCase.expectedValue)) {
                return;
            }
        }

        /*String input = "return 5;\n" +
                "return 10;\n" +
                "return 993 322;\n";

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);

        Program program = parser.parseProgram();
        checkParserErrors(parser);

        if (program.getStatements().size() != 3) {
            System.err.println("program.Statements does not contain 3 statements. got=" +  program.getStatements().size());
        }

        for (Statement stmt : program.getStatements()) {
            if (!(stmt instanceof ReturnStatement)) {
                System.err.println("stmt not ReturnStatement. got=" + stmt.getClass().getSimpleName());
                continue;
            }

            ReturnStatement returnStmt = (ReturnStatement) stmt;

            if (!returnStmt.tokenLiteral().equals("return")) {
                System.err.println("retuenStmt.TokenLiteral not 'return'. got= " + returnStmt.tokenLiteral());
            }
        }*/
    }

    /**
     * 验证一个语句是否为 let 语句，且变量名正确
     *
     * @param statement 要验证的语句
     * @param name      预期的变量名
     * @return 是否通过验证
     */
    public static boolean testLetStatement(Statement statement, String name) {
        if (!"let".equals(statement.getToken().getLiteral())) {
            System.err.println("s.TokenLiteral not 'let'. got=" + statement
                    .getToken().getLiteral());
            return false;
        }

        if (!(statement instanceof LetStatement)) {
            System.err.println("s not instanceof LetStatement. got=" + statement.getClass().getSimpleName());
            return false;
        }

        LetStatement letStmt = (LetStatement) statement;
        Identifier nameIdentifier = letStmt.getName();

        if (nameIdentifier == null) {
            System.err.println("letStmt.getName() is null");
            return false;
        }

        if (!name.equals(nameIdentifier.getValue())) {
            System.err.println("letStmt.Name.Value not '" + name + "'. got=" + nameIdentifier.getValue());
            return false;
        }

        if (!name.equals(nameIdentifier.getToken().getLiteral())) {
            System.err.println("letStmt.Name.TokenLiteral not '" + name
                    + "'. got=" + nameIdentifier.getToken().getLiteral());
            return false;
        }

        System.out.println("PASSED: let " + name + " parsed correctly");
        return true;
    }

    /**
     * 检查解析器是否有错误
     *
     * @param parser 解析器实例
     */
    public static void checkParserErrors(Parser parser) {
        List<String> errors = parser.getErrors();
        if (errors.isEmpty()) {
            System.out.println("No parser errors.");
            return;
        }

        System.out.println("Parser has " + errors.size() + " errors:");
        for (String msg : errors) {
            System.err.println("Parser error: " + msg);
        }
    }

    /**
     * 测试标识符表达式（Identifier Expression）的解析。
     *
     * <p>输入：一个简单的标识符 "foobar;"。
     *
     * <p>预期行为：
     * <ul>
     *   <li>解析后得到的 Program 节点包含 1 条语句</li>
     *   <li>该语句是 ExpressionStatement 类型</li>
     *   <li>该语句的表达式是 Identifier 类型</li>
     *   <li>Identifier 的值（value）为 "foobar"</li>
     *   <li>Identifier 的词法单元文本（token.literal）为 "foobar"</li>
     * </ul>
     *
     * <p>对应 Go 版本的测试函数：
     * <pre>{@code
     * func TestIdentifierExpressions(t *testing.T) {
     *     input := "foobar;"
     *     ...
     * }
     * }</pre>
     */
    public static void testIdentifierExpressions() {
        String input = "foobar;";

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkParserErrors(parser);

        if (program.getStatements().size() != 1) {
            System.err.println("progra has not enough statements. got="
                    + program.getStatements().size());
        }

        Statement statement = program.getStatements().get(0);
        if (!(statement instanceof ExpressionStatement)) {
            System.err.println("program.Statements[0] is not ast.ExpressionStatement. got="
                    + statement.getClass().getSimpleName());
            return;
        }
        ExpressionStatement stmt = (ExpressionStatement) statement;

        if (!(stmt.getExpression() instanceof Identifier)) {
            System.err.println("exp not *ast.Identifier. got=" +
                    stmt.getExpression().getClass().getSimpleName());
        }
        Identifier identifier = (Identifier) stmt.getExpression();
        if (!(identifier.getValue().equals("foobar"))) {
            System.err.println("ident.Value not foobar. got=" +
                    identifier.getValue());
        }
        if (!(identifier.tokenLiteral().equals("foobar"))) {
            System.err.println("ident.TokenLiteral not foobar. got=" +
                    identifier.tokenLiteral());
        }

    }

    /**
     * 测试整数字面量表达式（IntegerLiteral Expression）的解析。
     *
     * <p>输入：字符串 "5;"。
     *
     * <p>预期行为：
     * <ul>
     *   <li>解析后 Program 节点包含 1 条语句</li>
     *   <li>该语句是 ExpressionStatement 类型</li>
     *   <li>该语句的表达式是 IntegerLiteral 类型</li>
     *   <li>IntegerLiteral 的值为 5</li>
     *   <li>IntegerLiteral 的词法单元文本为 "5"</li>
     * </ul>
     */
    public static void testIntegerLiteralExpression() {
        String input = "5;";

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkParserErrors(parser);

        if (program.getStatements().size() != 1) {
            System.err.println("program has not enough statements. got=" +
                    program.getStatements().size());
        }
        if (!(program.getStatements().get(0) instanceof ExpressionStatement)) {
            System.err.println("program.Statements[0] is not ast.ExpressionStatement. got=" +
                    program.getStatements().get(0).getClass().getSimpleName());
        }
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements().get(0);

        if (!(stmt.getExpression() instanceof IntegerLiteral)) {
            System.err.println("exp not *ast.IntegerLiteral. got=" +
                    stmt.getExpression().getClass().getSimpleName());
            return;
        }
        IntegerLiteral integerLiteral = (IntegerLiteral) stmt.getExpression();
        if (integerLiteral.getValue() != 5) {
            System.err.println("integer.Value not 5. got=" +
                    integerLiteral.getValue());
        }
        if  (!(integerLiteral.tokenLiteral().equals("5"))) {
            System.err.println("integer.TokenLiteral not 5. got=" +
                    integerLiteral.tokenLiteral());
        }

    }

    /**
     * 测试前缀表达式（Prefix Expression）的解析。
     *
     * <p>输入：如 "!5;" 或 "-15;"。
     *
     * <p>预期行为：
     * <ul>
     *   <li>解析后 Program 节点包含 1 条语句</li>
     *   <li>该语句是 ExpressionStatement 类型</li>
     *   <li>该语句的表达式是 PrefixExpression 类型</li>
     *   <li>PrefixExpression 的操作符与预期一致</li>
     *   <li>PrefixExpression 的右操作数是整数，值与预期一致</li>
     * </ul>
     */
    public static void testParsingPrefixExpressions() {
        List<TestParsingPrefixExpressionsCase> testCases =
                Arrays.asList(new TestParsingPrefixExpressionsCase(
                        "!5;", "!", 5),
                        new TestParsingPrefixExpressionsCase(
                                "-15;", "-", 15),
                        new TestParsingPrefixExpressionsCase("!true", "!", true),
                        new TestParsingPrefixExpressionsCase("!false", "!", false));

        for (TestParsingPrefixExpressionsCase testCase : testCases) {
            Lexer lexer = new Lexer(testCase.input);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            if (program.getStatements().size() != 1) {
                System.err.println("program.Statements does not contain 1 statements. got=" +
                        program.getStatements().size());
                return;
            }

            if (!(program.getStatements().get(0) instanceof ExpressionStatement)) {
                System.err.println("program.Statements[0] is not ast.ExpressionStatement. got=" +
                        program.getStatements().get(0).getClass().getSimpleName());
                return;
            }
            ExpressionStatement statement = (ExpressionStatement) program.getStatements().get(0);

            if (!(statement.getExpression() instanceof PrefixExpression)) {
                System.err.println("stmt is not ast.PrefixExpression. got=" +
                        statement.getExpression().getClass().getSimpleName());
                return;
            }
            PrefixExpression prefixExpression = (PrefixExpression) statement.getExpression();
            if (!prefixExpression.getOperator().equals(testCase.operator)) {
                System.err.println("exp.Operator is not '" + testCase.operator
                        + "'. got=" + prefixExpression.getOperator());
            }
            if (!testLiteralExpression(prefixExpression.getRight(), testCase.value)) {
                return;
            }
        }
    }

    /**
     * 测试解析中缀表达式（如 "5 + 5;"）。
     *
     * <p>该测试用例会验证 Parser 能否正确地将输入的中缀表达式
     * 解析为抽象语法树（AST），并检查以下几点：
     * <ul>
     *     <li>Program 中只包含 1 条语句</li>
     *     <li>语句类型为 ExpressionStatement</li>
     *     <li>表达式类型为 InfixExpression</li>
     *     <li>左侧、操作符、右侧均符合预期</li>
     * </ul>
     */
    public static void testParsingInfixExpressions() {
        List<InfixTestCase> infixTestCases = Arrays.asList(new InfixTestCase(
                "5 + 5;", 5, "+", 5),
                new InfixTestCase("5 - 5;", 5, "-", 5),
                new InfixTestCase("5 * 5;", 5, "*", 5),
                new InfixTestCase("5 / 5;", 5, "/", 5),
                new InfixTestCase("5 > 5;", 5, ">", 5),
                new InfixTestCase("5 < 5;", 5, "<", 5),
                new InfixTestCase("5 == 5;", 5, "==", 5),
                new InfixTestCase("5 != 5;", 5, "!=", 5),
                new InfixTestCase("true == true", true, "==", true),
                new InfixTestCase("true != false", true, "!=", false),
                new InfixTestCase("false == false", false, "==", false));

        for (InfixTestCase testCase : infixTestCases) {
            Lexer lexer = new Lexer(testCase.input);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            if (program.getStatements().size() != 1) {
                System.err.println("program.Statements does not contain 1 statements. got=" +
                        program.getStatements().size());
                return;
            }

            if (!(program.getStatements().get(0) instanceof ExpressionStatement)) {
                System.err.println("program.Statements[0] is not ast.ExpressionStatement. got=" +
                        program.getStatements().get(0).getClass().getSimpleName());
                return;
            }

            ExpressionStatement stmt = (ExpressionStatement) program.getStatements().get(0);

            if (!testInfixExpression(stmt.getExpression(), testCase.leftValue,
                    testCase.operator, testCase.rightValue)) {
                return;
            }
        }

    }

    /**
     * 测试解析器对不同运算符的优先级和结合性的处理。
     *
     * <p>通过多组输入（例如 "-a * b", "a + b * c + d / e - f" 等），
     * 将 Parser 生成的 AST 转换为字符串（带括号），
     * 并与预期结果进行比较。</p>
     *
     * <p>本测试的核心目标：</p>
     * <ul>
     *     <li>验证一元运算符（-、!）的优先级高于二元运算符</li>
     *     <li>验证乘除优先级高于加减</li>
     *     <li>验证关系运算符（>、<）和相等运算符（==、!=）的结合顺序</li>
     *     <li>验证括号能正确改变运算顺序</li>
     *     <li>验证函数调用中的表达式是否正确解析</li>
     * </ul>
     */
    public static void testOperatorPrecedenceParsing() {
        List<OperatorTestCase> operatorTestCases = Arrays.asList(new OperatorTestCase(
                "-a * b", "((-a) * b)"),
                new OperatorTestCase("!-a", "(!(-a))"),
                new OperatorTestCase("a + b + c", "((a + b) + c)"),
                new OperatorTestCase("a + b - c", "((a + b) - c)"),
                new OperatorTestCase("a * b  * c", "((a * b) * c)"),
                new OperatorTestCase("a * b / c", "((a * b) / c)"),
                new OperatorTestCase("a + b / c", "(a + (b / c))"),
                new OperatorTestCase("a + b * c + d / e - f",
                        "(((a + (b * c)) + (d / e)) - f)"),
                new OperatorTestCase("3 + 4; -5 * 5", "(3 + 4)((-5) * 5)"),
                new OperatorTestCase("5 > 4 == 3 < 4", "((5 > 4) == (3 < 4))"),
                new OperatorTestCase("5 < 4 != 3 < 4", "((5 < 4) != (3 < 4))"),
                new OperatorTestCase("3 + 4 * 5 == 3 * 1 + 4 * 5",
                        "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))"),
                new OperatorTestCase("true", "true"),
                new OperatorTestCase("false", "false"),
                new OperatorTestCase("3 > 5 == false",
                        "((3 > 5) == false)"),
                new OperatorTestCase("3 < 5 == true",
                        "((3 < 5) == true)"),
                new OperatorTestCase("1 + (2 + 3) + 4",
                        "((1 + (2 + 3)) + 4)"),
                new OperatorTestCase("(5 + 5) * 2",
                        "((5 + 5) * 2)"),
                new OperatorTestCase("2 / (5 + 5)",
                        "(2 / (5 + 5))"),
                new OperatorTestCase("(5 + 5) * 2 * (5 + 5)",
                        "(((5 + 5) * 2) * (5 + 5))"),
                new OperatorTestCase("-(5 + 5)",
                        "(-(5 + 5))"),
                new OperatorTestCase("!(true == true)",
                        "(!(true == true))"),
                new OperatorTestCase("a + add(b * c) + d",
                        "((a + add((b * c))) + d)"),
                new OperatorTestCase("add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))",
                        "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))"),
                new OperatorTestCase("add(a + b + c * d / f + g)",
                        "add((((a + b) + ((c * d) / f)) + g))"));

        for (OperatorTestCase testCase : operatorTestCases) {
            Lexer lexer = new Lexer(testCase.input);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            String string = program.toString();
            if (!string.equals(testCase.expected)) {
                System.err.println("expected=" + testCase.expected + " ,got=" +
                        string);
            }
        }

    }

    /**
     * 测试单独的 if 表达式解析
     * 例如：if (x < y) { x }
     */
    public static void testIfExpression() {
        String input = "if (x < y) { x }";

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkParserErrors(parser);

        if (program.getStatements().size() != 1) {
            System.err.println("program.Statements does not contain 1 statements. got=" +
                    program.getStatements().size());
            return;
        }

        // 类型检查
        if (!(program.getStatements().get(0) instanceof ExpressionStatement)) {
            System.err.println("program.Statements[0] is not ast.ExpressionStatement. got=" +
                    program.getStatements().get(0).getClass().getSimpleName());
            return;
        }

        ExpressionStatement stmt = (ExpressionStatement) program.getStatements().get(0);

        // 类型检查
        if (!(stmt.getExpression() instanceof IfExpression)) {
            System.err.println("stmt.Expression is not ast.IfExpression. got=" +
                    stmt.getExpression().getClass().getSimpleName());
            return;
        }
        IfExpression ifExp = (IfExpression) stmt.getExpression();

        if (!testInfixExpression(ifExp.getCondition(), "x", "<", "y")) {
            return;
        }

        if (ifExp.getConsequence().getStatements().size() != 1) {
            System.err.println("ifExp.Consequence is not 1 statement. got=" +
                    ifExp.getConsequence().getStatements().size());
            return;
        }

        // 类型检查
        if (!(ifExp.getConsequence().getStatements().get(0) instanceof ExpressionStatement)) {
            System.err.println("ifExp.Consequence.Statements[0] is not ast.ExpressionStatement. got=" +
                    ifExp.getConsequence().getStatements().get(0).getClass().getSimpleName());
            return;
        }

        ExpressionStatement consequence = (ExpressionStatement) ifExp.getConsequence().getStatements().get(0);

        if (!testIdentifier(consequence.getExpression(), "x")) {
            return;
        }

        if (ifExp.getAlternative() != null) {
            System.err.println("ifExp.Alternative.Statements was not null. got=" +
                    ifExp.getAlternative().getStatements().get(0));
        }
    }

    /**
     * 测试 if-else 表达式解析
     * 例如：if (x < y) { x } else { y }
     */
    public static void testIfElseExpression() {
        String input = "if (x < y) { x } else { y }";

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkParserErrors(parser);

        if (program.getStatements().size() != 1) {
            System.err.println("program.Statements does not contain 1 statements. got=" +
                    program.getStatements().size());
            return;
        }

        Statement statement = program.getStatements().get(0);
        // 类型检查
        if (!(statement instanceof ExpressionStatement)) {
            System.err.println("program.Statements[0] is not ast.ExpressionStatement. got=" +
                    statement.getClass().getSimpleName());
            return;
        }

        ExpressionStatement stmt = (ExpressionStatement) statement;
        // 类型检查
        if (!(stmt.getExpression() instanceof IfExpression)) {
            System.err.println("stmt.Expression is not ast.IfExpression. got=" +
                    stmt.getExpression().getClass().getSimpleName());
            return;
        }

        IfExpression ifExp = (IfExpression) stmt.getExpression();

        if (!testInfixExpression(ifExp.getCondition(), "x", "<", "y")) {
            return;
        }

        if (ifExp.getConsequence().getStatements().size() != 1) {
            System.err.println("ifExp.Consequence is not 1 statement. got=" +
                    ifExp.getConsequence().getStatements().size());
            return;
        }

        Statement statement1 = ifExp.getConsequence().getStatements().get(0);
        // 类型检查
        if (!(statement1 instanceof ExpressionStatement)) {
            System.err.println("ifExp.Consequence.Statements[0] is not ast.ExpressionStatement." +
                    "got=" + statement1.getClass().getSimpleName());
            return;
        }
        ExpressionStatement consequence = (ExpressionStatement) statement1;
        if (!testIdentifier(consequence.getExpression(), "x")) {
            return;
        }

        if ((ifExp.getAlternative().getStatements().size() != 1)) {
            System.err.println("ifExp.Alternative is not 1 statement. got=" +
                    ifExp.getAlternative().getStatements().size());
            return;
        }

        Statement statement2 = ifExp.getAlternative().getStatements().get(0);
        // 类型检查
        if (!(statement2 instanceof ExpressionStatement)) {
            System.err.println("ifExp.Alternative.Statements[0] is not ast.ExpressionStatement" +
                    ". got=" + statement2.getClass().getSimpleName());
            return;
        }
        ExpressionStatement alternative = (ExpressionStatement) statement2;
        if (!testIdentifier(alternative.getExpression(), "y")) {
            return;
        }

    }

    /**
     * 测试解析器对函数字面量 (function literal) 的处理。
     *
     * <p>输入：一个函数定义 {@code fn(x, y) { x + y; }}
     * <br>测试点包括：
     * <ul>
     *     <li>是否被正确解析为 {@link FunctionLiteral}</li>
     *     <li>函数参数是否解析正确（数量和名称）</li>
     *     <li>函数体语句数量是否正确</li>
     *     <li>函数体中的表达式是否正确解析为 {@code x + y}</li>
     * </ul>
     */
    public static void testFunctionLiteralExpression() {
        String input = "fn(x, y) { x + y; }";

        // 1. 词法分析
        Lexer lexer = new Lexer(input);
        // 2. 语法分析
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkParserErrors(parser);

        // 3. 程序必须只有 1 条语句
        if (program.getStatements().size() != 1) {
            System.err.println("program.Statements does not contain 1 statements. got=" +
                    program.getStatements().size());
            return;
        }

        // 4. 获取语句并检查类型
        Statement statement = program.getStatements().get(0);
        // 类型检查
        if (!(statement instanceof ExpressionStatement)) {
            System.err.println("program.Statements[0] is not ast.ExpressionStatement. got=" +
                    statement.getClass().getSimpleName());
            return;
        }

        // 5. 检查表达式是否是 FunctionLiteral
        ExpressionStatement stmt = (ExpressionStatement) statement;
        // 类型检查
        if (!(stmt.getExpression() instanceof FunctionLiteral)) {
            System.err.println("stmt.Expression is not ast.FunctionLiteral. got=" +
                    stmt.getExpression().getClass().getSimpleName());
            return;
        }

        FunctionLiteral function = (FunctionLiteral) stmt.getExpression();
        // 6. 检查函数参数数量
        if (function.getParameters().size() != 2) {
            System.err.println("function literal parameters wrong. got=" +
                    function.getParameters().size());
            return;
        }

        // 7. 检查参数名是否正确
        testLiteralExpression(function.getParameters().get(0), "x");
        testLiteralExpression(function.getParameters().get(1), "y");

        // 8. 检查函数体是否只有 1 条语句
        if (function.getBody().getStatements().size() != 1) {
            System.err.println("function.Body.Statements has not 1 statements. got=" +
                    function.getBody().getStatements().size());
            return;
        }

        // 9. 获取函数体语句并检查类型
        Statement bodyStmt = function.getBody().getStatements().get(0);
        // 类型检查
        if (!(bodyStmt instanceof ExpressionStatement)) {
            System.err.println("function body stmt is not ast.ExpressionStatement. got=" +
                    bodyStmt.getClass().getSimpleName());
            return;
        }

        ExpressionStatement body = (ExpressionStatement) bodyStmt;

        // 10. 检查函数体表达式是否是 "x + y"
        testInfixExpression(body.getExpression(), "x", "+", "y");
    }

    /**
     * 测试函数调用表达式的解析。
     *
     * <p>输入示例：{@code add(1, 2 * 3, 4 + 5);}
     * <br>该方法验证解析器是否能正确构建对应的抽象语法树（AST），包括：
     * <ul>
     *     <li>函数调用是否被识别为 {@link CallExpression}</li>
     *     <li>调用的函数名是否为 {@code add}</li>
     *     <li>参数列表的长度是否正确</li>
     *     <li>各参数表达式是否正确解析（字面量、运算符优先级）</li>
     * </ul>
     *
     * <p>例如，{@code add(1, 2 * 3, 4 + 5)} 应被解析为：
     * <pre>
     * add(
     *   1,
     *   (2 * 3),
     *   (4 + 5)
     * )
     * </pre>
     */
    public static void testCallExpressionParsing() {
        String input = "add(1, 2 * 3, 4 + 5);";

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkParserErrors(parser);

        List<Statement> statements = program.getStatements();
        if (statements.size() != 1) {
            System.err.println("program.Statements does not contain 1 statements. got=" +
                    statements.size());
            return;
        }

        Statement statement = statements.get(0);
        // 类型检查
        if (!(statement instanceof ExpressionStatement)) {
            System.err.println("statement is not ast.ExpressionStatement. got=" +
                    statement.getClass().getSimpleName());
            return;
        }
        ExpressionStatement stmt = (ExpressionStatement) statement;

        // 类型检查
        if (!(stmt.getExpression() instanceof CallExpression)) {
            System.err.println("stmt.Expression is not ast.CallExpression. got=" +
                    stmt.getExpression().getClass().getSimpleName());
            return;
        }

        CallExpression exp = (CallExpression) stmt.getExpression();

        if(testIdentifier(exp.getFunction(), "add")) {
            return;
        }

        // 参数数量检查
        if (exp.getArguments().size() != 3) {
            System.err.println("wrong length of arguments. got=" +
                    exp.getArguments().size());
            return;
        }

        testLiteralExpression(exp.getArguments().get(0), 1);
        testInfixExpression(exp.getArguments().get(1), 2, "*", 3);
        testInfixExpression(exp.getArguments().get(2), 4, "+", 5);
    }

    /**
     * 辅助测试方法：根据期望值的类型，判断表达式节点是否匹配。
     *
     * @param exp      表达式节点
     * @param expected 期望值，可以是 Integer, Long, String, Boolean
     * @return true 如果检查通过，否则 false
     */
    public static boolean testLiteralExpression(Expression exp, Object expected) {
        if (expected instanceof Integer) {
            return testIntegerLiteral(exp, ((Integer) expected).longValue());
        } else if(expected instanceof Long) {
            return testIntegerLiteral(exp, (Long) expected);
        } else if(expected instanceof String){
            return testIdentifier(exp, (String) expected);
        } else if(expected instanceof Boolean) {
            return testBooleanLiteral(exp, (Boolean) expected);
        } else {
            System.err.println("type of exp not handled. got=" + exp);
            return false;
        }
    }

    /**
     * 辅助测试方法：检查表达式是否为指定值的整数字面量。
     *
     * @param exp   表达式
     * @param value 期望的整数值
     * @return true 如果检查通过，否则 false
     */
    public static boolean testIntegerLiteral(Expression exp, long value) {
        if (!(exp instanceof IntegerLiteral)) {
            System.err.println("exp not *ast.IntegerLiteral. got=" +
                    exp.getClass().getSimpleName());
            return false;
        }
        IntegerLiteral integerLiteral = (IntegerLiteral) exp;
        if (integerLiteral.getValue() != value) {
            System.err.println("integer.Value not "
                    + value + ". got=" + integerLiteral.getValue());
            return false;
        }
        if (!(integerLiteral.tokenLiteral().equals(String.valueOf(value)))) {
            System.err.println("integer.TokenLiteral not " + value + ". got="
                    + integerLiteral.tokenLiteral());
            return false;
        }

        return true;
    }

    /**
     * 辅助测试方法：检查表达式是否为指定标识符。
     *
     * @param exp   表达式节点
     * @param value 期望标识符的字符串值
     * @return true 如果检查通过，否则 false
     */
    public static boolean testIdentifier(Expression exp, String value) {
        if (!(exp instanceof Identifier)) {
            System.err.println("exp not *ast.Identifier. got=" +
                    exp.getClass().getSimpleName());
            return false;
        }
        Identifier identifier = (Identifier) exp;

        if (!identifier.getValue().equals(value)) {
            System.err.println("ident.Value not " + value +". got=" +
                    identifier.getValue());
            return false;
        }

        if (!identifier.tokenLiteral().equals(value)) {
            System.err.println("ident.TokenLiteral not " + value + ". got=" +
                    identifier.tokenLiteral());
            return false;
        }

        return true;
    }

    /**
     * 辅助测试方法：检查表达式是否为指定布尔值。
     *
     * @param exp   表达式节点
     * @param value 期望布尔值
     * @return true 如果检查通过，否则 false
     */
    public static boolean testBooleanLiteral(Expression exp, boolean value) {
        if (!(exp instanceof BooleanType)) {
            System.err.println("exp not *ast.Boolean. got=" +
                    exp.getClass().getSimpleName());
            return false;
        }

        BooleanType booleanType = (BooleanType) exp;

        if (booleanType.isValue() != value) {
            System.err.println("booleanType.Value not " + value + ". got=" +
                    booleanType.isValue());
            return false;
        }

        if (!booleanType.tokenLiteral().equals(String.valueOf(value))) {
            System.err.println("booleanType.TokenLiteral not " + value + ". got=" +
                    booleanType.tokenLiteral());
            return false;
        }

        return true;
    }

    /**
     * 辅助测试方法：检查表达式是否为指定中缀表达式。
     *
     * <p>会依次检查：</p>
     * <ul>
     *     <li>表达式类型是否为 {@link InfixExpression}</li>
     *     <li>左操作数是否与预期值匹配</li>
     *     <li>运算符是否与预期值匹配</li>
     *     <li>右操作数是否与预期值匹配</li>
     * </ul>
     *
     * @param exp      待检查的表达式节点
     * @param left     预期的左操作数（Integer, Long, String, Boolean）
     * @param operator 预期的运算符，例如 "+"、"-"、"*"、"/"
     * @param right    预期的右操作数（Integer, Long, String, Boolean）
     * @return true 如果中缀表达式及其操作数和运算符全部匹配，否则 false
     */
    public static boolean testInfixExpression(Expression exp, Object left, String operator,
                                              Object right) {
        if (!(exp instanceof InfixExpression)) {
            System.err.println("exp is not ast.InfixExpression. got=" +
                    exp.getClass().getSimpleName());
            return false;
        }

        InfixExpression infixExpression = (InfixExpression) exp;

        if (!testLiteralExpression(infixExpression.getLeft(), left)) {
            return false;
        }

        if (!infixExpression.getOperator().equals(operator)) {
            System.err.println("exp.Operator is not '" + operator + "'. got=" +
                    infixExpression.getOperator());
            return false;
        }

        if (!testLiteralExpression(infixExpression.getRight(), right)) {
            return false;
        }

        return true;
    }
}

/**
 * 前缀表达式测试用例数据结构。
 */
class TestParsingPrefixExpressionsCase {
    public String input;
    public String operator;
    public Object value;

    public TestParsingPrefixExpressionsCase(String input, String operator, Object object) {
        this.input = input;
        this.operator = operator;
        this.value = object;
    }
}

/**
 * 用于保存中缀表达式测试用例的数据结构。
 *
 * <p>包含：
 * <ul>
 *     <li>input：待解析的源代码字符串（例如 "5 + 5;"）</li>
 *     <li>leftValue：中缀表达式左操作数的整数值</li>
 *     <li>operator：中缀操作符（+、-、*、/、>、<、==、!=）</li>
 *     <li>rightValue：中缀表达式右操作数的整数值</li>
 * </ul>
 */
class InfixTestCase {
    public String input;
    public Object leftValue;
    public String operator;
    public Object rightValue;

    public InfixTestCase(String input, Object leftValue, String operator, Object rightValue) {
        this.input = input;
        this.leftValue = leftValue;
        this.operator = operator;
        this.rightValue = rightValue;
    }
}

/**
 * 用于保存运算符优先级测试用例的数据结构。
 *
 * <p>包含：
 * <ul>
 *     <li>input：待解析的源代码字符串（例如 "a + b * c"）</li>
 *     <li>expected：Parser 生成的 AST 的字符串表示（例如 "(a + (b * c))"）</li>
 * </ul>
 */
class OperatorTestCase {
    public String input;
    public String expected;

    public OperatorTestCase(String input, String expected) {
        this.input = input;
        this.expected = expected;
    }
}

/**
 * 表示一条 let 语句的测试用例。
 *
 * <p>用于测试解析器对 {@code let <identifier> = <value>;} 语句的解析是否正确。
 * <br>典型示例：
 * <ul>
 *     <li>输入：{@code let x = 5;} → 期望标识符 {@code x}，期望值 {@code 5}</li>
 *     <li>输入：{@code let foobar = true;} → 期望标识符 {@code foobar}，期望值 {@code true}</li>
 * </ul>
 */
class LetTestCase {
    public String input;
    public String expectedIdentifier;
    public Object expectedValue;

    public LetTestCase(String input, String expectedIdentifier, Object expectedValue) {
        this.input = input;
        this.expectedIdentifier = expectedIdentifier;
        this.expectedValue = expectedValue;
    }
}

/**
 * 表示一条 return 语句的测试用例。
 *
 * <p>用于测试解析器对 {@code return <value>;} 语句的解析是否正确。
 * <br>典型示例：
 * <ul>
 *     <li>输入：{@code return 5;} → 期望返回值 {@code 5}</li>
 *     <li>输入：{@code return true;} → 期望返回值 {@code true}</li>
 * </ul>
 */
class ReturnTestCase {
    public String input;
    public Object expectedValue;

    public ReturnTestCase(String input, Object expectedValue) {
        this.input = input;
        this.expectedValue = expectedValue;
    }
}