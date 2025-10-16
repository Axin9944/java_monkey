package com.linewell.monkey.object;

/**
 * 表示 Monkey 语言哈希对象（{@link com.linewell.monkey.object.imp.MonkeyHash}）
 * 中存储的键值对（Hash Pair）。
 * <p>
 * 每个 {@code HashPair} 保存了哈希表中单个条目的原始键和值：
 * <ul>
 *   <li>{@code key} —— 对应哈希表中的键对象（必须实现 {@link Hashable}）；</li>
 *   <li>{@code value} —— 与该键对应的值对象。</li>
 * </ul>
 *
 * <p>在 Monkey 解释器内部，哈希对象通常以如下形式存储：
 * <pre>
 * Map&lt;HashKey, HashPair&gt;
 * </pre>
 * 其中：
 * <ul>
 *   <li>{@link HashKey} 用作实际的查找键（基于对象类型与哈希值）；</li>
 *   <li>{@link HashPair} 保留原始的 {@link MonkeyObject} 键和值，
 *       以便在求值结果打印、比较或序列化时能完整还原。</li>
 * </ul>
 *
 * <p><b>示例：</b>
 * <pre>
 * {"one": 1, "two": 2}
 * </pre>
 * 在内部可能表示为：
 * <pre>
 * {
 *   HashKey(STRING, 0x...) -> HashPair(key="one", value=1),
 *   HashKey(STRING, 0x...) -> HashPair(key="two", value=2)
 * }
 * </pre>
 *
 * @see com.linewell.monkey.object.HashKey
 * @see com.linewell.monkey.object.Hashable
 * @see com.linewell.monkey.object.imp.MonkeyHash
 */
public class HashPair {

    /** 哈希表中该条目的原始键对象。 */
    private final MonkeyObject key;

    /** 与键对应的值对象。 */
    private MonkeyObject value;

    /** 获取键对象。 */
    public MonkeyObject getKey() {
        return key;
    }

    /** 获取值对象。 */
    public MonkeyObject getValue() {
        return value;
    }

    /**
     * 设置值对象
     *
     * @param value 当键重复时，覆盖掉之前 键的对象
     */
    public void setValue(MonkeyObject value) {
        this.value = value;
    }

    /**
     * 创建一个哈希键值对。
     *
     * @param key   原始键对象（必须可哈希）
     * @param value 键对应的值对象
     */
    public HashPair(MonkeyObject key, MonkeyObject value) {
        this.key = key;
        this.value = value;
    }
}
