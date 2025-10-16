package com.linewell.monkey.evaluator;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.*;
import com.linewell.monkey.object.*;
import com.linewell.monkey.object.imp.*;

import java.util.*;

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
 *     <li>内置函数（Built-in Functions，例如 len 等常用函数）</li>
 *     <li>数组字面量及索引操作</li>
 *     <li>哈希字面量及哈希索引操作</li>
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

    private static final Map<String, MonkeyBuiltin> BUILTINS = MonkeyBuiltin.getBUILTINS();

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
     *     <li>{@link CallExpression}：（函数调用，包括用户自定义函数与内置函数调用）</li>
     *     <li>{@link StringLiteral}：字符串类型</li>
     *     <li>{@link ArrayLiteral}：表示数组字面量，如 [1, 2, 3]</li>
     *     <li>{@link IndexExpression}：表示数组索引访问，如 a[0]</li>
     *     <li>{@link HashLiteral}：表示哈希字面量，如 {"name" : "mazi"}</li>
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

        // 数组表达式 (ArrayLiteral)，例如 let a = [1, 2, 3, 4];
        if (node instanceof ArrayLiteral) {
            List<MonkeyObject> elements = evalExpression(((ArrayLiteral) node).getElements(), env);
            if (elements.size() == 1 && isError(elements.get(0))) {
                return elements.get(0);
            }

            return new MonkeyArray(elements.toArray(new MonkeyObject[0]));
        }

        // 数组索引类型 （IndexExpression）
        // 例如 let a = [1, 2, 3, 4],
        // a[1],  {"name" : "麻子"}["name"]
        if (node instanceof IndexExpression) {
            MonkeyObject left = eval(((IndexExpression) node).getLeft(), env);
            if (isError(left)) {
                return left;
            }
            MonkeyObject index = eval(((IndexExpression) node).getIndex(), env);
            if (isError(index)) {
                return index;
            }

            return evalIndexExpression(left, index);
        }

        // 哈希类型 (HashLiteral)
        // 例如 {"name" : "麻子"}
        if (node instanceof HashLiteral) {
            return evalHashLiteral((HashLiteral) node, env);
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
     * 在指定环境中对标识符（Identifier）节点进行求值。
     * <p>
     * 该方法首先从当前 {@link Environment} 中查找标识符对应的变量绑定；
     * 若未找到，再尝试在全局内置函数表（{@code BUILTINS}）中查找。
     * 若仍未找到匹配项，则返回一个错误对象。
     * </p>
     *
     * <p>
     * 查找顺序如下：
     * <ol>
     *     <li>在当前环境中查找变量（用户定义的变量或函数）。</li>
     *     <li>在全局内置函数表中查找对应的内置函数（如 {@code len}）。</li>
     *     <li>若两者皆不存在，返回错误对象。</li>
     * </ol>
     * </p>
     *
     * @param node 标识符 AST 节点，表示要求值的变量名或函数名
     * @param env  当前求值环境，包含变量与函数的绑定信息
     * @return 对应的 {@link MonkeyObject}，可能是变量值、内置函数对象或错误对象
     */
    private static MonkeyObject evalIdentifier(Identifier node, Environment env) {

        // 1、先查变量
        MonkeyObject monkeyObject = env.get(node.getValue());
        if (monkeyObject != null) {
            return monkeyObject;
        }

        // 2、 后查内置函数
        MonkeyBuiltin monkeyBuiltin = BUILTINS.get(node.getValue());
        if (monkeyBuiltin != null) {
            return monkeyBuiltin;
        }

        // 都没有找到就返回错误
        return newError("identifier not found: " + node.getValue());
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
     * 本方法根据函数对象的类型，分别处理用户自定义函数与内置函数的调用逻辑。
     * </p>
     *
     * <p>
     * 求值流程：
     * <ol>
     *     <li>若 {@code obj} 为 {@link MonkeyFunction}（用户自定义函数）：
     *         <ul>
     *             <li>检查实参与形参数量是否匹配。</li>
     *             <li>创建新的函数执行环境（通过 {@link #extendFunctionEnv(MonkeyFunction, List)}）。</li>
     *             <li>在该环境中对函数体进行求值，并通过 {@link #unwrapReturnValue(MonkeyObject)} 获取返回值。</li>
     *         </ul>
     *     </li>
     *     <li>若 {@code obj} 为 {@link MonkeyBuiltin}（内置函数）：
     *         <ul>
     *             <li>将参数列表转换为数组形式。</li>
     *             <li>直接调用 {@link MonkeyBuiltin#call(MonkeyObject...)} 执行内置函数逻辑。</li>
     *         </ul>
     *     </li>
     *     <li>若 {@code obj} 既不是函数也不是内置函数，返回一个错误对象。</li>
     * </ol>
     * </p>
     *
     * @param obj  被调用的函数对象，可能是用户定义函数或内置函数
     * @param args 实参求值后的结果列表
     * @return 函数执行结果，可能为任意 {@link MonkeyObject}，或错误对象（如参数不匹配、类型错误等）
     */
    private static MonkeyObject applyFunction(MonkeyObject obj,
                                              List<MonkeyObject> args) {
        if (obj instanceof MonkeyFunction) {
            MonkeyFunction fn = (MonkeyFunction) obj;

            if (fn.getParameters().size() != args.size()) {
                return newError("wrong number of arguments: expected=" + fn.getParameters().size()
                        + ", got=" + args.size());
            }

            Environment env = extendFunctionEnv(fn, args);
            MonkeyObject eval = eval(fn.getBody(), env);
            return unwrapReturnValue(eval);
        }

        if (obj instanceof MonkeyBuiltin) {
            MonkeyBuiltin fn = (MonkeyBuiltin) obj;
            // 直接转换为数组
            MonkeyObject[] argsArray = args.toArray(new MonkeyObject[0]);
            return fn.call(argsArray);
        }

        return newError("not a function: " + obj.getClass()
                .getSimpleName());
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
     * 对数组索引表达式进行求值。
     * <p>
     * 该方法用于根据给定的下标 {@code index} 从数组 {@code array} 中取出对应元素，
     * 并在取值前执行类型检查与边界检查，以保证求值过程安全。
     * </p>
     *
     * <p>行为说明：</p>
     * <ul>
     *     <li>若 {@code index} 超出数组范围（小于 0 或大于等于数组长度），则返回 {@code NULL}。</li>
     *     <li>若索引在合法范围内，则返回对应位置的元素。</li>
     * </ul>
     *
     * <p>示例：</p>
     * <pre>
     *     MonkeyArray arr = [10, 20, 30];
     *     evalArrayIndexExpression(arr, new MonkeyInteger(1)); // 返回 20
     *     evalArrayIndexExpression(arr, new MonkeyInteger(5)); // 返回 NULL（越界）
     * </pre>
     *
     * @param array 待求值的数组对象 {@link MonkeyArray}
     * @param index 数组索引对象 {@link MonkeyInteger}
     * @return 若索引合法则返回对应元素，否则返回 {@code NULL}
     */
    private static MonkeyObject evalArrayIndexExpression(MonkeyObject array,
                                                         MonkeyObject index) {
        MonkeyArray monkeyArray = (MonkeyArray) array;
        MonkeyInteger idx = (MonkeyInteger) index;
        int len = monkeyArray.getElements().length;
        // 越界检查：索引小于 0 或大于等于数组长度时返回 NULL
        if (idx.getValue() < 0 || idx.getValue() >= len) {
            return NULL;
        }

        // 返回指定下标的数组元素
        return monkeyArray.getElements()[(int)idx.getValue()];
    }

    /**
     * 对通用的索引表达式 {@code left[index]} 进行求值。
     *
     * <p>该方法根据被索引对象（{@code left}）的类型，分派至不同的求值逻辑：</p>
     * <ul>
     *     <li>
     *         若 {@code left} 为 {@link MonkeyArray} 且 {@code index} 为 {@link MonkeyInteger}，
     *         调用 {@link #evalArrayIndexExpression(MonkeyObject, MonkeyObject)} 执行数组索引求值。
     *     </li>
     *     <li>
     *         若 {@code left} 为 {@link MonkeyHash}，
     *         调用 {@link #evalHashIndexExpression(MonkeyObject, MonkeyObject)} 执行哈希索引求值。
     *     </li>
     * </ul>
     *
     * <p>若索引操作应用于不支持的对象类型（例如字符串、布尔值、函数等），
     * 则返回一个 {@link MonkeyError}，提示该类型不支持索引操作。</p>
     *
     * <p>示例：</p>
     * <pre>{@code
     * evalIndexExpression(arrayObj, intObj);   // 合法，返回数组中的元素
     * evalIndexExpression(hashObj, stringObj); // 合法，返回哈希中对应键的值
     * evalIndexExpression(stringObj, intObj);  // 非法，返回错误对象
     * }</pre>
     *
     * @param left  被索引的对象（可为 {@link MonkeyArray} 或 {@link MonkeyHash}）
     * @param index 索引对象（数组索引时应为 {@link MonkeyInteger}；
     *              哈希索引时应为实现 {@link Hashable} 接口的对象）
     * @return 索引求值结果；
     *         若类型不支持索引操作，则返回 {@link MonkeyError}
     */
    private static MonkeyObject evalIndexExpression(MonkeyObject left,
                                                    MonkeyObject index) {
        if (left.type() == ObjectType.ARRAY_OBJ && index.type() == ObjectType.INTEGER_OBJ) {
            return evalArrayIndexExpression(left, index);
        } else if(left.type() == ObjectType.HASH_OBJ) {
            return evalHashIndexExpression(left, index);
        } else {
            return new MonkeyError(String.format("index operator not supported: %s",
                    left.type()));
        }
    }

    /**
     * 对 Monkey 语言中的哈希字面量（Hash Literal）表达式进行求值。
     *
     * <p>示例：</p>
     * <pre>{@code
     * {"one": 1, "two": 2, "three": 3}
     * }</pre>
     *
     * <p>求值过程：</p>
     * <ol>
     *     <li>依次对每个键（key）与值（value）表达式进行求值；</li>
     *     <li>检查键对象是否实现 {@link Hashable} 接口（确保可作为哈希键）；</li>
     *     <li>通过 {@code key.HashKey()} 生成 {@link HashKey} 并存入 {@link HashPair}；</li>
     *     <li>若过程中出现错误对象（{@code MonkeyError}），立即返回错误。</li>
     * </ol>
     *
     * <p>最终返回一个 {@link MonkeyHash} 对象，包含所有已求值的键值对。</p>
     *
     * @param node 表示哈希字面量的语法节点，包含键值表达式对
     * @param env 当前求值的环境（变量作用域）
     * @return 若成功求值，返回 {@link MonkeyHash}；
     *         若出现错误（如键不可哈希或表达式求值出错），返回 {@link MonkeyError}。
     */
    private static MonkeyObject evalHashLiteral(HashLiteral node, Environment env) {
        Map<HashKey, HashPair> pairs = new HashMap<>();

        Map<Expression, Expression> expression = node.getExpression();

        for (Map.Entry<Expression, Expression> entry : expression.entrySet()) {
            // 求值哈希键
            MonkeyObject key = eval(entry.getKey(), env);
            if (isError(key)) {
                return key;
            }

            // 检查键是否可哈希
            if (!(key instanceof Hashable)) {
                return newError(String.format("unusable as hash key: %s", key.type()));
            }

            MonkeyObject value = eval(entry.getValue(), env);
            if (isError(value)) {
                return value;
            }

            // 生成哈希键与哈希对
            HashKey hashKey = ((Hashable) key).HashKey();
            pairs.put(hashKey, new HashPair(key, value));
        }

        return new MonkeyHash(pairs);
    }

    /**
     * 对 Monkey 语言中的哈希索引表达式（Hash Index Expression）进行求值。
     *
     * <p>示例：</p>
     * <pre>{@code
     * {"foo": 5}["foo"]  // => 5
     * }</pre>
     *
     * <p>求值过程：</p>
     * <ol>
     *     <li>检查被索引对象是否为 {@link MonkeyHash}；</li>
     *     <li>检查索引对象是否实现 {@link Hashable} 接口（可作为哈希键）；</li>
     *     <li>计算索引的 {@link HashKey}，在哈希表中查找对应的 {@link HashPair}；</li>
     *     <li>若存在匹配项，则返回其值；若不存在，则返回 {@code NULL}；</li>
     *     <li>若任一检查失败，则返回 {@link MonkeyError}。</li>
     * </ol>
     *
     * @param hash 表示被索引的哈希对象（应为 {@link MonkeyHash} 类型）
     * @param index 表示索引键（应为实现 {@link Hashable} 的对象，如 {@link MonkeyString}、{@link MonkeyInteger}）
     * @return 若键存在，返回对应值；
     *         若键不存在，返回 {@code NULL}；
     *         若出现错误（如不可哈希键或错误类型），返回 {@link MonkeyError}。
     */
    private static MonkeyObject evalHashIndexExpression(MonkeyObject hash,
                                                        MonkeyObject index) {
        if (!(hash instanceof MonkeyHash)) {
            return newError("hash key is not MonkeyHash.");
        }

        if (!(index instanceof Hashable)) {
            return newError("unusable as hash key: " + index.type());
        }

        HashPair hashPair = ((MonkeyHash) hash).getPairs().get(((Hashable) index).HashKey());

        if (hashPair == null) {
            return NULL;
        }

        return hashPair.getValue();
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
