package com.linewell.monkey.evaluator;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.*;
import com.linewell.monkey.object.Environment;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;
import com.linewell.monkey.object.imp.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 解释器核心类，负责对 AST 节点进行求值 (evaluation)，
 * 包含表达式、语句及函数调用等的执行逻辑。
 *
 * <p>支持的语言特性包括：</p>
 * <ul>
 *     <li>整数与布尔字面量</li>
 *     <li>算术运算与比较运算</li>
 *     <li>逻辑前缀表达式（!、-）</li>
 *     <li>if 表达式</li>
 *     <li>let 变量声明与赋值</li>
 *     <li>函数定义（fn）与函数调用</li>
 *     <li>闭包（函数携带其定义时的外层环境）</li>
 *     <li>return 控制流</li>
 *     <li>错误传播机制</li>
 *     <li>字符串类型</li>
 * </ul>
 *
 * <p>求值过程以 {@link #eval(Node, Environment)} 为入口，
 * 根据 AST 节点类型进行分派处理。</p>
 *
 * @author axin
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
     *     <li>{@link Program}：（程序根节点）</li>
     *     <li>{@link ExpressionStatement}：（表达式语句）</li>
     *     <li>{@link IntegerLiteral}：（整数字面量）</li>
     *     <li>{@link BooleanType}：（布尔字面量）</li>
     *     <li>{@link PrefixExpression}：（前缀表达式）</li>
     *     <li>{@link InfixExpression}：（中缀表达式）</li>
     *     <li>{@link BlockStatement}（语句块）</li>
     *     <li>{@link IfExpression}：（if 表达式）</li>
     *     <li>{@link ReturnStatement}：（return 语句）</li>
     *     <li>{@link LetStatement}：（let 语句）</li>
     *     <li>{@link Identifier}：（标识符）</li>
     *     <li>{@link FunctionLiteral}：函数定义（fn）</li>
     *     <li>{@link CallExpression}：函数调用（fn(...)）</li>
     *     <li>{@link StringLiteral}：字符串类型</li>
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

        // 函数定义表达式（FunctionLiteral）
        // 例如：fn(x, y) { x + y }
        // 求值时不会立即执行函数体，而是创建一个函数对象（闭包），
        // 将参数列表、函数体和当前环境一起封装成 MonkeyFunction 返回。
        if (node instanceof FunctionLiteral) {
            FunctionLiteral function = (FunctionLiteral) node;
            BlockStatement body = function.getBody();
            List<Identifier> parameters = function.getParameters();
            return new MonkeyFunction(parameters, body, env);
        }

        // 函数调用表达式（CallExpression）
        // 例如：add(2, 3)
        // 先对函数部分（add）求值，得到对应的函数对象；
        // 再对参数列表求值，然后调用 applyFunction 执行函数体。
        if (node instanceof CallExpression) {
            CallExpression call = (CallExpression) node;
            MonkeyObject eval = eval(call.getFunction(), env);
            if (isError(eval)) {
                return eval;
            }
            List<MonkeyObject> args = evalExpression(call.getArguments(), env);
            if (args.size() == 1 && isError(args.get(0))) {
                return args.get(0);
            }
            return applyFunction(eval, args);
        }

        // 字符串字面量 (StringLiteral)
        // 例如 \"Hello World\"
        if (node instanceof StringLiteral) {
            return new MonkeyString(((StringLiteral) node).getValue());
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
        } else if((left.type() == ObjectType.STRING_OBJ) &&
                (right.type() == ObjectType.STRING_OBJ)) {
            // 两边都是字符串，进入字符串拼接分支
            return evalStringInfixExpression(operator, left, right);
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
     * 在指定环境中对参数列表进行求值。
     * <p>
     * 逐个对表达式求值，并将结果放入列表中。
     * 若其中任意表达式求值出错（返回 {@link MonkeyError}），
     * 则立即返回仅包含错误的列表。
     * </p>
     *
     * @param exps 参数表达式列表
     * @param env  当前执行环境
     * @return 参数求值结果列表（可能包含一个错误对象）
     */
    private static List<MonkeyObject> evalExpression(List<Expression> exps, Environment env) {
        List<MonkeyObject> result = new ArrayList<>();

        for (Expression exp : exps) {
            MonkeyObject eval = eval(exp, env);
            if (isError(eval)) {
                result.add(eval);
                return result;
            }
            result.add(eval);
        }

        return result;
    }

    /**
     * 创建一个新的函数调用环境。
     * <p>
     * 用于实现闭包与函数参数绑定：
     * <ul>
     *     <li>新建一个空的环境，其外层指向函数定义时的环境（闭包）</li>
     *     <li>将函数形参与实参一一绑定到新环境中</li>
     * </ul>
     *
     * 例如：
     * <pre>
     * fn add(x, y) { x + y };
     * add(1, 2);
     * </pre>
     * 调用时会生成一个局部环境：
     * <ul>
     *     <li>x → 1</li>
     *     <li>y → 2</li>
     * </ul>
     *
     * @param fn   被调用的函数对象
     * @param args 实参求值结果
     * @return 新建的局部环境，外层指向函数定义时环境
     */
    private static Environment extendFunctionEnv(MonkeyFunction fn,
                                                 List<MonkeyObject> args) {
        Environment env = new Environment(new HashMap<>(), fn.getEnv());

        List<Identifier> parameters = fn.getParameters();

        for (int i = 0; i < fn.getParameters().size(); i++) {
            env.set(parameters.get(i).getValue(), args.get(i));
        }

        return env;
    }

    /**
     * 对函数返回结果进行解包。
     * <p>
     * 若返回对象为 {@link MonkeyReturn}，
     * 则取出其内部值并返回；
     * 否则原样返回（用于普通表达式的结果传递）。
     * </p>
     *
     * @param obj 待处理对象
     * @return 真正的返回值对象
     */
    private static MonkeyObject unwrapReturnValue(MonkeyObject obj) {
        if (obj instanceof MonkeyReturn) {
            MonkeyReturn ret = (MonkeyReturn) obj;
            return ret.getValue();
        }

        return obj;
    }

    /**
     * 对函数调用表达式进行求值。
     * <p>
     * 步骤：
     * <ol>
     *     <li>先对函数部分（左侧）求值，得到 {@link MonkeyFunction}</li>
     *     <li>再对实参列表求值，得到 {@code List<MonkeyObject>}</li>
     *     <li>调用 {@link #applyFunction(MonkeyObject, List)} 实现函数调用逻辑</li>
     * </ol>
     * </p>
     *
     * @param obj  被调用的函数对象
     * @param args 实参求值结果列表
     * @return 函数返回值或错误对象
     */
    private static MonkeyObject applyFunction(MonkeyObject obj,
                                              List<MonkeyObject> args) {
        if (!(obj instanceof MonkeyFunction)) {
            return newError("not a function: " + obj.getClass()
                    .getSimpleName());
        }

        MonkeyFunction fn = (MonkeyFunction) obj;

        if (fn.getParameters().size() != args.size()) {
            return newError("wrong number of arguments: expected=" + fn.getParameters().size()
                            + ", got=" + args.size());
        }

        Environment env = extendFunctionEnv(fn, args);
        MonkeyObject eval = eval(fn.getBody(), env);
        return unwrapReturnValue(eval);
    }

    /**
     * 计算字符串类型（{@link com.linewell.monkey.object.imp.MonkeyString}）的中缀表达式。
     * <p>
     * 该方法用于求值阶段处理字符串之间的中缀运算（目前仅支持字符串拼接 {@code +}）。
     * 当左右操作数均为 {@code MonkeyString} 类型时，
     * 返回一个新的 {@code MonkeyString}，其值为两者拼接结果。
     * </p>
     *
     * <p><b>语义说明：</b></p>
     * <ul>
     *   <li>当操作符为 {@code +} 且左右操作数均为字符串时，执行字符串拼接。</li>
     *   <li>当操作符不是 {@code +} 时，返回错误对象（不支持的操作）。</li>
     *   <li>当任一操作数不是 {@code MonkeyString} 类型时，返回错误对象。</li>
     * </ul>
     *
     * <p><b>错误处理：</b></p>
     * <ul>
     *   <li>若操作符不为 {@code +}，返回：{@code "unknown operator: STRING <op> STRING"}。</li>
     *   <li>若左或右操作数类型错误，返回相应错误信息。</li>
     * </ul>
     *
     * <p><b>示例：</b></p>
     * <pre>
     * evalStringInfixExpression("+",
     *     new MonkeyString("Hello "),
     *     new MonkeyString("World"));
     * // => MonkeyString("Hello World")
     *
     * evalStringInfixExpression("*",
     *     new MonkeyString("Hello"), new MonkeyString("World"));
     * // => Error("unknown operator: STRING * STRING")
     * </pre>
     *
     * @param operator 中缀运算符（目前仅支持 {@code +}）
     * @param left     左操作数对象（期望类型为 {@link com.linewell.monkey.object.imp.MonkeyString}）
     * @param right    右操作数对象（期望类型为 {@link com.linewell.monkey.object.imp.MonkeyString}）
     * @return 字符串拼接结果（{@code MonkeyString}）或错误对象（{@code MonkeyError}）
     */
    private static MonkeyObject evalStringInfixExpression(String operator,
                                                          MonkeyObject left,
                                                          MonkeyObject right) {
        if (!operator.equals("+")) {
            return newError("unknown operator: " + left.type() + " " +
                    operator + " " + right.type());
        }

        if (!(left instanceof MonkeyString)) {
            return newError("left is not MonkeyString.");
        }

        if (!(right instanceof MonkeyString)) {
            return newError("right is not MonkeyString.");
        }

        MonkeyString leftObj = (MonkeyString) left;
        MonkeyString rightObj = (MonkeyString) right;

        return new MonkeyString(leftObj.getValue() + rightObj.getValue());
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
