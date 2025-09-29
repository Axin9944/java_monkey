package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

/**
 * {@code MonkeyError} 表示 Monkey 解释器执行过程中出现的错误对象。
 *
 * <p>在解释器运行时，如果遇到语法错误、类型错误或运行时错误，
 * 会生成一个 {@code MonkeyError} 实例，并携带错误信息。</p>
 *
 * <p>主要特性：</p>
 * <ul>
 *   <li>类型标记为 {@link ObjectType#ERROR_OBJ}</li>
 *   <li>内部保存一条错误消息（{@code message}）</li>
 *   <li>{@link #inspect()} 方法返回格式化后的错误信息</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>
 * MonkeyError err = new MonkeyError("type mismatch: INTEGER + BOOLEAN");
 *
 * System.out.println(err.type());    // 输出：ERROR
 * System.out.println(err.inspect()); // 输出："ERROR: type mismatch: INTEGER + BOOLEAN"
 * </pre>
 *
 * @see MonkeyObject
 * @see ObjectType#ERROR_OBJ
 */
public class MonkeyError implements MonkeyObject {

    /** 错误信息的文本内容 */
    private String message;

    /**
     * 获取错误消息。
     *
     * @return 错误消息字符串
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置错误消息。
     *
     * @param message 错误消息字符串
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 构造一个新的 {@code MonkeyError}。
     *
     * @param message 错误消息字符串
     */
    public MonkeyError(String message) {
        this.message = message;
    }

    /**
     * 返回对象的类型标记。
     *
     * @return {@link ObjectType#ERROR_OBJ}
     */
    @Override
    public ObjectType type() {
        return ObjectType.ERROR_OBJ;
    }

    /**
     * 返回该错误的字符串表示形式。
     *
     * @return 格式为 {@code "ERROR: <message>"} 的字符串
     */
    @Override
    public String inspect() {
        return "ERROR: " + message;
    }
}
