package com.linewell.monkey.ast;

import com.linewell.monkey.token.Token;

public interface Statement extends Node {

    /***
     * 空方法，用于标记这是 语句节点
     */
    void statementNode();

    Token getToken();
}
