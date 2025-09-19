package com.linewell.monkey.ast;

public interface Node {

    /***
     * 返回节点对应的第一个 token 字面量
     * @return
     */
    String tokenLiteral();
}
