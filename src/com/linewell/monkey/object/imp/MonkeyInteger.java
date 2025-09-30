package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

import java.util.Objects;

/**
 * {@code MonkeyInteger} 表示 Monkey 语言中的整数对象。
 *
 * <p>该类实现了 {@link MonkeyObject} 接口，
 * 是运行时环境中用于存储整型字面量（如 {@code 1}, {@code 42}, {@code -100}）的封装对象。</p>
 *
 * <p>在解释执行过程中，解析器（Parser）将源码中的整数字面量
 * 转换为 {@code MonkeyInteger}，并交由求值器（Evaluator）计算。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>存储一个 {@code long} 类型的整数值</li>
 *   <li>提供 {@link #type()} 方法标记其对象类型为 {@code INTEGER_OBJ}</li>
 *   <li>提供 {@link #inspect()} 方法，返回整数的字符串形式</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>
 * MonkeyInteger num = new MonkeyInteger(123);
 * System.out.println(num.type());    // 输出：INTEGER
 * System.out.println(num.inspect()); // 输出："123"
 * </pre>
 *
 * @see MonkeyObject
 * @see ObjectType#INTEGER_OBJ
 */
public class MonkeyInteger implements MonkeyObject {

    /** 存储整数值（对应 Monkey 语言中的整数字面量） */
    private long value;

    /**
     * 获取整数值。
     *
     * @return 存储的 long 值
     */
    public long getValue() {
        return value;
    }

    /**
     * 构造函数，创建一个新的整数对象。
     *
     * @param value 整数值
     */
    public MonkeyInteger(long value) {
        this.value = value;
    }

    /**
     * 设置整数值。
     *
     * @param value 新的 long 值
     */
    public void setValue(long value) {
        this.value = value;
    }

    /**
     * 获取对象的类型标记。
     *
     * @return {@link ObjectType#INTEGER_OBJ}
     */
    @Override
    public ObjectType type() {
        return ObjectType.INTEGER_OBJ;
    }

    /**
     * 返回整数的字符串表示。
     *
     * @return 整数的字符串形式，例如 {@code "123"}
     */
    @Override
    public String inspect() {
        return Long.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MonkeyInteger)) return false;
        MonkeyInteger that = (MonkeyInteger) o;
        return value == that.value;
    }
}
