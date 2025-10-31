package com.linewell.monkey.evaluator;

import com.linewell.monkey.ast.imp.Identifier;
import com.linewell.monkey.ast.imp.Program;
import com.linewell.monkey.lexer.Lexer;
import com.linewell.monkey.object.Environment;
import com.linewell.monkey.object.HashKey;
import com.linewell.monkey.object.HashPair;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.imp.*;
import com.linewell.monkey.parser.Parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.linewell.monkey.object.imp.MonkeyBoolean.FALSE;
import static com.linewell.monkey.object.imp.MonkeyBoolean.TRUE;

/**
 * 对 {@link Evaluator} 进行单元测试的类。
 * <p>
 * 本测试类的目标是验证解释器（Evaluator）能否正确执行输入的 Monkey 程序。
 * 当前实现了以下两类测试：
 * <ul>
 *     <li>整数表达式测试：加减乘除、括号优先级、前缀运算等</li>
 *     <li>布尔表达式测试：关系运算（<、>、==、!=）、布尔字面量、布尔逻辑表达式</li>
 *     <li>逻辑非运算（!、!!）</li>
 *     <li>条件语句（if-else 表达式）</li>
 *     <li>返回语句（return）</li>
 *     <li>错误处理（类型错误、未定义标识符等）</li>
 *     <li>变量绑定（let 语句）</li>
 *     <li>函数定义与函数对象测试（{@link #testFunctionObject()}）</li>
 *     <li>函数调用与参数传递测试（{@link #testFunctionApplication()}）</li>
 *     <li>闭包（Closure）测试（{@link #testClosures()}）</li>
 *     <li>解析字符串对象测试（{@link #testStringLiteral()}）</li>
 *     <li>字符串对象拼接测试（{@link #testStringConcatenation()}）</li>
 *     <li>内置函数调用测试（{@link #testBuiltinFunction()}）：
 *            验证内置函数（如 <code>len()</code>）的求值结果与错误处理逻辑</li>
 *      <li>数组字面量解析与求值测试（{@link #testArrayLiteral()}）</li>
 *      <li>数组索引表达式求值测试（{@link #testArrayIndexExpressions()}）</li>
 *      <li>quote函数求值 （{@link #testQuote()}）</li>
 *      <li>unquote函数求值 及 unquote与quote的组合使用
 *          （{@link #testQuoteUnquote()}） </li>
 * </ul>
 * <p>
 * 测试方法通过将输入的源代码字符串送入词法分析器、语法分析器，
 * 构造出 AST，再交由解释器执行，并将结果与预期值对比。
 *
 * @author axin
 */
public class EvaluatorTest {

    public static void main(String[] args) {
        // 执行整数表达式的测试
        testEvalIntegerExpression();

        // 执行布尔表达式的测试
        testEvalBooleanExpression();

        // 执行逻辑非运算的测试
        testBangOperator();

        // 执行 if-else 表达式测试
        testIfElseExpression();

        // 执行 return 语句测试
        testReturnStatement();

        // 执行错误处理测试
        testErrorHandling();

        // 执行 let 语句测试
        testLetStatement();

        // 执行函数对象解析测试
        testFunctionObject();

        // 执行函数调用测试
        testFunctionApplication();

        // 执行闭包测试
        testClosures();

        // 执行解析字符串对象测试
        testStringLiteral();

        // 执行字符串拼接测试
        testStringConcatenation();

        // 执行内置函数测试
        testBuiltinFunction();

        // 执行数组字面量解析与求值测试
        testArrayLiteral();

        // 执行数组索引表达式求值测试
        testArrayIndexExpressions();

        // 执行哈希字面量解析与求值测试
        testHashLiterals();

        // 执行哈希表索引表达式求值测试
        testHashIndexExpressions();

        // 执行 quote函数 测试
        testQuote();

        // 执行 quote 及 unquote 测试
        testQuoteUnquote();
    }

    /**
     * 测试整数表达式的求值结果是否正确。
     * <p>
     * 使用一系列输入表达式（字符串）和期望结果，依次传入解释器进行求值，
     * 然后与期望结果进行比对，输出测试结果。
     */
    public static void testEvalIntegerExpression() {
        List<EvalIntTestCase> testCases = Arrays.asList(
                new EvalIntTestCase("5", 5L),
                new EvalIntTestCase("10", 10L),
                new EvalIntTestCase("-5", -5L),
                new EvalIntTestCase("-10", -10L),
                new EvalIntTestCase("5 + 5 + 5 + 5 - 10", 10L),
                new EvalIntTestCase("2 * 2 * 2 * 2 * 2", 32),
                new EvalIntTestCase("-50 + 100 + -50", 0),
                new EvalIntTestCase("5 * 2 + 10", 20L),
                new EvalIntTestCase("5 + 2 * 10", 25L),
                new EvalIntTestCase("20 + 2 * -10", 0L),
                new EvalIntTestCase("50 / 2 * 2 + 10", 60L),
                new EvalIntTestCase("2 * (5 + 10)", 30L),
                new EvalIntTestCase("3 * 3 * 3 + 10", 37L),
                new EvalIntTestCase("3 * (3 * 3) + 10", 37L),
                new EvalIntTestCase("(5 + 10 * 2 + 15 / 3) * 2 + -10", 50L)
        );

        for (EvalIntTestCase testCase : testCases) {
            MonkeyObject evaluated = testEval(testCase.input);
            if (testMonkeyInteger(evaluated, testCase.expected)) {
                System.out.println(testCase.input + " = " + testCase.expected + " [Parse]");
            }
        }

    }

    /**
     * 测试布尔表达式的求值结果是否正确。
     * <p>
     * 测试内容包括：
     * <ul>
     *     <li>布尔字面量（true、false）</li>
     *     <li>整数比较（<、>、==、!=）</li>
     *     <li>布尔比较（true == false, true != false 等）</li>
     *     <li>括号中的逻辑运算</li>
     * </ul>
     */
    public static void testEvalBooleanExpression() {
        List<EvalBoolTestCase> testCases = Arrays.asList(
                new EvalBoolTestCase("true", true),
                new EvalBoolTestCase("false", false),
                new EvalBoolTestCase("1 < 2", true),
                new EvalBoolTestCase("1 > 2", false),
                new EvalBoolTestCase("1 < 1", false),
                new EvalBoolTestCase("1 > 1", false),
                new EvalBoolTestCase("1 == 1", true),
                new EvalBoolTestCase("1 != 1", false),
                new EvalBoolTestCase("1 == 2", false),
                new EvalBoolTestCase("1 != 2", true),
                new EvalBoolTestCase("true == true", true),
                new EvalBoolTestCase("false == false", true),
                new EvalBoolTestCase("true == false", false),
                new EvalBoolTestCase("true != false", true),
                new EvalBoolTestCase("false != true", true),
                new EvalBoolTestCase("(1 < 2) == true", true),
                new EvalBoolTestCase("(1 < 2) == false", false),
                new EvalBoolTestCase("(1 > 2) == true", false),
                new EvalBoolTestCase("(1 > 2) == false", true)
        );

        for (EvalBoolTestCase testCase : testCases) {
            MonkeyObject monkeyObject = testEval(testCase.input);
            if (testMonkeyBoolean(monkeyObject, testCase.expected)) {
                System.out.println(testCase.input + " = " + testCase.expected + " [Parse]");
            }
        }
    }

    /**
     * 测试逻辑非运算符（!、!!）的求值结果是否正确。
     * <p>
     * 涉及布尔值、整数值的逻辑取反，验证解释器能否正确区分真值与假值。
     */
    public static void testBangOperator() {
        List<EvalBoolTestCase> testCases = Arrays.asList(
                new EvalBoolTestCase("!true", false),
                new EvalBoolTestCase("!false", true),
                new EvalBoolTestCase("!5", false),
                new EvalBoolTestCase("!!true", true),
                new EvalBoolTestCase("!!false", false),
                new EvalBoolTestCase("!!5", true)
        );

        for (EvalBoolTestCase testCase : testCases) {
            MonkeyObject monkeyObject = testEval(testCase.input);
            if (testMonkeyBoolean(monkeyObject, testCase.expected)) {
                System.out.println(testCase.input + " = " + testCase.expected + " [Parse]");
            }
        }
    }

    /**
     * 测试 if-else 表达式的求值结果。
     * <p>
     * 包括条件为 true、false，是否进入 else 分支，以及条件表达式为整数时的隐式真值判断。
     */
    public static void testIfElseExpression() {
        List<EvalIfElseTestCase> testCases = Arrays.asList(
                new EvalIfElseTestCase("if (true) { 10 }", 10),
                new EvalIfElseTestCase("if (false) { 10 }", null),
                new EvalIfElseTestCase("if (1) {10}", 10),
                new EvalIfElseTestCase("if (1 < 2) { 10 }", 10),
                new EvalIfElseTestCase("if (1 > 2) { 10 }", null),
                new EvalIfElseTestCase("if (1 > 2) { 10 } else { 20 }", 20),
                new EvalIfElseTestCase("if (1 < 2) { 10 } else { 20 }", 10)
        );

        for (EvalIfElseTestCase testCase : testCases) {
            MonkeyObject monkeyObject = testEval(testCase.input);
            if (testCase.expected instanceof Integer) {
                if (testMonkeyInteger(monkeyObject, (long)((Integer)(testCase.expected)))) {
                    System.out.println("[Parse]" + testCase.input + " = " + testCase.expected);
                }
            } else {
                if (testNullObject(monkeyObject)) {
                    System.out.println("[Parse]" + testCase.input + " = " + testCase.expected);
                }
            }
        }
    }

    /**
     * 测试 return 语句的求值结果。
     * <p>
     * 验证解释器在函数或语句块中遇到 return 语句时，能否立即返回正确的值。
     */
    public static void testReturnStatement() {
        List<EvalIntTestCase> testCases = Arrays.asList(
                new EvalIntTestCase("return 10;", 10),
                new EvalIntTestCase("return 10; 9", 10),
                new EvalIntTestCase("return 2 * 5; 9;", 10),
                new EvalIntTestCase("9; return 2 * 5; 9;", 10),
                new EvalIntTestCase("if (10 > 1) {\n" +
                        "   if (10 > 1) {\n" +
                        "       return 10;\n" +
                        "   }\n" +
                        "   return 1;\n" +
                        "}", 10),
                new EvalIntTestCase("if (1 > 2) {return 5} else " +
                        "{return 6}", 6)
        );

        for(EvalIntTestCase testCase : testCases) {
            MonkeyObject monkeyObject = testEval(testCase.input);
            if (testMonkeyInteger(monkeyObject, testCase.expected)) {
                System.out.println("[Parse]" + testCase.input + " = " + testCase.expected);
            }
        }
    }

    /**
     * 测试错误处理机制。
     * <p>
     * 包括：
     * <ul>
     *     <li>类型不匹配错误（如整数与布尔值相加）</li>
     *     <li>未知操作符错误</li>
     *     <li>未定义标识符错误</li>
     * </ul>
     */
    public static void testErrorHandling() {
        List<EvalErrorTestCase> testCases = Arrays.asList(
                new EvalErrorTestCase("5 + true;", "type mismatch: INTEGER + BOOLEAN"),
                new EvalErrorTestCase("5 + true; 5;", "type mismatch: INTEGER + BOOLEAN"),
                new EvalErrorTestCase("-true", "unknown operator: -BOOLEAN"),
                new EvalErrorTestCase("true + false;", "unknown operator: BOOLEAN + BOOLEAN"),
                new EvalErrorTestCase("5; true + false; 5", "unknown operator: BOOLEAN + BOOLEAN"),
                new EvalErrorTestCase("if (10 > 1) { true + false; }", "unknown operator: BOOLEAN + BOOLEAN"),
                new EvalErrorTestCase("if (10 > 1) { return true + false; }", "unknown operator: BOOLEAN + BOOLEAN"),
                new EvalErrorTestCase("foobar", "identifier not found: foobar"),
                new EvalErrorTestCase("\"Hello\" - \"World\"", "unknown operator: STRING - STRING"),
                new EvalErrorTestCase("{\"name\": \"Monkey\"}[fn(x) { x }];", "unusable as hash key: FUNCTION")
        );

        for (EvalErrorTestCase testCase : testCases) {
            MonkeyObject monkeyObject = testEval(testCase.input);

            if (!(monkeyObject instanceof MonkeyError)) {
                System.err.println("no error object returned. got=" + monkeyObject.type());
                continue;
            }

            MonkeyError monkeyError = (MonkeyError)monkeyObject;

            if (!monkeyError.getMessage().equals(testCase.expectedMessage)) {
                System.err.println("wrong error message. expected=" +
                        testCase.expectedMessage + ", got=" + monkeyError.getMessage());
            } else {
                System.out.println("[Parse]" + testCase.input + " = " + testCase.expectedMessage);
            }

        }
    }

    /**
     * 测试 let 语句的执行结果。
     * <p>
     * 验证解释器是否能够正确地进行变量声明与赋值，并在后续表达式中正确引用。
     */
    public static void testLetStatement() {
        List<EvalIntTestCase> testCases = Arrays.asList(
                new EvalIntTestCase("let a = 5; a;", 5),
                new EvalIntTestCase("let a = 5 * 5; a;", 25),
                new EvalIntTestCase("let a = 5; let b = a; b;", 5),
                new EvalIntTestCase("let a = 5; let b = a; let c = a + b + 5; c;", 15)
        );

        for (EvalIntTestCase testCase : testCases) {
            if (testMonkeyInteger(testEval(testCase.input), testCase.expected)) {
                System.out.println("[Parse]" + testCase.input + " = " + testCase.expected);
            }
        }
    }

    /**
     * 测试函数对象（Function Object）的解析与构造。
     *
     * <p>该测试验证解释器是否能够正确识别并解析函数定义表达式，
     * 包括参数列表和函数体的内容。例如：</p>
     *
     * <pre>
     * fn(x) { x + 2 };
     * </pre>
     *
     * <p>期望结果：</p>
     * <ul>
     *   <li>生成的对象类型应为 {@link MonkeyFunction}</li>
     *   <li>函数参数应为单个标识符 “x”</li>
     *   <li>函数体应为表达式 “(x + 2)”</li>
     * </ul>
     */
    public static void testFunctionObject() {
        String input = "fn(x) { x + 2 };";

        // 对函数定义表达式求值
        MonkeyObject monkeyObject = testEval(input);
        if (!(monkeyObject instanceof MonkeyFunction)) {
            System.err.println("object is not Function. got=" + monkeyObject.getClass()
                    .getSimpleName());
            return;
        }

        MonkeyFunction fun = (MonkeyFunction)monkeyObject;

        // 验证参数数量是否正确
        List<Identifier> parameters = fun.getParameters();
        if (parameters.size() != 1) {
            System.err.println("function has wrong parameters. Parameters=" +
                    parameters.toString());
            return;
        }

        // 验证参数名是否为 x
        if(!parameters.get(0).toString().equals("x")) {
            System.err.println("parameter is not x. got=" + parameters.get(0).toString());
            return;
        }

        // 验证函数体内容
        String expectedBody = "(x + 2)";

        if (!fun.getBody().toString().equals(expectedBody)) {
            System.err.println("body is not " + expectedBody + "got=" +
                    fun.getBody().toString());
            return;
        }

        System.out.println("[Parse]" + fun.getBody().toString());
    }

    /**
     * 测试函数调用（Function Application）的求值逻辑。
     *
     * <p>本测试验证解释器是否能够正确处理函数调用、参数传递与返回值，
     * 包括匿名函数直接调用、嵌套调用与参数运算。例如：</p>
     *
     * <pre>
     * let add = fn(x, y) { x + y; };
     * add(5 + 5, add(5, 5)); // 期望输出 20
     * </pre>
     *
     * <p>测试覆盖：</p>
     * <ul>
     *     <li>函数变量的定义与调用</li>
     *     <li>return 语句在函数中的执行</li>
     *     <li>多参数函数</li>
     *     <li>匿名函数直接调用</li>
     * </ul>
     */
    public static void testFunctionApplication() {
        List<EvalIntTestCase> testCases = Arrays.asList(
                new EvalIntTestCase("let identity = fn(x) { x; }; " +
                        "identity(5);", 5),
                new EvalIntTestCase("let identity = fn(x) { return x; }; " +
                        "identity(5);", 5),
                new EvalIntTestCase("let double = fn(x) { return x * 2; }; " +
                        "double(5);", 10),
                new EvalIntTestCase("let add = fn(x, y) { x + y; }; " +
                        "add(5, 5);", 10),
                new EvalIntTestCase("let add = fn(x, y) { x + y; }; " +
                        "add(5 + 5, add(5, 5));", 20),
                new EvalIntTestCase("fn (x) { x; }(5)", 5)
        );

        for (EvalIntTestCase testCase : testCases) {
            if(testMonkeyInteger(testEval(testCase.input), testCase.expected)) {
                System.out.println("[Parse]" + testCase.input + " = " + testCase.expected);
            }
        }
    }

    /**
     * 测试闭包（Closure）的求值逻辑。
     *
     * <p>闭包是函数求值中的关键特性。本测试验证解释器能否正确捕获
     * 外层函数的局部变量，并在返回的内部函数中使用该变量。</p>
     *
     * <p>示例：</p>
     * <pre>
     * let newAdder = fn(x) {
     *     fn(y) { x + y };
     * };
     * let addTwo = newAdder(2);
     * addTwo(2); // 期望结果 4
     * </pre>
     *
     * <p>验证内容：</p>
     * <ul>
     *   <li>外层函数执行后返回的内部函数能访问其词法作用域变量</li>
     *   <li>解释器正确处理嵌套环境（Environment）</li>
     * </ul>
     */
    public static void testClosures() {
        String input = "let newAdder = fn(x) {\n" +
                        "   fn(y) { x + y };\n" +
                "};\n" +
                "\n" +
                "let addTwo = newAdder(2);\n" +
                "addTwo(2);";

        if (testMonkeyInteger(testEval(input), 4)) {
            System.out.println("[Parse]" + input + " = " + 4);
        }
    }

    /**
     * 测试字符串字面量（{@code StringLiteral}）在解释执行阶段的求值结果。
     * <p>
     * 本测试验证 Monkey 解释器能否正确将字符串字面量表达式（例如 {@code "Hello World!"}）
     * 解析并求值为对应的 {@link com.linewell.monkey.object.imp.MonkeyString} 对象。
     * </p>
     *
     * <h3>测试流程：</h3>
     * <ol>
     *   <li>输入源码：{@code "Hello World!"}</li>
     *   <li>调用 {@code testEval()} 方法执行完整的词法分析 → 语法解析 → 求值流程。</li>
     *   <li>验证返回结果类型为 {@link com.linewell.monkey.object.imp.MonkeyString}。</li>
     *   <li>检查字符串内容是否与期望值一致。</li>
     * </ol>
     *
     * <h3>预期结果：</h3>
     * <ul>
     *   <li>求值结果类型为 {@code MonkeyString}。</li>
     *   <li>其 {@code value} 属性为 {@code "Hello World!"}。</li>
     *   <li>控制台输出：{@code [Parse]===> Hello World!}</li>
     * </ul>
     *
     * 若类型或内容不匹配，将输出错误信息到标准错误流。
     */
    private static void testStringLiteral() {
        String input = "\"Hello Wrold!\"";

        MonkeyObject evaluted = testEval(input);
        if (!(evaluted instanceof MonkeyString)) {
            System.err.println("object is not MonkeyString. got=" +
                    evaluted.getClass().getSimpleName());
            return;
        }
        MonkeyString monkeyString = (MonkeyString)evaluted;

        if (!monkeyString.getValue().equals("Hello Wrold!")) {
            System.err.println("String has wrong value. got=" +
                    monkeyString.getValue());
            return;
        }
        System.out.println("[Parse]===> " + monkeyString.getValue());
    }

    /**
     * 测试字符串拼接表达式在求值阶段的执行结果。
     * <p>
     * 本测试验证 Monkey 解释器能否正确执行字符串的中缀拼接运算（{@code +}），
     * 并生成新的 {@link com.linewell.monkey.object.imp.MonkeyString} 对象。
     * </p>
     *
     * <h3>测试流程：</h3>
     * <ol>
     *   <li>输入源码：{@code "Hello" + " " + "World!"}</li>
     *   <li>通过 {@code testEval()} 方法执行完整求值流程。</li>
     *   <li>验证结果类型为 {@link com.linewell.monkey.object.imp.MonkeyString}。</li>
     *   <li>检查拼接结果是否为 {@code "Hello World!"}。</li>
     * </ol>
     *
     * <h3>预期结果：</h3>
     * <ul>
     *   <li>解释器正确执行字符串拼接逻辑。</li>
     *   <li>求值结果为新的 {@code MonkeyString("Hello World!")}</li>
     *   <li>控制台输出：{@code [Parse]===> Hello World!}</li>
     * </ul>
     *
     * 若结果类型或内容错误，将输出详细错误信息以便调试。
     */
    private static void testStringConcatenation() {
        String input = "\"Hello\" + \" \" + \"World!\"";

        MonkeyObject evaluated = testEval(input);
        if(!(evaluated instanceof MonkeyString)) {
            System.err.println("object is not MonkeyString. got=" +
                    evaluated.getClass().getSimpleName());
            return;
        }

        MonkeyString str = (MonkeyString)evaluated;

        if (!str.getValue().equals("Hello World!")) {
            System.err.println("String has wrong value. got=" + str.getValue());
        }

        System.out.println("[Parse]===> " + str.getValue());
    }

    /**
     * 测试 Monkey 语言内置函数的求值结果。
     * <p>
     * 当前主要测试 {@code len()} 函数的行为，验证其在不同输入下的返回值或错误信息。
     * </p>
     *
     * <p>测试内容包括：</p>
     * <ul>
     *     <li>空字符串求长度（应返回 0）</li>
     *     <li>普通字符串求长度（如 "four" → 4）</li>
     *     <li>长字符串求长度（"hello world" → 11）</li>
     *     <li>非字符串类型参数（如整数 1，应返回类型错误）</li>
     *     <li>参数数量不正确（如传入两个参数，应返回参数数量错误）</li>
     * </ul>
     *
     * <p>测试逻辑：</p>
     * <ol>
     *     <li>构造一组 {@link EvalIfElseTestCase} 测试用例，包含输入表达式与期望结果。</li>
     *     <li>调用 {@link #testEval(String)} 对每个表达式进行求值。</li>
     *     <li>若期望结果为整数，则验证返回值是否为 {@link MonkeyInteger} 且数值匹配。</li>
     *     <li>若期望结果为字符串，则验证返回对象是否为 {@link MonkeyError} 且错误信息匹配。</li>
     * </ol>
     *
     * <p>
     * 测试输出：
     * 成功的测试用例通过 {@code System.out.println()} 输出，
     * 错误或不匹配的情况通过 {@code System.err.println()} 报告。
     * </p>
     */
    private static void testBuiltinFunction() {
        List<EvalIfElseTestCase> testCases = Arrays.asList(
                new EvalIfElseTestCase("len(\"\")", 0),
                new EvalIfElseTestCase("len(\"four\")", 4),
                new EvalIfElseTestCase("len(\"hello world\")", 11),
                new EvalIfElseTestCase("len(1)",
                        "argument to 'len' not supported got MonkeyInteger"),
                new EvalIfElseTestCase("len(\"one\", \"two\")", "" +
                        "wrong number of aruments.got=2, want=1")
        );

        for (EvalIfElseTestCase testCase : testCases) {
            MonkeyObject monkeyObject = testEval(testCase.input);
            Object expected = testCase.expected;
            if (expected instanceof Integer) {
                if(testMonkeyInteger(monkeyObject, (Integer) expected)) {
                    System.out.println("[Parse] ===> " +
                            testCase.input + " = " + expected);
                }
                continue;
            }

            if (expected instanceof String) {
                if (!(monkeyObject instanceof MonkeyError)) {
                    System.err.println("object is not Error. got=" +
                            monkeyObject.type());
                    continue;
                }

                MonkeyError error = (MonkeyError)monkeyObject;

                if (error.getMessage().equals(expected)) {
                    System.err.println("wrong error message. got=" +
                            expected + " want=" + error.getMessage());
                }
            }
        }
    }

    /**
     * 测试数组字面量（Array Literal）的解析与求值。
     * <p>
     * 该测试用于验证解释器是否能够正确解析并计算数组字面量中的各个表达式。
     * 例如输入：
     * <pre>
     *     [1, 2 * 2, 3 + 3]
     * </pre>
     * 期望结果为：
     * <pre>
     *     [1, 4, 6]
     * </pre>
     * </p>
     *
     * <p>测试逻辑包括：</p>
     * <ul>
     *     <li>检查返回对象是否为 {@link MonkeyArray}</li>
     *     <li>验证数组长度是否正确</li>
     *     <li>验证数组中每个元素的求值结果是否符合预期</li>
     * </ul>
     *
     * <p>该测试主要用于验证：</p>
     * <ul>
     *     <li>数组字面量的语法解析</li>
     *     <li>数组元素中的表达式求值是否正确</li>
     *     <li>数组在解释器中的基本行为是否符合设计</li>
     * </ul>
     */
    private static void testArrayLiteral() {
        String input = "[1, 2 * 2, 3 + 3]";

        MonkeyObject monkeyObject = testEval(input);
        if (!(monkeyObject instanceof MonkeyArray)) {
            System.err.println("object is not Array. got=" + monkeyObject.type());
        }

        MonkeyArray monkeyArray = (MonkeyArray)monkeyObject;

        if (monkeyArray.getElements().length != 3){
            System.err.println("array has wrong num of elementsd. got=" +
                    monkeyArray.getElements().length);
        }

        testMonkeyInteger(monkeyArray.getElements()[0], 1);
        testMonkeyInteger(monkeyArray.getElements()[1], 2 * 2);
        testMonkeyInteger(monkeyArray.getElements()[2], 3 + 3);

        System.out.println("[Parse]===>" + input);
    }

    /**
     * 测试数组索引表达式（Array Index Expression）的解析与求值。
     * <p>
     * 该测试用于验证解释器能否正确处理数组的索引访问操作，
     * 包括索引为常量、表达式、变量引用、越界索引等多种情况。
     * </p>
     *
     * <p>测试用例如下：</p>
     * <ul>
     *     <li>{@code [1, 2, 3][0]} → 1</li>
     *     <li>{@code [1, 2, 3][1 + 1]} → 3</li>
     *     <li>{@code let i = 0; [1][i]} → 1</li>
     *     <li>{@code [1, 2, 3][3]} → null（越界）</li>
     *     <li>{@code [1, 2, 3][-1]} → null（负索引）</li>
     * </ul>
     *
     * <p>测试逻辑包括：</p>
     * <ul>
     *     <li>验证索引表达式能否正确返回对应元素</li>
     *     <li>验证表达式型索引（如 {@code 1 + 1}）是否能被正确计算</li>
     *     <li>验证越界或负索引情况下是否返回 {@code NULL}</li>
     * </ul>
     *
     * <p>该测试覆盖了解释器中索引运算的主要场景，是数组求值功能的重要验证部分。</p>
     */
    private static void testArrayIndexExpressions() {
        List<EvalIfElseTestCase> testCases = Arrays.asList(
                new EvalIfElseTestCase("[1, 2, 3][0];", 1),
                new EvalIfElseTestCase("[1, 2, 3][1];", 2),
                new EvalIfElseTestCase("[1, 2, 3][2];", 3),
                new EvalIfElseTestCase("let i = 0; [1][i]", 1),
                new EvalIfElseTestCase("[1, 2, 3][1 + 1]", 3),
                new EvalIfElseTestCase("let myArray = [1, 2, 3]; myArray[2]", 3),
                new EvalIfElseTestCase("let myArray = [1, 2, 3]; " +
                        "myArray[0] + myArray[1] + myArray[2]", 6),
                new EvalIfElseTestCase("let myArray = [1, 2, 3]; let i = myArray[0];" +
                        "myArray[i]", 2),
                new EvalIfElseTestCase("[1, 2, 3][3]", null),
                new EvalIfElseTestCase("[1, 2, 3][-1]", null)
        );

        for (EvalIfElseTestCase testCase : testCases) {
            MonkeyObject evalted = testEval(testCase.input);
            Object integer = testCase.expected;
            if (integer != null) {
                testMonkeyInteger(evalted, (Integer) integer);
            } else {
                testNullObject(evalted);
            }
            System.out.println("[Parse]===>" + testCase.input + " = " +
                    integer);
        }

    }

    /**
     * 测试 Monkey 语言中哈希字面量（Hash Literal）的解析与求值。
     *
     * <p>该测试验证解释器是否能够正确地：</p>
     * <ul>
     *     <li>对哈希结构中的键和值表达式进行求值；</li>
     *     <li>支持不同类型的键（字符串、整数、布尔值）；</li>
     *     <li>确保哈希中键的 {@link HashKey} 计算一致性；</li>
     *     <li>生成包含正确键值对的 {@link MonkeyHash} 对象。</li>
     * </ul>
     *
     * <p>测试输入示例：</p>
     * <pre>{@code
     * let two = "two";
     * {
     *     "one": 10 - 9,
     *     two: 1 + 1,
     *     "thr" + "ee": 6 / 2,
     *     4: 4,
     *     true: 5,
     *     false: 6,
     * }
     * }</pre>
     *
     * <p>预期求值结果：</p>
     * <ul>
     *     <li>"one" → 1</li>
     *     <li>"two" → 2</li>
     *     <li>"three" → 3</li>
     *     <li>4 → 4</li>
     *     <li>true → 5</li>
     *     <li>false → 6</li>
     * </ul>
     *
     * <p>若求值结果不是 {@link MonkeyHash}，或生成的键值对数量或值不匹配，
     * 则打印错误信息。</p>
     */
    private static void testHashLiterals() {
        String input = "let two = \"two\";\n" +
                "\t{\n" +
                "\t\t\"one\": 10 - 9,\n" +
                "\t\ttwo: 1 + 1,\n" +
                "\t\t\"thr\" + \"ee\": 6 / 2,\n" +
                "\t\t4 : 4,\n" +
                "\t\ttrue: 5,\n" +
                "        false: 6,\n" +
                "\t}";

        MonkeyObject evlauated = testEval(input);
        if (!(evlauated instanceof MonkeyHash)) {
            System.err.println("Eval didn't return Hash. got=" +
                    evlauated.type());
            return;
        }

        MonkeyHash result = (MonkeyHash) evlauated;

        Map<HashKey, Long> expected = new HashMap<>();
        expected.put(new MonkeyString("one").HashKey(), 1l);
        expected.put(new MonkeyString("two").HashKey(), 2l);
        expected.put(new MonkeyString("three").HashKey(), 3l);
        expected.put(new MonkeyInteger(4).HashKey(), 4l);
        expected.put(TRUE.HashKey(), 5l);
        expected.put(FALSE.HashKey(), 6l);

        if (result.getPairs().size() != expected.size()) {
            System.err.println("Hash has wrong num of pairs. got=" +
                    result.getPairs().size());
            return;
        }

        for (Map.Entry<HashKey, Long> entry : expected.entrySet()) {
            HashPair hashPair = result.getPairs().get(entry.getKey());
            if (hashPair == null) {
                System.err.println("no pair for given key in Pairs");
            }

            testMonkeyInteger(hashPair.getValue(), entry.getValue());
        }

        System.out.println("[Parse] ===> " + input);

    }

    /**
     * 测试 Monkey 语言中哈希索引表达式（Hash Index Expression）的求值。
     *
     * <p>该测试验证解释器是否能正确解析并求值形如
     * {@code {"key": value}[index]} 的哈希索引语句。</p>
     *
     * <p>测试内容包括：</p>
     * <ul>
     *     <li>通过字符串键访问哈希值（存在键 / 不存在键）；</li>
     *     <li>使用变量作为键访问哈希值；</li>
     *     <li>访问空哈希表；</li>
     *     <li>使用不同类型的键（整数、布尔）进行索引。</li>
     * </ul>
     *
     * <p>示例测试用例：</p>
     * <pre>{@code
     * {"foo": 5}["foo"]         // => 5
     * {"foo": 5}["bar"]         // => null
     * let key = "foo"; {"foo": 5}[key] // => 5
     * {}["foo"]                 // => null
     * {5: 5}[5]                 // => 5
     * {true: 5}[true]           // => 5
     * {false: 5}[false]         // => 5
     * }</pre>
     *
     * <p>若求值结果与预期不符，将打印错误信息；
     * 否则在控制台输出对应解析过程的日志。</p>
     */
    private static void testHashIndexExpressions() {
        List<EvalIfElseTestCase> testCases = Arrays.asList(
                new EvalIfElseTestCase("{\"foo\": 5}[\"foo\"]", 5L),
                new EvalIfElseTestCase("{\"foo\": 5}[\"bar\"]", null),
                new EvalIfElseTestCase("let key = \"foo\"; {\"foo\": 5}[key]", 5L),
                new EvalIfElseTestCase("{}[\"foo\"]", null),
                new EvalIfElseTestCase("{5: 5}[5]", 5L),
                new EvalIfElseTestCase("{true: 5}[true]", 5L),
                new EvalIfElseTestCase("{false: 5}[false]", 5L)
        );

        for (EvalIfElseTestCase testCase : testCases) {
            MonkeyObject evaluated = testEval(testCase.input);
            Object expected = testCase.expected;
            if (expected != null) {
                testMonkeyInteger(evaluated, (Long) expected);
            } else {
                testNullObject(evaluated);
            }
            System.out.println("[Parse] ===> " + testCase.input + " = " +
                    expected);
        }
    }

    /**
     * 测试 {@code quote(...)} 表达式的求值结果。
     * <p>
     * 该方法用于验证解释器在处理 {@code quote} 表达式时，
     * 是否能够正确生成对应的 {@link com.linewell.monkey.object.imp.MonkeyQuote} 对象，
     * 并确保其内部封装的 AST 节点字符串表示与预期一致。
     * </p>
     *
     * <p><b>测试目标：</b></p>
     * <ul>
     *   <li>确认 {@code quote(expr)} 返回的对象类型为 {@link MonkeyQuote}。</li>
     *   <li>验证 {@link MonkeyQuote#getNode()} 不为 {@code null}。</li>
     *   <li>比较节点的字符串输出（{@code node.toString()}）与期望值是否一致。</li>
     * </ul>
     *
     * <p><b>测试示例：</b></p>
     * <pre>
     * quote(5)              → QUOTE(5)
     * quote(5 + 8)          → QUOTE((5 + 8))
     * quote(foobar)         → QUOTE(foobar)
     * quote(foobar + barfoo)→ QUOTE((foobar + barfoo))
     * </pre>
     *
     * <p><b>输出：</b></p>
     * <ul>
     *   <li>若测试通过：在控制台打印形如 {@code [Parse] ===>quote(5 + 8) = (5 + 8)} 的提示。</li>
     *   <li>若测试失败：输出错误信息，标明期望值与实际结果。</li>
     * </ul>
     *
     * <p>说明：该测试方法通常在解释器功能测试阶段执行，用于验证宏系统的基础特性。</p>
     */
    private static void testQuote() {
        List<QuoteTestCase> quoteTestCases = Arrays.asList(
                new QuoteTestCase("quote(5)", "5"),
                new QuoteTestCase("quote(5 + 8)", "(5 + 8)"),
                new QuoteTestCase("quote(foobar)", "foobar"),
                new QuoteTestCase("quote(foobar + barfoo)",
                        "(foobar + barfoo)")
        );

        for (QuoteTestCase testCase : quoteTestCases) {
            MonkeyObject monkeyObject = testEval(testCase.input);
            if (!(monkeyObject instanceof MonkeyQuote)) {
                System.err.println("expected *object.Quote. got=" +
                        monkeyObject.type());
                return;
            }

            MonkeyQuote quote = (MonkeyQuote) monkeyObject;

            if (quote.getNode() == null) {
                System.err.println(testCase.input + "quote Node is null");
                return;
            }

            if (!quote.getNode().toString().equals(testCase.expected)) {
                System.err.println("not equal. got=" + quote.getNode() +
                        "want=" + testCase.expected);
                return;
            }

            System.out.println("[Parse] ===>" + testCase.input + " = "
                    + testCase.expected);
        }
    }
    /**
     * 测试 {@code quote(...)} 与 {@code unquote(...)} 组合使用时的语义与求值行为。
     * <p>
     * 本方法验证 Monkey 语言中宏系统（Macro System）的核心机制：
     * <ul>
     *   <li>{@code quote(expr)}：在求值阶段不执行 {@code expr}，而是返回其对应的抽象语法树（AST）。</li>
     *   <li>{@code unquote(expr)}：在 {@code quote(...)} 内部使用时，会在当前环境中对 {@code expr} 求值，
     *       并将结果嵌入回 AST 中。</li>
     * </ul>
     * <p>
     * 每个测试用例由输入的 Monkey 源代码字符串和期望输出的 AST 字符串表示，
     * 测试目标是确保 {@link MonkeyQuote} 对象中封装的 AST 与预期一致。
     * </p>
     * <p>
     * 例如：
     * <pre>
     * quote(unquote(4 + 4))        → 8
     * quote(8 + unquote(4 + 4))    → (8 + 8)
     * quote(unquote(quote(4 + 4))) → (4 + 4)
     * </pre>
     * </p>
     * 测试逻辑：
     * <ol>
     *   <li>调用 {@code testEval(input)} 解析并执行输入程序。</li>
     *   <li>断言返回值为 {@link MonkeyQuote} 类型。</li>
     *   <li>比较 {@link MonkeyQuote#getNode()} 的字符串形式与预期结果是否一致。</li>
     * </ol>
     * <p>
     * 若结果不匹配，则在控制台输出错误提示；否则打印成功的解析结果。
     * </p>
     *
     * @see com.linewell.monkey.object.imp.MonkeyQuote
     * @see quote(Node, Environment) 方法：生成 MonkeyQuote 对象
     * @see evalUnquotedCalls(Node, Environment) 方法：处理 unquote 调用
     */
    private static void testQuoteUnquote() {
        List<QuoteTestCase> quoteTestCases = Arrays.asList(
                new QuoteTestCase("quote(unquote(4))", "4"),
                new QuoteTestCase("quote(unquote(4 + 4))", "8"),
                new QuoteTestCase("quote(8 + unquote(4 + 4))", "(8 + 8)"),
                new QuoteTestCase("quote(unquote(4 + 4) + 8)", "(8 + 8)"),
                new QuoteTestCase("let foobat = 8 \n" +
                        "quote(foobat)", "foobat"),
                new QuoteTestCase("let foooba = 8;\n" +
                        "\t\t\t\t\tquote(unquote(foooba))", "8"),
                new QuoteTestCase("quote(unquote(true))",
                        "true"),
                new QuoteTestCase("quote(unquote(true == false))",
                        "false"),
                new QuoteTestCase("quote(unquote(quote(4 + 4)))",
                        "(4 + 4)"),
                new QuoteTestCase("let quotedInfixExpression = quote(4 + 4);\n" +
                        "\t\t\t\t\tquote(unquote(4 + 4) + unquote(quotedInfixExpression))",
                        "(8 + (4 + 4))")
        );

        for (QuoteTestCase testCase : quoteTestCases) {
            MonkeyObject monkeyObject = testEval(testCase.input);
            if (!(monkeyObject instanceof MonkeyQuote)) {
                System.err.println("expxted MonkeyQuote. got=" +
                        monkeyObject.type());
                return;
            }

            MonkeyQuote quote = (MonkeyQuote) monkeyObject;

            if (quote.getNode() == null) {
                System.err.println("quote.getNode is null");
                return;
            }

            if (!quote.getNode().toString().equals(testCase.expected)) {
                System.err.println("not equal. got=" + quote.getNode().toString() +
                        ", want=" + testCase.expected);
            }

            System.out.println("[Parse] ===> " + testCase.input + " = " +
                    testCase.expected);
        }

    }

    /**
     * 将输入的表达式字符串进行词法分析、语法分析，然后交给求值器执行。
     *
     * @param input Monkey 源代码字符串
     * @return 表达式求值后的结果（MonkeyObject）
     */
    private static MonkeyObject testEval(String input) {
        // 词法分析器：把字符串分割成 Token
        Lexer lexer = new Lexer(input);
        // 语法分析器：把 Token 转换为 AST
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        // 执行环境（保存变量、函数等）
        Environment env = new Environment();

        // 调用解释器进行求值
        return Evaluator.eval(program, env);
    }

    /**
     *  辅助测试方法
     * 验证求值结果是否为整数，并且值等于期望值。
     *
     * @param obj      求值结果对象
     * @param expected 期望的整数值
     * @return 如果类型和值都正确返回 true，否则打印错误信息并返回 false
     */
    private static boolean testMonkeyInteger(MonkeyObject obj, long expected) {
        // 检查类型是否是整数
        if (!(obj instanceof MonkeyInteger)) {
            System.err.println("monkey object is not Integer. got=" +
                    obj.getClass().getSimpleName());
            return false;
        }

        MonkeyInteger result = (MonkeyInteger) obj;

        // 检查值是否与期望相等
        if (result.getValue() != expected) {
            System.err.println("monkey integer has wrong value. got=" +
                    result.getValue() + ", want=" + expected);
            return false;
        }

        return true;
    }

    /**
     * 验证求值结果是否为布尔值，并且值等于期望值。
     *
     * @param obj      求值结果对象
     * @param expected 期望的布尔值
     * @return 如果类型和值都正确返回 true，否则打印错误信息并返回 false
     */
    public static boolean testMonkeyBoolean(MonkeyObject obj, boolean expected) {
        if (!(obj instanceof MonkeyBoolean)) {
            System.err.println("monkey object is not Boolean. got=" +
                    obj.getClass().getSimpleName());
            return false;
        }

        MonkeyBoolean result = (MonkeyBoolean) obj;

        if (result.isValue() != expected) {
            System.err.println("monkey boolean has wrong value. got=" +
                    result.isValue() + ", want=" + expected);
            return  false;
        }
        return true;
    }

    /**
     * 验证求值结果是否为 Null 对象。
     * <p>
     * 在 Monkey 语言中，条件不满足或 if-else 没有返回值时，
     * 解释器会返回一个单例 {@link MonkeyNull#NULL} 对象。
     * 本方法用于检查结果对象是否等于该单例。
     *
     * @param obj 求值结果对象
     * @return 如果对象为 MonkeyNull.NULL 返回 true，否则打印错误信息并返回 false
     */
    public static boolean testNullObject(MonkeyObject obj) {
        if (obj != MonkeyNull.NULL) {
            System.err.println("object is not NULL. got=" + obj.type());
            return false;
        }
        return true;
    }

}

/**
 * 用于描述整数表达式测试用例的数据类。
 * <p>
 * 包含一个输入表达式（字符串）和期望的计算结果（long 类型）。
 */
class EvalIntTestCase {
    // 输入的表达式
    public String input;
    // 期望的求值结果
    public long expected;

    public EvalIntTestCase(String input, long expected) {
        this.input = input;
        this.expected = expected;
    }
}

/**
 * 用于描述布尔表达式测试用例的数据类。
 * <p>
 * 包含一个输入表达式（字符串）和期望的计算结果（boolean 类型）。
 */
class EvalBoolTestCase {
    public String input;
    public boolean expected;

    public EvalBoolTestCase(String input, boolean expected) {
        this.input = input;
        this.expected = expected;
    }
}

/**
 * 用于描述 if-else 表达式测试用例的数据类。
 * <p>
 * 输入为一段 Monkey 语言 if-else 语句，
 * 期望值可以是整数（进入分支时返回的值）或 null（不进入任何分支）。
 */
class EvalIfElseTestCase {
    public String input;
    public Object expected;

    public EvalIfElseTestCase(String input, Object expected) {
        this.input = input;
        this.expected = expected;
    }
}

/**
 * 用于描述错误处理测试用例的数据类。
 * <p>
 * 输入为一段可能触发错误的 Monkey 代码，
 * 期望结果是解释器返回的错误消息字符串。
 */
class EvalErrorTestCase {
    public String input;
    public String expectedMessage;

    public EvalErrorTestCase(String input, String expectedMessage) {
        this.input = input;
        this.expectedMessage = expectedMessage;
    }
}

/**
 * {@code quote(...)} 表达式的测试用例类。
 * <p>
 * 用于封装单个 {@code quote} 测试案例的输入与预期输出，
 * 以便在批量测试中逐一验证解释器的求值结果。
 * </p>
 *
 * <p><b>字段说明：</b></p>
 * <ul>
 *   <li>{@link #input} —— 待求值的 {@code quote(...)} 表达式源代码。</li>
 *   <li>{@link #expected} —— 预期的 AST 节点字符串表示（即 {@code quote} 返回对象的 {@code node.toString()}）。</li>
 * </ul>
 *
 * <p><b>示例：</b></p>
 * <pre>
 * QuoteTestCase("quote(5 + 8)", "(5 + 8)")
 * QuoteTestCase("quote(foobar)", "foobar")
 * </pre>
 *
 * <p>说明：该类通常与 {@link EvaluatorTest#testQuote()} 搭配使用，用于单元测试解释器的宏系统基础功能。</p>
 *
 * @see com.linewell.monkey.object.imp.MonkeyQuote
 * @see EvaluatorTest#testQuote()
 */
class QuoteTestCase {
    public String input;
    public String expected;

    public QuoteTestCase(String input, String expected) {
        this.input = input;
        this.expected = expected;
    }
}