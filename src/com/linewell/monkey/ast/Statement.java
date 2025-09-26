package com.linewell.monkey.ast;

import com.linewell.monkey.token.Token;

/**
 * 语句（Statement）接口。
 *
 * <p>继承自 {@link Node}，用于在 AST 中表示语句节点。
 * 不同于 {@link Expression}，语句通常是完整的执行单元，例如：</p>
 *
 * <ul>
 *     <li>let 语句（变量声明）</li>
 *     <li>return 语句</li>
 *     <li>表达式语句（仅执行表达式而不返回值）</li>
 * </ul>
 *
 * <p>与 Go 的隐式接口不同，Java 中为了保证在编译期就能访问到 Token，
 * 这里显式要求所有语句节点必须实现 {@link #getToken()} 方法。</p>
 */
public interface Statement extends Node {


    /**
     * 返回该语句关联的 Token。
     * <p>
     * 通常是语句的起始 Token，例如 let 语句的 "let"，
     * return 语句的 "return"。
     *
     * @return 与该语句绑定的词法单元 Token
     */
    Token getToken();
}
