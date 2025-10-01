package com.linewell.monkey.evaluator;

import com.linewell.monkey.ast.imp.Program;
import com.linewell.monkey.lexer.Lexer;
import com.linewell.monkey.object.Environment;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.imp.MonkeyInteger;
import com.linewell.monkey.parser.Parser;

import java.util.Arrays;
import java.util.List;

/**
 * 对 {@link Evaluator} 进行单元测试的类。
 * <p>
 * 当前实现了整数表达式求值的测试，包括加减乘除、括号优先级、前缀表达式等。
 */
public class EvaluatorTest {

    public static void main(String[] args) {
        // 执行整数表达式的测试
        testEvalIntegerExpression();
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
