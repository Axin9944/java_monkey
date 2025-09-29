package com.linewell.monkey.object;

/**
 * {@code MonkeyObject} 是 Monkey 解释器中所有运行时对象的统一接口。
 *
 * <p>每一个在 Monkey 程序运行时产生的值（如整数、布尔值、null、函数返回值、错误对象等），
 * 都会实现该接口，以便在解释器求值（Evaluator）和环境（Environment）中被统一处理。</p>
 *
 * <p>接口定义了两个核心方法：</p>
 * <ul>
 *   <li>{@link #type()} —— 返回该对象的 {@link ObjectType} 类型标记，用于运行时类型判断。</li>
 *   <li>{@link #inspect()} —— 返回对象的字符串形式，用于调试、错误输出或 REPL 的展示。</li>
 * </ul>
 *
 * <p>例如：</p>
 * <pre>
 * MonkeyObject obj = new MonkeyInteger(10);
 * System.out.println(obj.type());    // 输出：INTEGER
 * System.out.println(obj.inspect()); // 输出："10"
 * </pre>
 *
 * @see ObjectType
 */
public interface MonkeyObject {

    /**
     * 获取该对象的类型。
     *
     * @return 该对象的 {@link ObjectType}，例如 {@code INTEGER_OBJ}、{@code BOOLEAN_OBJ}
     */
    ObjectType type();

    /**
     * 返回对象的字符串表示形式。
     *
     * <p>该方法通常用于调试、打印，或者在 REPL 中向用户展示结果。</p>
     *
     * @return 对象的可读字符串表示，例如：
     *         <ul>
     *           <li>{@code "10"} —— 对应整数对象</li>
     *           <li>{@code "true"} —— 对应布尔对象</li>
     *           <li>{@code "null"} —— 对应空对象</li>
     *         </ul>
     */
    String inspect();
}
