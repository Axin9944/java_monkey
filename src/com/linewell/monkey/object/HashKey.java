package com.linewell.monkey.object;

import java.util.Objects;

/**
 * 表示可哈希对象（{@link Hashable}）在 Monkey 解释器中的哈希键。
 * <p>
 * 每个哈希键由对象的类型（{@link ObjectType}）和一个 64 位整型哈希值组成，
 * 用于在运行时哈希表（{@code MonkeyHash}）中唯一标识键对象。
 * </p>
 *
 * <p>哈希值通常由对象自身内容计算得到，例如：
 * <ul>
 *   <li>整数对象的值本身；</li>
 *   <li>布尔对象的常量值（true/false）；</li>
 *   <li>字符串对象通过 {@link #fnv1a64(String)} 计算出的 FNV-1a 64 位哈希。</li>
 * </ul>
 *
 * <p>此类同时重写了 {@link #equals(Object)} 和 {@link #hashCode()}，
 * 以确保不同对象类型与哈希值组合时能够正确比较。
 *
 * <p><b>示例：</b>
 * <pre>
 * HashKey key1 = new HashKey(123L, ObjectType.INTEGER_OBJ);
 * HashKey key2 = new HashKey(HashKey.fnv1a64("hello"), ObjectType.STRING_OBJ);
 * </pre>
 *
 * @see com.linewell.monkey.object.Hashable
 * @see com.linewell.monkey.object.imp.MonkeyHash
 */
public class HashKey {

    /** 对象类型，例如 INTEGER、BOOLEAN、STRING 等。 */
    private ObjectType type;

    /** 该类型对象对应的 64 位哈希值。 */
    private final long value;

    /** 获取对象类型。 */
    public ObjectType getType() {
        return type;
    }

    /** 获取哈希值。 */
    public long getValue() {
        return value;
    }

    /**
     * 创建一个哈希键。
     *
     * @param value 哈希值
     * @param type  对象类型
     */
    public HashKey(long value, ObjectType type) {
        this.value = value;
        this.type = type;
    }

    /**
     * 计算字符串的 FNV-1a 64 位哈希值。
     * <p>
     * 该算法具备速度快、冲突率低的特点，
     * 适用于解释器中字符串键的哈希计算。
     *
     * @param input 输入字符串
     * @return 计算得到的 64 位哈希值
     */
    public static long fnv1a64(String input) {
        final long FNV_64_PRIME = 0x100000001b3L;
        final long FNV_64_OFFSET_BASIS = 0xcbf29ce484222325L;
        long hash = FNV_64_OFFSET_BASIS;
        for (byte b : input.getBytes()) {
            hash ^= (b & 0xff);
            hash *= FNV_64_PRIME;
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashKey)) return false;
        HashKey other = (HashKey) o;
        return value == other.value && type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "HashKey{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }
}
