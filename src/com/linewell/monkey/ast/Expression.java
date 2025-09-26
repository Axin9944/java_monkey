package com.linewell.monkey.ast;

/**
 * 表达式（Expression）接口。
 *
 * <p>这是一个标记接口（Marker Interface），继承自 {@link Node}。
 * 主要用于在 AST 中对节点进行语义分类：</p>
 *
 * <ul>
 *     <li>所有的表达式节点（如字面量、函数调用、运算表达式等）都应实现该接口</li>
 *     <li>与之对应，语句（Statement）节点会实现 {@code Statement} 接口</li>
 * </ul>
 *
 * <p>这种分类有助于在编译器/解释器中区分不同类型的语法元素，
 * 例如在遍历 AST 或进行语义分析时，可以快速判断某个节点是“表达式”还是“语句”。</p>
 */
public interface Expression extends Node{

}
