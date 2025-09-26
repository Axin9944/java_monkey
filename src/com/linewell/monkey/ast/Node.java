package com.linewell.monkey.ast;

/**
 * AST 抽象语法树的基础节点接口。
 *
 * <p>所有语法树节点（表达式 {@link com.linewell.monkey.ast.Expression}、
 * 语句 {@link com.linewell.monkey.ast.Statement}）都必须实现该接口。</p>
 *
 * <p>设计意义：</p>
 * <ul>
 *     <li>统一 AST 节点的抽象类型，便于遍历和处理</li>
 *     <li>提供 {@link #tokenLiteral()} 方法，用于快速获取节点关联的第一个词法单元，
 *         在调试和错误报告时尤其有用</li>
 *     <li>重写 {@link #toString()} 方法，将节点序列化为源码风格字符串，
 *         常用于调试或 AST 转源码的过程</li>
 * </ul>
 */
public interface Node {

    /**
     * 返回当前节点对应的第一个 token 的字面量。
     * <p>
     * 例如：let 语句节点会返回 "let"，布尔字面量节点会返回 "true"/"false"。
     *
     * @return 节点首个 token 的字面量
     */
    String tokenLiteral();

    /**
     * 返回当前节点的字符串表示形式。
     * <p>
     * 一般实现为节点的源码风格字符串，便于调试和打印 AST。
     *
     * @return 节点对应的字符串
     */
    String toString();
}
