package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 抽象语法树（AST）中的宏字面量（Macro Literal）节点，实现 {@link Expression} 接口。
 * <p>
 * 该类用于封装Monkey语言中宏定义的核心信息，是宏机制在AST中的核心载体，
 * 对应语法结构：{@code macro(参数列表) { 语句块 }}。
 * <p>
 * 宏字面量与函数字面量（FunctionLiteral）语法结构相似，但核心区别在于：
 * 宏在解析阶段执行代码替换，而函数在运行时执行；宏的参数仅作为语法替换模板使用，
 * 不涉及运行时的参数传递。
 *
 * @author （可补充作者信息）
 * @see Expression
 * @see Identifier
 * @see BlockStatement
 */
public class MacroLiteral implements Expression {

    private Token token;

    private List<Identifier> parameters;

    private BlockStatement body;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
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

    public MacroLiteral() {
    }

    public MacroLiteral(Token token, List<Identifier> parameters, BlockStatement body) {
        this.token = token;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 将宏字面量节点转换为可读的字符串形式，还原宏定义语法。
     * <p>
     * 输出格式示例：{code macro(x, y) { x + y; }}
     *
     * @return 宏字面量的字符串表示，与Monkey源码中的宏定义语法一致
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(tokenLiteral());
        sb.append("(");
        if (parameters != null) {
            String parameter = parameters.stream().map(Identifier::toString)
                    .collect(Collectors.joining(", "));
            sb.append(parameter);
        }
        sb.append(")");
        sb.append(body.toString());

        return sb.toString();
    }
}
