package com.linewell.monkey.ast.modify;

import com.linewell.monkey.ast.Node;

/**
 * {@code ModifierFunc} 是一个函数式接口，用于对 AST 节点执行统一修改操作。
 * <p>
 * 它被 {@link Modify#modify(Node, ModifierFunc)} 调用，用于在遍历过程中
 * 对每个节点执行自定义逻辑，例如替换、转换或分析。
 */
@FunctionalInterface
public interface ModifierFunc {

    /**
     * 对给定节点执行修改操作。
     *
     * @param node 当前 AST 节点
     * @return 修改后的节点（或原节点）
     */
    Node modifierFunc(Node node);
}
