package com.linewell.monkey.object;

/**
 * 定义可作为哈希表键（Hash Key）使用的运行时对象接口。
 * <p>
 * 在 Monkey 语言中，只有实现了 {@code Hashable} 接口的对象类型，
 * 才能作为哈希字面量（Hash Literal）的键，例如：
 * <pre>
 * {"name": "Monkey", "age": 3}
 * </pre>
 *
 * <p>典型实现包括：
 * <ul>
 *   <li>{@link com.linewell.monkey.object.imp.MonkeyInteger}</li>
 *   <li>{@link com.linewell.monkey.object.imp.MonkeyBoolean}</li>
 *   <li>{@link com.linewell.monkey.object.imp.MonkeyString}</li>
 * </ul>
 *
 * <p>每个实现类必须根据自身内容生成唯一且稳定的 {@link HashKey}，
 * 以保证哈希表（{@code MonkeyHash}）能够正确进行键的比较与查找。
 *
 * @see com.linewell.monkey.object.HashKey
 * @see com.linewell.monkey.object.imp.MonkeyHash
 */
public interface Hashable {

    /**
     * 返回该对象的哈希键（{@link HashKey}），
     * 用于在哈希表中唯一标识当前对象。
     *
     * @return 对应的 {@link HashKey} 实例
     */
    HashKey HashKey();
}
