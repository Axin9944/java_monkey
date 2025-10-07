package com.linewell.monkey.object.imp;

import com.linewell.monkey.ast.imp.BlockStatement;
import com.linewell.monkey.ast.imp.Identifier;
import com.linewell.monkey.object.Environment;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

import java.util.List;

/**
 * {@code MonkeyFunction} 表示 Monkey 语言中的函数对象。
 * <p>
 * 在解释执行过程中，当解析到函数定义表达式时，
 * 会创建一个 {@code MonkeyFunction} 实例，用于封装函数的参数列表、
 * 函数体（语句块）以及定义该函数时的词法环境（Environment）。
 * <p>
 * 该类实现了 {@link MonkeyObject} 接口，是 Monkey 解释器中所有对象类型的一部分。
 * 在执行函数调用时，解释器会使用其中保存的环境信息进行作用域链的恢复。
 *
 * <p><b>示例（Monkey 代码）:</b>
 * <pre>
 * let add = fn(x, y) {
 *     return x + y;
 * };
 * </pre>
 * 上述定义会在 Java 层生成一个 {@code MonkeyFunction} 对象，
 * 其中 parameters = [x, y]，body 为函数体语句块，env 为定义时的环境。
 *
 * @author axin
 */
public class MonkeyFunction implements MonkeyObject {

    /** 函数的参数列表（形参标识符集合） */
    private List<Identifier> parameters;

    /** 函数体，对应 AST 中的 BlockStatement 节点 */
    private BlockStatement body;

    /** 定义该函数时所处的环境（用于实现闭包） */
    private Environment env;

    public List<Identifier> getParameters() {
        return parameters;
    }

    public void setParameters(List<Identifier> parameters) {
        this.parameters = parameters;
    }

    public BlockStatement getBody() {
        return body;
    }

    public void setBody(BlockStatement body) {
        this.body = body;
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public MonkeyFunction() {
    }

    /**
     * 构造函数。
     *
     * @param parameters 函数参数列表
     * @param body       函数体语句块
     * @param env        定义该函数的环境（用于闭包）
     */
    public MonkeyFunction(List<Identifier> parameters, BlockStatement body, Environment env) {
        this.parameters = parameters;
        this.body = body;
        this.env = env;
    }

    /**
     * 返回对象类型常量。
     *
     * @return {@link ObjectType#FUNCTION_OBJ}，表示这是一个函数对象
     */
    @Override
    public ObjectType type() {
        return ObjectType.FUNCTION_OBJ;
    }

    /**
     * 返回函数的字符串表示形式，用于调试或 REPL 输出。
     * <p>
     * 生成形如：
     * <pre>
     * fn(x, y) {
     *     return x + y;
     * }
     * </pre>
     *
     * @return 函数的可读字符串表示
     */
    @Override
    public String inspect() {
        StringBuilder fun = new StringBuilder();

        fun.append("fn");
        fun.append("(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i != parameters.size() - 1) {
                fun.append(parameters.get(i)).append(", ");
            } else {
                fun.append(parameters.get(i));
            }
        }
        fun.append(") {\n");
        fun.append(body.toString());
        fun.append("\n}");

        return fun.toString();
    }
}
