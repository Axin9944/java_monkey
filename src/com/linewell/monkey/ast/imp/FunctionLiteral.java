package com.linewell.monkey.ast.imp;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.token.Token;

import java.util.List;

/**
 * AST 节点：函数字面量 (Function Literal)
 * 对应 Go 语言中的结构：
 * <pre>
 * type FunctionLiteral struct {
 *     Token      token.Token      // "fn" 词法单元
 *     Parameters []*Identifier    // 参数列表
 *     Body       *BlockStatement  // 函数体
 * }
 * </pre>
 */
public class FunctionLiteral implements Expression {

    // "fn" 关键字的词法单元
    private Token token;

    // 函数参数列表
    private List<Identifier> parameters;

    // 函数体语句块
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

    public FunctionLiteral() {
    }

    public FunctionLiteral(Token token, List<Identifier> parameters, BlockStatement body) {
        this.token = token;
        this.parameters = parameters;
        this.body = body;
    }

    /**
     * 返回词法单元的字面值（一般是 "fn"）
     */
    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    /**
     * 构造函数字面量的字符串形式，例如：
     * <pre>
     * fn(x, y) { ... }
     * </pre>
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(tokenLiteral());
        // 写入左括号
        sb.append("(");

        // 参数列表拼接，用逗号+空格分隔
        for (int i = 0; i < parameters.size(); i++) {
            sb.append(parameters.get(i).toString());
            if (i < parameters.size() - 1) {
                sb.append(", ");
            }
        }

        /*
        // ========== 版本2：Java 8 Stream 拼接 ==========
        if (parameters != null && !parameters.isEmpty()) {
            String paramsStr = parameters.stream()
                    .map(Identifier::toString)
                    .collect(Collectors.joining(", "));
            sb.append(paramsStr);
        }
        */

        // 写入右括号
        sb.append(")");
        // 写入函数体
        sb.append(body.toString());

        return sb.toString();
    }
}
