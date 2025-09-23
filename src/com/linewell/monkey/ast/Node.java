package com.linewell.monkey.ast;

public interface Node {

    /***
     * 返回节点对应的第一个 token 字面量
     * @return
     */
    String tokenLiteral();

    /**
     * 返回节点的字符串表示，通常用于调试和生成源码
     * @return 节点的源码风格字符串
     */
    String toString();
}
