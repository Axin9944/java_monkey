package com.linewell.monkey.object.imp;

import com.linewell.monkey.ast.Node;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

/**
 * 引用（Quote）对象类。
 * <p>
 * 该类用于封装 Monkey 语言中的 {@code quote(...)} 表达式的运行时对象。
 * 与普通求值不同，{@code quote} 表达式不会对内部 AST 进行求值，
 * 而是将抽象语法树（{@link Node}）本身作为数据对象返回，从而支持
 * “代码即数据（code as data）” 的宏系统（Macro System）基础功能。
 * </p>
 *
 * <p><b>功能说明：</b></p>
 * <ul>
 *   <li>在求值阶段，解释器遇到 {@code quote(expr)} 时，不执行 {@code expr} 的求值，
 *       而是将其对应的语法树节点封装为一个 {@link MonkeyQuote} 对象返回。</li>
 *   <li>该对象仅保存 AST 结构，不包含执行结果。</li>
 *   <li>可配合 {@code unquote(...)} 实现宏展开与动态代码生成等高级特性。</li>
 * </ul>
 *
 * <p><b>示例：</b></p>
 * <pre>
 * // Monkey 代码：
 * quote(1 + 2)
 *
 * // 对应的运行时对象：
 * MonkeyQuote{ node = (1 + 2) }
 *
 * // inspect() 输出：
 * QUOTE((1 + 2))
 * </pre>
 *
 * <p><b>典型用途：</b></p>
 * <ul>
 *   <li>在求值器（{@code Evaluator}）中使用 {@code quote(Node)} 方法生成。</li>
 *   <li>为宏系统提供 AST 层面的操作能力。</li>
 * </ul>
 *
 * <p>说明：该类仅封装语法树节点，不包含执行逻辑。</p>
 *
 * @see com.linewell.monkey.evaluator.Evaluator#quote(Node)
 * @see com.linewell.monkey.ast.Node
 * @author axin
 */
public class MonkeyQuote implements MonkeyObject {

    private Node node;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public MonkeyQuote(Node node) {
        this.node = node;
    }

    public MonkeyQuote() {
    }

    @Override
    public ObjectType type() {
        return ObjectType.QUOTE_OBJ;
    }

    @Override
    public String inspect() {
        return "QUOTE(" + node + ")";
    }

}
