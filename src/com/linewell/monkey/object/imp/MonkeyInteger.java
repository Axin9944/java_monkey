package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.HashKey;
import com.linewell.monkey.object.Hashable;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

import java.util.Objects;

/**
 * {@code MonkeyInteger} 表示 Monkey 语言中的整数对象。
 *
 * <p>该类实现了 {@link MonkeyObject} 与 {@link Hashable} 接口，
 * 是运行时环境中用于存储整数字面量（如 {@code 1}, {@code 42}, {@code -100}）的封装对象。</p>
 *
 * <h3>哈希支持：</h3>
 * <p>整数对象可直接作为哈希表键。
 * {@link #HashKey()} 方法使用整数值本身作为哈希值，
 * 并附加对象类型标识 {@code INTEGER_OBJ}。</p>
 *
 * <p>示例：</p>
 * <pre>
 * MonkeyInteger one = new MonkeyInteger(1);
 * System.out.println(one.HashKey());
 * // 输出: HashKey{type=INTEGER_OBJ, value=1}
 * </pre>
 *
 * @see MonkeyObject
 * @see Hashable
 * @see com.linewell.monkey.object.HashKey
 * @see ObjectType#INTEGER_OBJ
 */
public class MonkeyInteger implements MonkeyObject, Hashable {

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

    /**
     * 生成整数对象的哈希键。
     * <p>哈希值等于整数的数值本身，类型为 {@code INTEGER_OBJ}。</p>
     */
    @Override
    public HashKey HashKey() {
        return new HashKey(value, type());
    }
}
