package com.linewell.monkey.evaluator;

import com.linewell.monkey.ast.imp.Identifier;
import com.linewell.monkey.ast.imp.Program;
import com.linewell.monkey.lexer.Lexer;
import com.linewell.monkey.object.Environment;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.imp.*;
import com.linewell.monkey.parser.Parser;

import java.util.Arrays;
import java.util.List;

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
                new EvalErrorTestCase("foobar", "identifier not found: foobar")
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