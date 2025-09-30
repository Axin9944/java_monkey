package com.linewell.monkey.evaluator;

import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.IfExpression;
import com.linewell.monkey.object.Environment;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;
import com.linewell.monkey.object.imp.*;

import java.util.List;

/**
 * 解释器核心类，负责对 AST 节点进行求值 (evaluation)，
 * 包含各种表达式与语句的执行逻辑。
 * <p>
 * 主要功能包括：
 * <ul>
 *     <li>顺序执行语句列表</li>
 *     <li>处理前缀、后缀运算符</li>
 *     <li>执行 if 表达式</li>
 *     <li>处理整数运算、布尔运算、错误处理等</li>
 * </ul>
 */
public class Evaluator {

    /** 全局共享的布尔真值对象 */
    private static final MonkeyBoolean TRUE = MonkeyBoolean.TRUE;

    /** 全局共享的布尔假值对象 */
    private static final MonkeyBoolean FALSE = MonkeyBoolean.FALSE;

    /** 全局共享的空对象 */
    private static final MonkeyNull NULL = MonkeyNull.NULL;

    //TODO 等待其他方法编写完毕
    public static MonkeyObject eval(Node node, Environment env) {
        return null;
    }

    /**
     * 在指定环境中依次执行一组语句。
     * <p>
     * 本方法会按顺序执行传入的语句列表，并将最后一个语句的执行结果作为返回值。
     * 如果在执行过程中遇到 {@link MonkeyReturn}，则会立即返回其中封装的值，
     * 后续语句将不会再被执行。
     * </p>
     *
     * @param statements 需要执行的语句列表
     * @param env        执行语句时所使用的环境（变量、作用域等信息）
     * @return 最后一个语句的执行结果；如果遇到 {@link MonkeyReturn}，则返回其中的值
     */
    private static MonkeyObject evalStatements(List<Statement> statements,
                                               Environment env) {
        MonkeyObject result = null;

        for (Statement statement : statements) {
            result = eval(statement, env);

            if (result instanceof MonkeyReturn) {
                return ((MonkeyReturn) result).getValue();
            }
        }

        return result;
    }

    /**
     * 将 Java 原生 boolean 转换为 Monkey 语言中的布尔对象。
     *
     * @param input 原生布尔值
     * @return MonkeyBoolean.TRUE 或 MonkeyBoolean.FALSE
     */
    private static MonkeyBoolean nativeBoolToMonkeyBoolean(boolean input) {
        return input ? TRUE : FALSE;
    }

    /**
     * 处理前缀表达式。
     *
     * @param operator 前缀运算符，如 "!" 或 "-"
     * @param right    运算符右侧的表达式求值结果
     * @return 运算结果对象
     */
    private static MonkeyObject evalPrefixExpression(String operator,
                                                     MonkeyObject right) {
        switch (operator) {
            case "!" :
                // 逻辑非运算，例如：!true -> false
                return evalBangOperatorExpression(right);
            case "-" :
                // 数值取负运算，例如：-5 -> -5
                return evalMinusPrefixOperatorExpression(right);
            default:
                // 未知前缀运算符
                return newError("unknown operator: " + operator + right.type());
        }
    }

    /**
     * 处理 "!"（逻辑非）运算。
     *
     * @param right 运算对象
     * @return 取反后的布尔值
     */
    private static MonkeyObject evalBangOperatorExpression(MonkeyObject right) {
        if (right == TRUE) {
            return FALSE;
        } else if (right == FALSE) {
            return TRUE;
        } else if (right == NULL) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    /**
     * 处理负号（取反）运算。
     *
     * @param right 运算对象
     * @return 若对象为整数则返回其相反数，否则返回错误
     */
    private static MonkeyObject evalMinusPrefixOperatorExpression(MonkeyObject right) {
        if (right.type() != ObjectType.INTEGER_OBJ) {
            return newError("unknown operator: -" + right.type());
        }

        long value = ((MonkeyInteger) right).getValue();
        return new MonkeyInteger(-value);
    }

    /**
     * 处理中缀运算符（如 +, -, *, /, ==, != 等）。
     *
     * @param operator 运算符
     * @param left     左操作数
     * @param right    右操作数
     * @return 运算结果或错误
     */
    private static MonkeyObject evalInfixExpression(String operator,
                                                    MonkeyObject left, MonkeyObject right) {
        if ((left.type() == ObjectType.INTEGER_OBJ) &&
                (right.type() == ObjectType.INTEGER_OBJ)) {
            // 两边都是整数，进入整数运算分支
            return evalIntegerInfixExpression(operator, left, right);
        } else if (operator.equals("==")) {
            // 判断相等（适用于非整数类型，例如布尔值、null）
            return nativeBoolToMonkeyBoolean(left.equals(right));
        } else if (operator.equals("!=")) {
            // 判断不相等
            return nativeBoolToMonkeyBoolean(!left.equals(right));
        } else if (left.type() != right.type()) {
            // 类型不一致时报错，例如 true + 5
            return newError("type mismatch: " + left.type() + operator + right.type());
        } else {
            // 其他情况一律视为未知运算符
            return newError("unknown operator: " + left.type() + operator + right.type());
        }
    }

    /**
     * 针对整数的中缀运算。
     *
     * @param operator 运算符
     * @param left     左整数对象
     * @param right    右整数对象
     * @return 运算结果或错误
     */
    private static MonkeyObject evalIntegerInfixExpression(String operator,
                                                           MonkeyObject left,
                                                           MonkeyObject right) {
        long leftValue = ((MonkeyInteger) left).getValue();
        long rightValue = ((MonkeyInteger) right).getValue();

        switch (operator) {
            case "+" :
                return new MonkeyInteger(leftValue + rightValue);
            case "-" :
                return new MonkeyInteger(leftValue - rightValue);
            case "*" :
                return new MonkeyInteger(leftValue * rightValue);
            case "/" :
                return new MonkeyInteger(leftValue / rightValue);
            case "%" :
                return new MonkeyInteger(leftValue % rightValue);
            case "<" :
                return nativeBoolToMonkeyBoolean(leftValue < rightValue);
            case ">" :
                return nativeBoolToMonkeyBoolean(leftValue > rightValue);
            case "==" :
                return nativeBoolToMonkeyBoolean(leftValue == rightValue);
            case "!=" :
                return nativeBoolToMonkeyBoolean(leftValue != rightValue);
            default:
                return newError("unknown operator: " + left.type() + operator + right.type());
        }
    }

    /**
     * 执行 if 表达式。
     *
     * @param ie  if 表达式节点
     * @param env 执行环境
     * @return if 分支结果或 NULL
     */
    private static MonkeyObject evalIfExpression(IfExpression ie, Environment env) {
        MonkeyObject condition = eval(ie.getCondition(), env);
        if (isError(condition)) {
            return condition;
        }

        if (isTruthy(condition)) {
            return eval(ie.getConsequence(), env);
        } else if (ie.getAlternative() != null) {
            return eval(ie.getAlternative(), env);
        } else {
            return NULL;
        }
    }

    /**
     * 判断对象的真值性。
     * <p>
     * 规则：
     * <ul>
     *     <li>null 为 false</li>
     *     <li>true 为 true</li>
     *     <li>false 为 false</li>
     *     <li>其他对象都视为 true</li>
     * </ul>
     *
     * @param obj 待判断对象
     * @return 是否为真
     */
    private static boolean isTruthy(MonkeyObject obj) {
        if (obj == NULL) {
            return false;
        } else if (obj == TRUE) {
            return true;
        } else if (obj == FALSE) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断对象是否为错误对象。
     *
     * @param obj 待检查对象
     * @return 是否为错误类型
     */
    private static boolean isError(MonkeyObject obj) {
        if (obj != null) {
            return obj.type() == ObjectType.ERROR_OBJ;
        }
        return false;
    }

    /**
     * 创建一个新的错误对象。
     *
     * @param message 错误信息
     * @return 错误对象
     */
    private static MonkeyError newError(String message) {
        return new MonkeyError(message);
    }
}
