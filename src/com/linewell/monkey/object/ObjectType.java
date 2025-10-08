package com.linewell.monkey.object;

/**
 * {@code ObjectType} 枚举定义了 Monkey 解释器中所有运行时对象的类型。
 *
 * <p>在解释执行过程中，每个 Monkey 对象（如整数、布尔值、返回值等）
 * 都会有一个对应的 {@code ObjectType} 类型标记，方便运行时判断对象类别，
 * 以及在错误提示中输出。</p>
 *
 * <p>典型使用场景：</p>
 * <ul>
 *   <li>在 {@code MonkeyObject} 抽象类或接口的实现中，返回该对象所属的类型。</li>
 *   <li>在求值器（Evaluator）中，根据不同的类型执行对应的运算逻辑。</li>
 *   <li>在错误信息中输出对象类型，以帮助调试。</li>
 * </ul>
 */
public enum ObjectType {

    /** 整数类型对象，例如 {@code 1}, {@code 100} */
    INTEGER_OBJ("INTEGER"),

    /** 布尔类型对象，例如 {@code true}, {@code false} */
    BOOLEAN_OBJ("BOOLEAN"),

    /** 空对象，表示无值（相当于 {@code null}） */
    NULL_OBJ("NULL"),

    /**
     * 函数返回值对象。
     * <p>在解释执行函数时，会用 {@code RETURN_VALUE_OBJ} 包裹返回结果，
     * 直到退出函数作用域时再解包。</p>
     */
    RETURN_VALUE_OBJ("RETURN_VALUE"),

    /**
     * 函数对象。
     * <p>
     * 表示用户在 Monkey 语言中定义的函数（例如 {@code fn(x) { x + 2; }})。
     * 在运行时，函数会被封装为 {@code FUNCTION_OBJ} 类型，
     * 并携带其参数列表、函数体（语法树）以及定义时的环境（闭包上下文）。
     * </p>
     *
     * <p><b>典型用途：</b></p>
     * <ul>
     *   <li>在求值器（Evaluator）中，用于函数定义与函数调用。</li>
     *   <li>支持闭包（Closure）特性，函数可引用定义时环境中的变量。</li>
     * </ul>
     */
    FUNCTION_OBJ("FUNCTION"),

    /**
     * 字符串对象。
     * <p>
     * 表示 Monkey 语言中的字符串字面量（例如 {@code "Hello World"}）。
     * 该类型对象在解释执行阶段可参与字符串拼接等操作。
     * </p>
     *
     * <p><b>典型用途：</b></p>
     * <ul>
     *   <li>在词法分析器中由 {@code TokenType.STRING} 生成。</li>
     *   <li>在语法树中对应 {@code StringLiteral} 节点。</li>
     *   <li>在求值器中用于字符串连接（例如 {@code "a" + "b"} → {@code "ab"}）。</li>
     * </ul>
     */
    STRING_OBJ("STRING"),

    /**
     * 错误对象。
     * <p>用于表示运行时错误，如运算符类型不匹配、未定义的标识符等。</p>
     */
    ERROR_OBJ("ERROR");

    private final String literal;

    ObjectType(String literal) {
        this.literal = literal;
    }

    /**
     * 获取该对象类型对应的字符串字面量。
     *
     * @return 类型的字符串表示，例如 {@code "INTEGER"}、{@code "BOOLEAN"}
     */
    @Override
    public String toString() {
        return literal;
    }
}
