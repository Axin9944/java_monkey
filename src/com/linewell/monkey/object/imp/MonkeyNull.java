package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

/**
 * {@code MonkeyNull} 表示 Monkey 语言中的空值（null）。
 *
 * <p>在 Monkey 解释器中，用于表示：
 * <ul>
 *   <li>没有返回值的表达式</li>
 *   <li>{@code if} 表达式条件不成立且没有 else 分支时的结果</li>
 *   <li>未定义变量的默认占位符（某些情况）</li>
 * </ul>
 *
 * <p>其行为类似于 Java 中的 {@code null}，
 * 但在解释器实现中被显式封装成一个对象类型。</p>
 *
 * <p>主要特性：</p>
 * <ul>
 *   <li>类型标记固定为 {@link ObjectType#NULL_OBJ}</li>
 *   <li>{@link #inspect()} 方法返回字符串 {@code "null"}</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>
 * MonkeyNull nullObj = new MonkeyNull();
 *
 * System.out.println(nullObj.type());    // 输出：NULL
 * System.out.println(nullObj.inspect()); // 输出："null"
 * </pre>
 *
 * @see MonkeyObject
 * @see ObjectType#NULL_OBJ
 */
public class MonkeyNull implements MonkeyObject {

    // 单例实例
    public static final MonkeyNull NULL = new MonkeyNull();

    private MonkeyNull() {}

    /**
     * 返回对象的类型标记。
     *
     * @return {@link ObjectType#NULL_OBJ}
     */
    @Override
    public ObjectType type() {
        return ObjectType.NULL_OBJ;
    }

    /**
     * 返回该对象的字符串表示形式。
     *
     * @return {@code "null"}
     */
    @Override
    public String inspect() {
        return "null";
    }
}
