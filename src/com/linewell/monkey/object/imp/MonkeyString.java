package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

/**
 * 表示 Monkey 语言中的字符串对象（运行时类型：{@link ObjectType#STRING_OBJ}）。
 * <p>
 * 在解释执行阶段，当遇到字符串字面量（例如 {@code "Hello World"}）时，
 * 解析器会构造一个 {@code MonkeyString} 实例，用于在运行时环境中表示该字符串值。
 * </p>
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>封装字符串的实际内容（{@link #value}）。</li>
 *   <li>在 {@link #type()} 方法中返回运行时类型标识 {@code STRING_OBJ}。</li>
 *   <li>在 {@link #inspect()} 方法中返回字符串的可打印表示。</li>
 * </ul>
 *
 * <p><b>典型使用场景：</b></p>
 * <ul>
 *   <li>由语法树节点 {@code StringLiteral} 在求值阶段生成。</li>
 *   <li>在求值器（Evaluator）中参与字符串运算（如拼接）。</li>
 *   <li>在 REPL 输出或调试信息中用于展示字符串值。</li>
 * </ul>
 *
 * <p><b>示例：</b></p>
 * <pre>
 * MonkeyString str = new MonkeyString("Hello");
 * System.out.println(str.type());    // 输出: STRING_OBJ
 * System.out.println(str.inspect()); // 输出: Hello
 * </pre>
 *
 * @see com.linewell.monkey.object.MonkeyObject
 * @see com.linewell.monkey.object.ObjectType
 * @see com.linewell.monkey.ast.imp.StringLiteral
 */
public class MonkeyString implements MonkeyObject {

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
}
