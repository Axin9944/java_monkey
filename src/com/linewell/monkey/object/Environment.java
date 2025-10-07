package com.linewell.monkey.object;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code Environment} 表示 Monkey 解释器的运行环境（符号表，或称作用域）。
 *
 * <p>它维护一个从变量名（字符串）到 {@link MonkeyObject} 的映射关系，
 * 用于在解释执行过程中存储和查找变量绑定。</p>
 *
 * <h3>环境的层级结构</h3>
 * <p>
 * Monkey 语言支持函数与闭包，因此需要支持“词法作用域”。
 * 每次进入一个函数或块级作用域时，解释器会创建一个新的
 * {@code Environment} 实例，该实例称为“局部环境（Local Environment）”，
 * 并通过 {@link #outer} 字段指向定义时的外层环境。
 * </p>
 *
 * <p>变量查找遵循“从内到外”规则：</p>
 * <ul>
 *     <li>优先在当前环境（当前作用域）中查找变量；</li>
 *     <li>如果未找到且存在外层环境（outer），则递归向外查找；</li>
 *     <li>若所有环境中都不存在该变量，则返回 {@code null}。</li>
 * </ul>
 *
 * <h3>示例</h3>
 * <pre>
 * let x = 10;
 * let add = fn(y) {
 *     let z = 5;
 *     return x + y + z;
 * };
 * </pre>
 * <ul>
 *   <li>全局环境（Global Environment）：
 *       <ul>
 *           <li>x -> MonkeyInteger(10)</li>
 *           <li>add -> MonkeyFunction(...)</li>
 *       </ul>
 *   </li>
 *   <li>调用 {@code add(2)} 时会创建一个新的局部环境：
 *       <ul>
 *           <li>y -> MonkeyInteger(2)</li>
 *           <li>z -> MonkeyInteger(5)</li>
 *           <li>outer -> 指向全局环境</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * @author axin
 */
public class Environment {

    /**
     * 当前环境中的符号表：
     * 保存变量名到 {@link MonkeyObject} 的映射。
     */
    private Map<String, MonkeyObject> store;

    /**
     * 指向外层环境（父作用域）。
     * <p>用于实现嵌套作用域和闭包。</p>
     * <p>如果为 {@code null}，则表示当前环境为全局环境。</p>
     */
    private Environment outer;

    public Environment getOuter() {
        return outer;
    }

    public void setOuter(Environment outer) {
        this.outer = outer;
    }

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
     * <p>该环境没有外层环境（即全局环境）。</p>
     */
    public Environment() {
        this.store = new HashMap<>();
        this.outer = null;
    }

    /**
     * 使用给定的存储映射创建一个环境。
     * <p>外层环境默认为 {@code null}（全局环境）。</p>
     *
     * @param store 初始符号表（变量名到对象的映射）
     */
    public Environment(Map<String, MonkeyObject> store) {
        this.store = store;
        outer = null;
    }

    /**
     * 创建一个带有外层环境的局部环境。
     * <p>此构造函数通常在函数调用或块作用域中使用，
     * 用于创建一个新的局部环境，并与父作用域形成作用域链。</p>
     *
     * @param store 当前作用域的符号表
     * @param outer 外层环境（父作用域）
     */
    public Environment(Map<String, MonkeyObject> store, Environment outer) {
        this.store = store;
        this.outer = outer;
    }


    /**
     * 根据变量名从当前环境及其外层环境中查找对象。
     * <p>查找顺序为从内到外（即当前环境优先）。</p>
     *
     * @param name 变量名
     * @return 对应的 {@link MonkeyObject}；若未定义则返回 {@code null}
     */
    public MonkeyObject get(String name) {
        // 先在当前环境查找
        if (store.containsKey(name)) {
            return store.get(name);
        }

        // 当前环境没有，且存在外层环境，则递归查找
        if (outer != null) {
            return outer.get(name);
        }
        return null;
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
