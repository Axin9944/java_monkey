package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.HashKey;
import com.linewell.monkey.object.HashPair;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@code MonkeyHash} 表示 Monkey 语言中的哈希对象（Hash Object）。
 *
 * <p>该类实现了 {@link MonkeyObject} 接口，
 * 是运行时环境中用于存储哈希字面量（如 {@code {"one": 1, "two": 2}}）的核心数据结构。</p>
 *
 * <h3>结构说明：</h3>
 * <ul>
 *   <li>内部使用 {@code Map<HashKey, HashPair>} 存储键值对。</li>
 *   <li>{@link HashKey} 是根据键对象（如 {@code MonkeyString}, {@code MonkeyInteger}, {@code MonkeyBoolean}）
 *       通过 {@link com.linewell.monkey.object.Hashable#HashKey()} 方法生成的哈希键。</li>
 *   <li>{@link HashPair} 保存了原始键对象与对应的值对象。</li>
 * </ul>
 *
 * <h3>求值过程：</h3>
 * <ol>
 *   <li>在解析哈希字面量时，解析器构建 {@link com.linewell.monkey.ast.imp.HashLiteral} 节点。</li>
 *   <li>求值器（Evaluator）遍历键值表达式，计算后生成对应的 {@link HashKey} 和 {@link HashPair}。</li>
 *   <li>最后封装成一个 {@code MonkeyHash} 对象并返回。</li>
 * </ol>
 *
 * <h3>示例：</h3>
 * <pre>
 * MonkeyString key1 = new MonkeyString("name");
 * MonkeyInteger value1 = new MonkeyInteger(100);
 *
 * Map&lt;HashKey, HashPair&gt; map = new HashMap&lt;&gt;();
 * map.put(key1.HashKey(), new HashPair(key1, value1));
 *
 * MonkeyHash hash = new MonkeyHash(map);
 * System.out.println(hash.inspect());
 * // 输出: {"name" : 100}
 * </pre>
 *
 * <h3>方法说明：</h3>
 * <ul>
 *   <li>{@link #getPairs()} / {@link #setPairs(Map)}：访问或修改哈希表内容。</li>
 *   <li>{@link #inspect()}：返回可读字符串形式，常用于 REPL 或调试输出。</li>
 *   <li>{@link #type()}：返回对象类型 {@link ObjectType#HASH_OBJ}。</li>
 * </ul>
 *
 * @see com.linewell.monkey.object.HashKey
 * @see com.linewell.monkey.object.HashPair
 * @see com.linewell.monkey.object.Hashable
 * @see com.linewell.monkey.ast.imp.HashLiteral
 * @see ObjectType#HASH_OBJ
 */
public class MonkeyHash implements MonkeyObject {

    /** 存储哈希表的键值映射关系。 */
    private Map<HashKey, HashPair> pairs;

    /** 获取哈希表的键值对映射。 */
    public Map<HashKey, HashPair> getPairs() {
        return pairs;
    }

    /** 设置哈希表的键值对映射。 */
    public void setPairs(Map<HashKey, HashPair> pairs) {
        this.pairs = pairs;
    }

    public MonkeyHash() {
    }

    public MonkeyHash(Map<HashKey, HashPair> pairs) {
        this.pairs = pairs;
    }

    /** 返回对象类型标识。 */
    @Override
    public ObjectType type() {
        return ObjectType.HASH_OBJ;
    }

    /**
     * 返回哈希对象的可读字符串表示。
     * <p>格式类似：{@code {"key1" : value1, "key2" : value2}}。</p>
     *
     * @return 格式化的哈希内容字符串
     */
    @Override
    public String inspect() {

        StringBuilder sb = new StringBuilder();

        sb.append("{");
        if (pairs != null) {
            String p = pairs.values().stream()
                    .map(pair -> pair.getKey().inspect() + " : " +
                            pair.getValue().inspect())
                    .collect(Collectors.joining(", "));
            sb.append(p);
        }
        sb.append("}");

        return sb.toString();
    }
}
