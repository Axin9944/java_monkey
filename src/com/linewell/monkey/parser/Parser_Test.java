package com.linewell.monkey.parser;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.*;
import com.linewell.monkey.lexer.Lexer;

import java.util.Arrays;
import java.util.List;

public class Parser_Test {

    public static void main(String[] args) {
        System.out.println("Running Parser Tests...\n");
        //testLetStatements();
        testReturnStatements();
        testIdentifierExpressions();
        testIntegerLiteralExpression();
        testParsingPrefixExpressions();
        testParsingInfixExpressions();
        testOperatorPrecedenceParsing();
    }

    /**
     * 测试 let 语句的解析（包括错误处理）
     */
    public static void testLetStatements() {
        // 输入：故意写错的 Monkey 代码（缺少 '='）
        String input = "\"" +
                "let x = 5;" +
                "let = 10;" +
                "let foobar 838383;" +
                "\"";

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);

        // 解析整个程序
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
        }
    }

    public static void testReturnStatements(){
        String input = "return 5;\n" +
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
        }
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
                                "-15;", "-", 15));

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
            if (!testIntegerLiteral(prefixExpression.getRight(), testCase.integerValue)) {
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
                new InfixTestCase("5 != 5;", 5, "!=", 5));

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

            if (!(stmt.getExpression() instanceof InfixExpression)) {
                System.err.println("stmt is not InfixExpression. got=" +
                        stmt.getClass().getSimpleName());
                return;
            }

            InfixExpression infixExp = (InfixExpression) stmt.getExpression();

            if (!testIntegerLiteral(infixExp.getLeft(), testCase.leftValue)) {
                return;
            }

            if (!infixExp.getOperator().equals(testCase.operator)) {
                System.err.println("exp.Operator is not '" + testCase.operator
                        + "'. got=" + infixExp.getOperator());
                return;
            }

            if (!testIntegerLiteral(infixExp.getRight(), testCase.rightValue)) {
                return;
            }
        }

    }

    /**
     * 测试运算符优先级的解析。
     *
     * <p>该测试用例通过多组输入（如 "-a * b", "a + b * c + d / e - f" 等），
     * 检查 Parser 生成的 AST 的 toString() 输出是否与预期的括号化表达式一致。
     * <br>
     * 核心目的是验证：
     * <ul>
     *     <li>一元运算符（-、!）优先级高于二元运算符</li>
     *     <li>乘除高于加减</li>
     *     <li>关系运算符（>、<）和相等运算符（==、!=）的结合性</li>
     *     <li>表达式的整体结合顺序是否正确</li>
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
                        "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))"));

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
}

/**
 * 前缀表达式测试用例数据结构。
 */
class TestParsingPrefixExpressionsCase {
    public String input;
    public String operator;
    public long integerValue;

    public TestParsingPrefixExpressionsCase(String input, String operator, long integerValue) {
        this.input = input;
        this.operator = operator;
        this.integerValue = integerValue;
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
    public long leftValue;
    public String operator;
    public long rightValue;

    public InfixTestCase(String input, long leftValue, String operator, long rightValue) {
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
