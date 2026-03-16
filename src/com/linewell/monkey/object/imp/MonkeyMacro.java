package com.linewell.monkey.object.imp;

import com.linewell.monkey.ast.imp.BlockStatement;
import com.linewell.monkey.ast.imp.Identifier;
import com.linewell.monkey.object.Environment;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Monkey解释器中运行时的宏对象，实现 {@link MonkeyObject} 接口。
 * <p>
 * 该类与抽象语法树（AST）层的 {@link MacroLiteral} 不同：
 * <ul>
 *   <li>{@link MacroLiteral} 是解析阶段的AST节点，仅存储语法结构；</li>
 *   <li>{@code MonkeyMacro} 是运行时的宏对象，关联了执行环境（{link Environment}），
 *       是宏替换和执行的核心载体。</li>
 * </ul>
 * 宏对象在解析阶段由 {@link MacroLiteral} 转换而来，用于后续宏调用时的代码替换逻辑。
 *
 * @author huangxin
 * @see MonkeyObject
 * @see MacroLiteral
 * @see Environment
 */
public class MonkeyMacro implements MonkeyObject {

    List<Identifier> parameters;

    BlockStatement body;

    Environment env;

    public MonkeyMacro() {
    }

    public MonkeyMacro(List<Identifier> parameters, BlockStatement body, Environment env) {
        this.parameters = parameters;
        this.body = body;
        this.env = env;
    }

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

    @Override
    public ObjectType type() {
        return ObjectType.MACRO_OBJ;
    }

    /**
     * 将宏对象转换为可读的字符串形式，还原宏的定义语法。
     * <p>
     * 输出格式示例：
     * <pre>
     * macro(x, y) {
     *   x + y;
     * }
     * </pre>
     *
     * @return 宏对象的字符串表示，符合Monkey语言的宏定义语法规范
     */
    @Override
    public String inspect() {

        StringBuilder sb = new StringBuilder();

        String parame = "";
        if (parameters != null) {
            parame =  parameters.stream().map(Object::toString)
                    .collect(Collectors.joining(", "));
        }

        sb.append("macro");
        sb.append("(");
        sb.append(parame);
        sb.append(") {\n");
        sb.append(body.toString());
        sb.append("\n}");

        return sb.toString();
    }
}
