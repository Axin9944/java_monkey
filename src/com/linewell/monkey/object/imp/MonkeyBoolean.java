package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

/**
 * {@code MonkeyBoolean} 表示 Monkey 语言中的布尔对象。
 *
 * <p>该类实现了 {@link MonkeyObject} 接口，
 * 用于封装布尔字面量（如 {@code true} 和 {@code false}）。</p>
 *
 * <p>在解释执行过程中，解析器（Parser）将源码中的布尔字面量
 * 转换为 {@code MonkeyBoolean} 对象，并交由求值器（Evaluator）处理逻辑运算。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>存储一个 {@code boolean} 值</li>
 *   <li>通过 {@link #type()} 标记对象类型为 {@code BOOLEAN_OBJ}</li>
 *   <li>通过 {@link #inspect()} 返回布尔值的字符串形式</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>
 * MonkeyBoolean t = new MonkeyBoolean(true);
 * System.out.println(t.type());    // 输出：BOOLEAN
 * System.out.println(t.inspect()); // 输出："true"
 *
 * MonkeyBoolean f = new MonkeyBoolean(false);
 * System.out.println(f.inspect()); // 输出："false"
 * </pre>
 *
 * @see MonkeyObject
 * @see ObjectType#BOOLEAN_OBJ
 */
public class MonkeyBoolean implements MonkeyObject {

    // 单例实例
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
}
