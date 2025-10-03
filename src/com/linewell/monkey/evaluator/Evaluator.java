package com.linewell.monkey.evaluator;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.*;
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

    /**
     * 对给定的 AST 节点在指定环境中进行求值。
     * <p>
     * 本方法是求值器的核心入口，根据节点的类型不同，分发到对应的处理逻辑。
     * 支持的节点类型包括：
     * <ul>
     *     <li>Program（程序根节点）</li>
     *     <li>ExpressionStatement（表达式语句）</li>
     *     <li>IntegerLiteral（整数字面量）</li>
     *     <li>BooleanType（布尔字面量）</li>
     *     <li>PrefixExpression（前缀表达式）</li>
     *     <li>InfixExpression（中缀表达式）</li>
     *     <li>BlockStatement（语句块）</li>
     *     <li>IfExpression（if 表达式）</li>
     *     <li>ReturnStatement（return 语句）</li>
     *     <li>LetStatement（let 语句）</li>
     *     <li>Identifier（标识符）</li>
     * </ul>
     *
     * @param node AST 节点
     * @param env  当前环境，包含变量和函数的绑定
     * @return 节点求值后的结果对象（MonkeyObject），可能是值、错误或控制流对象
     */
    public static MonkeyObject eval(Node node, Environment env) {

        // 如果是整个程序（根节点），交给 evalProgram 处理
        if (node instanceof Program) {
            return evalProgram((Program) node, env);
        }

        // 如果是表达式语句（形如 "1 + 2;"），直接对表达式部分求值
        if (node instanceof ExpressionStatement) {
            return eval(((ExpressionStatement) node).getExpression(), env);
        }

        // 整数字面量，直接返回对应的 MonkeyInteger
        if (node instanceof IntegerLiteral) {
            return new MonkeyInteger(((IntegerLiteral) node).getValue());
        }

        // 布尔字面量，转换为 Monkey 内部的布尔对象
        if (node instanceof BooleanType) {
            return nativeBoolToMonkeyBoolean(((BooleanType) node).isValue());
        }

        // 前缀表达式（例如 !true, -5）
        if (node instanceof PrefixExpression) {
            PrefixExpression ast =  (PrefixExpression) node;
            // 先对右操作数求值
            MonkeyObject right = eval(ast.getRight(), env);
            if (isError(right)) {
                // 如果右操作数出错，直接返回错误
                return right;
            }
            return evalPrefixExpression(ast.getOperator(), right);
        }

        // 中缀表达式（例如 1 + 2, true == false）
        if (node instanceof InfixExpression) {
            InfixExpression ast = (InfixExpression) node;
            // 先求值左操作数
            MonkeyObject left = eval(ast.getLeft(), env);
            if (isError(left)) {
                return left;
            }

            // 再求值右操作数
            MonkeyObject right = eval(ast.getRight(), env);
            if (isError(right)) {
                return right;
            }
            return evalInfixExpression(ast.getOperator(), left, right);
        }

        // 语句块（由多条语句组成的 {} 块）
        if (node instanceof BlockStatement) {
            return evalBlockStatement(((BlockStatement) node), env);
        }

        // if 表达式
        if (node instanceof IfExpression) {
            return evalIfExpression(((IfExpression) node), env);
        }

        // return 语句
        if (node instanceof ReturnStatement) {
            // 先对 return 的值部分求值
            MonkeyObject val = eval(((ReturnStatement) node).getReturnValue(), env);
            if (isError(val)) {
                return val;
            }
            // 用 MonkeyReturn 包装，交给上层逻辑处理控制流
            return new MonkeyReturn(val);
        }

        // let 语句（变量定义）
        if (node instanceof LetStatement) {
            LetStatement letStmt = (LetStatement) node;
            MonkeyObject val = eval(letStmt.getExpression(), env);
            if (isError(val)) {
                return val;
            }
            // 将结果绑定到环境中
            env.set(letStmt.getName().getValue(), val);
        }

        // 标识符（变量或函数名）
        if (node instanceof Identifier) {
            return evalIdentifier((Identifier) node, env);
        }

        // 未处理的情况，返回 null（表示不支持该节点）
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
     * @return MonkeyBoolean.TRUE 或 MonkeyBoolean. FALSE
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
            return newError("type mismatch: " + left.type() + " " +
                    operator + " " + right.type());
        } else {
            // 其他情况一律视为未知运算符
            return newError("unknown operator: " + left.type() + " " +
                    operator + " " + right.type());
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
                return newError("unknown operator: " + left.type() + " "
                        + operator + " " + right.type());
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
     * 在指定环境中对标识符节点进行求值。
     * <p>
     * 方法会从环境中查找该标识符对应的值。
     * 如果标识符未定义，则返回一个错误对象。
     *
     * @param node 标识符 AST 节点
     * @param env  当前的环境，包含变量绑定信息
     * @return 标识符对应的值，如果未找到则返回错误对象
     */
    private static MonkeyObject evalIdentifier(Identifier node, Environment env) {
        MonkeyObject monkeyObject = env.get(node.getValue());
        if (monkeyObject == null) {
            return newError("identifier not found: " + node.getValue());
        }

        return monkeyObject;
    }

    /**
     * 在指定环境中对语句块节点进行求值。
     * <p>
     * 方法会按顺序依次执行语句块中的所有语句。
     * 如果遇到 return 语句或错误对象，则立即返回，不再继续执行后续语句。
     *
     * @param block 语句块 AST 节点
     * @param env   当前的环境，包含变量绑定信息
     * @return 最后一个语句的执行结果，或者在执行过程中遇到的 return/错误对象
     */
    private static MonkeyObject evalBlockStatement(BlockStatement block,
                                                   Environment env) {
        MonkeyObject result = null;

        List<Statement> statements = block.getStatements();

        for (Statement statement : statements) {
            result = eval(statement, env);

            if (result != null) {
                ObjectType rt = result.type();
                if ((rt == ObjectType.RETURN_VALUE_OBJ) ||
                        (rt == ObjectType.ERROR_OBJ)) {
                    return result;
                }
            }
        }

        return result;
    }

    /**
     * 在指定环境中对程序节点（AST 根节点）进行求值。
     * <p>
     * 方法会按顺序依次执行程序中的所有顶层语句。
     * 如果遇到 return 语句，则返回其内部的值；
     * 如果遇到错误对象，则立即返回该错误对象。
     *
     * @param program 程序 AST 节点，表示整个 Monkey 程序
     * @param env     当前的环境，包含变量绑定信息
     * @return 程序执行的结果；如果遇到 return，则返回其值；如果遇到错误对象，则返回错误对象
     */
    private static MonkeyObject evalProgram(Program program, Environment env) {
        MonkeyObject result = null;

        List<Statement> statements = program.getStatements();
        for (Statement statement : statements) {
            result = eval(statement, env);

            if (result instanceof MonkeyReturn) {
                return ((MonkeyReturn) result).getValue();
            }

            if (result instanceof MonkeyError) {
                return result;
            }
        }

        return result;
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
