package com.linewell.monkey.evaluator;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.*;
import com.linewell.monkey.ast.modify.Modify;
import com.linewell.monkey.object.Environment;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.imp.MonkeyMacro;
import com.linewell.monkey.object.imp.MonkeyQuote;

import java.util.ArrayList;
import java.util.List;

/**
 * 宏展开处理器（MacroExpansion）
 * <p>
 * 核心功能：
 * 1. 识别并解析程序AST中的宏定义语句（LetStatement + MacroLiteral）
 * 2. 将宏定义存储到执行环境（Environment）中
 * 3. 移除AST中的宏定义语句（避免运行时执行）
 * 4. 遍历AST，将宏调用表达式替换为宏体的实际节点（宏展开）
 * <p>
 * 适用场景：Monkey语言解释器/编译器中的宏处理阶段
 */
public class MacroExpansion {

    /**
     * 遍历程序AST节点，识别并定义宏，同时记录宏定义语句的下标。
     * <p>
     * 核心逻辑：
     * 1. 遍历Program中的所有Statement；
     * 2. 识别宏定义语句（isMacroDefinition）；
     * 3. 将宏定义添加到执行环境（env）中；
     * 4. 记录宏定义语句的下标（后续用于从Program中移除这些语句）。
     *
     * @param program 程序AST根节点，包含所有待解析的语句
     * @param env     执行环境，用于存储宏定义
     */
    public void defineMacros(Program program, Environment env) {
        // 存储宏定义语句的下标，用于后续移除
        List<Integer> definitions = new ArrayList<>();

        // 第一步：遍历所有语句，识别宏定义并记录下标
        for (int i = 0; i < program.getStatements().size(); i++) {
            Statement statement = program.getStatements().get(i);
            // 判断当前语句是否是合法的宏定义
            if (isMacroDefinition(statement)) {
                // 将宏定义添加到执行环境
                addMacro(statement, env);
                // 记录宏定义语句的下标
                definitions.add(i);
            }
        }

        // 第二步：逆序移除宏定义语句（从后往前删，避免下标偏移）
        for (int i = definitions.size() - 1; i >= 0; i--) {
            int definitionIndex = definitions.get(i);
            // 边界校验：防止下标越界
            if (program.getStatements().size() < definitionIndex) {
                System.err.println("Invalid definition index: " + definitionIndex);
                return;
            }
            // 移除宏定义语句（这些语句仅用于定义，无需执行）
            program.getStatements().remove(definitionIndex);
        }
    }

    /**
     * 判断单个Statement是否是合法的宏定义语句
     * <p>
     * 宏定义的判定条件：
     * 1. 语句类型必须是LetStatement（let声明）
     * 2. LetStatement的值必须是MacroLiteral（宏字面量）
     *
     * @param node 待判断的AST节点（Statement类型）
     * @return true=是宏定义语句，false=不是
     */
    private boolean isMacroDefinition(Statement node) {
        // 条件1：必须是LetStatement（let声明语句）
        if (!(node instanceof LetStatement)) {
            return false;
        }
        LetStatement letStatement = (LetStatement) node;

        Expression value = letStatement.getValue();
        // 条件2：LetStatement的值必须是MacroLiteral（宏字面量）
        if (!(value instanceof MacroLiteral)) {
            return false;
        }

        return true;
    }

    /**
     * 将合法的宏定义语句注册到执行环境中
     * <p>
     * 处理逻辑：
     * 1. 类型校验（确保是LetStatement + MacroLiteral）
     * 2. 构建MonkeyMacro实例（包含参数、宏体、外层环境）
     * 3. 将宏名和MonkeyMacro绑定到执行环境
     *
     * @param statement 合法的宏定义语句（LetStatement类型）
     * @param env       执行环境，用于存储宏定义
     */
    private void addMacro(Statement statement, Environment env) {
        // 类型校验1：确保是LetStatement
        if (!(statement instanceof LetStatement)) {
            System.err.println("statement is not a Letstatement get=" +
                    statement.getClass().getSimpleName());
            return;
        }
        LetStatement letStatement = (LetStatement) statement;

        // 类型校验2：确保LetStatement的值是MacroLiteral
        if (!(letStatement.getValue() instanceof MacroLiteral)) {
            System.err.println("statement.value is not a MacroLetStatement get=" +
                    letStatement.getValue().getClass().getSimpleName());
            return;
        }

        // 提取宏字面量（包含宏的参数列表和宏体）
        MacroLiteral macroLiteral = (MacroLiteral) letStatement.getValue();

        // 构建MonkeyMacro实例（绑定参数、宏体、当前执行环境）
        MonkeyMacro macro = new MonkeyMacro(macroLiteral.getParameters(), // 宏的参数列表（Identifier列表）
                macroLiteral.getBody(), // 宏的执行体（AST节点）
                env); // 宏定义时的外层环境

        // 将宏注册到执行环境（key：宏名，value：MonkeyMacro实例）
        env.set(letStatement.getName().getValue(), macro);
    }

    /**
     * 遍历AST节点，将所有宏调用表达式替换为宏体的实际节点（宏展开核心逻辑）
     * <p>
     * 处理流程：
     * 1. 使用Modify工具遍历AST所有节点
     * 2. 筛选出CallExpression（函数调用表达式）
     * 3. 判断是否是宏调用（调用的是环境中的MonkeyMacro）
     * 4. 若是宏调用：解析参数→扩展环境→执行宏体→替换为宏体节点
     * 5. 若不是：返回原节点
     *
     * @param program 待展开宏的AST根节点
     * @param env     包含宏定义的执行环境
     * @return 宏展开后的新AST节点
     */
    public Node expandMacros(Node program, Environment env) {
        return Modify.modify(program, node -> {
            // 过滤非函数调用表达式的节点（直接返回原节点）
            if (!(node instanceof CallExpression)) {
                return node;
            }

            CallExpression callExpression = (CallExpression) node;

            // 判断当前调用是否是宏调用（从环境中获取对应的MonkeyMacro）
            MonkeyMacro macroCall = isMacroCall(callExpression, env);
            if (macroCall == null) {
                // 不是宏调用，返回原节点
                return node;
            }

            // 步骤1：将宏调用的参数转为MonkeyQuote（保留AST节点，不立即求值）
            List<MonkeyQuote> args = quoteArgs(callExpression);

            // 步骤2：扩展宏的执行环境（绑定宏参数和实际传入的参数）
            Environment evalEnv = extendMacroEnv(macroCall, args);

            // 步骤3：执行宏体，得到宏展开后的AST节点
            MonkeyObject evaluated = Evaluator.eval(macroCall.getBody(), evalEnv);

            // 校验：宏必须返回AST节点（MonkeyQuote类型）
            if(!(evaluated instanceof MonkeyQuote)) {
                System.err.println("we only support returning AST-nodes from macros");
                return null;
            }

            MonkeyQuote quote = (MonkeyQuote) evaluated;

            return quote.getNode();
        });
    }

    /**
     * 判断函数调用表达式是否是宏调用
     * <p>
     * 判定逻辑：
     * 1. 调用的函数必须是Identifier（标识符，如宏名）
     * 2. 标识符在环境中存在对应的MonkeyMacro实例
     *
     * @param exp 函数调用表达式（CallExpression）
     * @param env 包含宏定义的执行环境
     * @return MonkeyMacro=是宏调用，null=不是
     */
    private MonkeyMacro isMacroCall(CallExpression exp, Environment env) {
        Expression function = exp.getFunction();
        // 条件1：函数必须是Identifier类型（宏名是标识符）
        if (!(function instanceof Identifier)) {
            System.err.println("function is not a Identifier");
            return null;
        }
        Identifier identifier = (Identifier) function;

        MonkeyObject monkeyObject = env.get(identifier.getValue());

        // 条件2：环境中必须存在该宏名
        if (monkeyObject == null) {
            return null;
        }

        // 条件3：获取到的对象必须是MonkeyMacro类型
        if (!(monkeyObject instanceof MonkeyMacro)) {
            return null;
        }

        return (MonkeyMacro) monkeyObject;
    }

    /**
     * 将宏调用的参数列表转为MonkeyQuote列表
     * <p>
     * 核心目的：保留参数的AST节点（不立即求值），让宏可以操作原始AST节点
     * （宏的特性：宏参数是AST节点，而非求值后的结果）
     *
     * @param exp 宏调用表达式（CallExpression）
     * @return 包装后的MonkeyQuote列表
     */
    private List<MonkeyQuote> quoteArgs(CallExpression exp) {
        List<MonkeyQuote> args = new ArrayList<>();

        for (Node node : exp.getArguments()) {
            MonkeyQuote monkeyQuote = new MonkeyQuote(node);
            args.add(monkeyQuote);
        }

        return args;
    }

    /**
     * 扩展宏的执行环境（绑定宏参数和实际参数）
     * <p>
     * 处理逻辑：
     * 1. 创建新的环境，继承宏定义时的外层环境（作用域链）
     * 2. 将宏的形参（Identifier）和实际参数（MonkeyQuote）绑定到新环境
     *
     * @param macro 宏实例（包含参数列表）
     * @param args  宏调用的实际参数（MonkeyQuote列表）
     * @return 扩展后的执行环境
     */
    private Environment extendMacroEnv(MonkeyMacro macro, List<MonkeyQuote> args) {
        Environment extended = new Environment();
        extended.setOuter(macro.getEnv());

        List<Identifier> parameters = macro.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            extended.set(parameters.get(i).getValue(), args.get(i));
        }

        return extended;
    }
}
