package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.HashKey;
import com.linewell.monkey.object.Hashable;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

/**
 * {@code MonkeyBoolean} 表示 Monkey 语言中的布尔对象。
 *
 * <p>该类实现了 {@link MonkeyObject} 与 {@link Hashable} 接口，
 * 用于封装布尔字面量（如 {@code true} 和 {@code false}）。</p>
 *
 * <p>在解释执行过程中，解析器（Parser）会将源码中的布尔字面量
 * 转换为 {@code MonkeyBoolean} 对象，并在求值器（Evaluator）阶段
 * 参与逻辑表达式的计算。</p>
 *
 * <h3>哈希支持：</h3>
 * <p>布尔对象可作为哈希表的键（Hash Key）。
 * {@link #HashKey()} 方法会根据布尔值生成唯一的哈希键：
 * <ul>
 *   <li>{@code true → HashKey(1, BOOLEAN_OBJ)}</li>
 *   <li>{@code false → HashKey(0, BOOLEAN_OBJ)}</li>
 * </ul>
 * </p>
 *
 * <p>该设计保证了布尔键的可比较性与哈希表查找的一致性。</p>
 *
 * <h3>示例：</h3>
 * <pre>
 * MonkeyBoolean t = MonkeyBoolean.getBoolean(true);
 * System.out.println(t.inspect());  // 输出: "true"
 * System.out.println(t.HashKey());  // 输出: HashKey{type=BOOLEAN_OBJ, value=1}
 * </pre>
 *
 * @see MonkeyObject
 * @see Hashable
 * @see com.linewell.monkey.object.HashKey
 * @see ObjectType#BOOLEAN_OBJ
 */
public class MonkeyBoolean implements MonkeyObject, Hashable {

    /** 预定义单例实例，避免重复创建。 */
    public static final MonkeyBoolean TRUE = new MonkeyBoolean(true);
    public static final MonkeyBoolean FALSE = new MonkeyBoolean(false);

    /** 存储布尔值（true 或 false） */
    private boolean value;

    /**
     * 获取布尔值。
     *
     * @return {@code true} 或 {@code false}
     */
    public boolean isValue() {
        return value;
    }

    /**
     * 构造函数，创建一个新的布尔对象。
     *
     * @param value 初始布尔值
     */
    private MonkeyBoolean(boolean value) {
        this.value = value;
    }

    /**
     * 获取对象的类型标记。
     *
     * @return {@link ObjectType#BOOLEAN_OBJ}
     */
    @Override
    public ObjectType type() {
        return ObjectType.BOOLEAN_OBJ;
    }

    /**
     * 返回布尔值的字符串表示。
     *
     * @return {@code "true"} 或 {@code "false"}
     */
    @Override
    public String inspect() {
        return Boolean.toString(value);
    }

    /**
     * 根据给定的布尔值返回对应的 {@code MonkeyBoolean} 实例。
     *
     * <p>使用单例模式，避免重复创建相同值的对象。</p>
     *
     * @param value 布尔值
     * @return 对应的 {@link MonkeyBoolean} 实例
     */
    public static MonkeyBoolean getBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * 生成用于哈希表键比较的 {@link HashKey}。
     * <p>使用固定整数（0/1）作为哈希值以确保布尔键唯一性。</p>
     */
    @Override
    public HashKey HashKey() {
        int value = 0;
        if (this.value) {
            value = 1;
        }

        return new HashKey(value, type());
    }
}
