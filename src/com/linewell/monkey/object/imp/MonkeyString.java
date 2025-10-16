package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.HashKey;
import com.linewell.monkey.object.Hashable;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

/**
 * {@code MonkeyString} 表示 Monkey 语言中的字符串对象。
 *
 * <p>该类实现了 {@link MonkeyObject} 与 {@link Hashable} 接口，
 * 是运行时环境中字符串字面量（如 {@code "hello"}）的封装类型。</p>
 *
 * <h3>哈希支持：</h3>
 * <p>字符串对象可以作为哈希表的键。
 * {@link #HashKey()} 方法使用 FNV-1a 64 位哈希算法
 * （由 {@link HashKey#fnv1a64(String)} 实现）计算字符串内容的哈希值，
 * 并结合对象类型标识 {@code STRING_OBJ} 生成唯一键。</p>
 *
 * <p>这种方式确保了不同字符串即使内容相似也能拥有唯一哈希值。</p>
 *
 * <h3>示例：</h3>
 * <pre>
 * MonkeyString s = new MonkeyString("foo");
 * System.out.println(s.inspect());  // 输出: foo
 * System.out.println(s.HashKey());  // 输出: HashKey{type=STRING_OBJ, value=0x...}
 * </pre>
 *
 * @see MonkeyObject
 * @see Hashable
 * @see com.linewell.monkey.object.HashKey
 * @see ObjectType#STRING_OBJ
 */
public class MonkeyString implements MonkeyObject, Hashable {

    /** 字符串的实际值（不包含引号） */
    private String value;

    /** 使用给定字符串值创建运行时字符串对象。 */
    public MonkeyString(String value) {
        this.value = value;
    }

    /** 无参构造函数（主要用于反射或序列化场景）。 */
    public MonkeyString() {
    }

    /** 获取字符串的实际内容。 */
    public String getValue() {
        return value;
    }

    /** 设置字符串的实际内容。 */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 返回该对象的运行时类型标识。
     *
     * @return {@link ObjectType#STRING_OBJ}
     */
    @Override
    public ObjectType type() {
        return ObjectType.STRING_OBJ;
    }

    /**
     * 返回字符串对象的可打印表示。
     * <p>在 REPL、日志或调试输出中直接显示字符串内容。</p>
     *
     * @return 字符串的文本内容
     */
    @Override
    public String inspect() {
        return value;
    }

    /**
     * 生成字符串对象的哈希键。
     * <p>使用 FNV-1a 64 位哈希函数确保字符串键的唯一性与稳定性。</p>
     */
    @Override
    public HashKey HashKey() {
        return new HashKey(HashKey.fnv1a64(value), type());
    }
}
