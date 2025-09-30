package com.linewell.monkey.object;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code Environment} 表示 Monkey 解释器的运行环境（符号表）。
 *
 * <p>它维护一个从变量名（字符串）到 {@link MonkeyObject} 的映射关系，
 * 用于在解释执行过程中存储和查找变量绑定。</p>
 *
 * <p>例如在执行以下 Monkey 代码时：</p>
 * <pre>
 * let x = 5;
 * let y = x + 10;
 * </pre>
 * {@code Environment} 会维护如下映射：
 * <ul>
 *   <li>x -> MonkeyInteger(5)</li>
 *   <li>y -> MonkeyInteger(15)</li>
 * </ul>
 */
public class Environment {

    /**
     * 存储变量名到 {@link MonkeyObject} 的映射。
     */
    private Map<String, MonkeyObject> store;

    /**
     * 获取当前环境的底层存储映射。
     *
     * @return 符号表（变量名到对象的映射）
     */
    public Map<String, MonkeyObject> getStore() {
        return store;
    }

    /**
     * 设置当前环境的底层存储映射。
     *
     * @param store 新的符号表
     */
    public void setStore(Map<String, MonkeyObject> store) {
        this.store = store;
    }

    /**
     * 创建一个新的空环境。
     *
     * <p>内部使用 {@link HashMap} 初始化存储。</p>
     */
    public Environment() {
        this.store = new HashMap<>();
    }

    /**
     * 使用给定的存储映射创建环境。
     *
     * @param store 初始符号表（变量名到对象的映射）
     */
    public Environment(Map<String, MonkeyObject> store) {
        this.store = store;
    }

    /**
     * 根据变量名获取对象。
     *
     * @param name 变量名
     * @return 对应的 {@link MonkeyObject}，如果未定义则返回 {@code null}
     */
    public MonkeyObject get(String name) {
        return store.get(name);
    }

    /**
     * 设置或更新变量绑定。
     *
     * <p>如果变量已存在，则覆盖旧值；否则新增一个变量。</p>
     *
     * @param name  变量名
     * @param value 要绑定的值
     * @return 传入的 {@code value}（与 Go 版本保持一致）
     */
    public MonkeyObject set(String name, MonkeyObject value) {
        store.put(name, value);
        return value;
    }
}
