package com.linewell.monkey.object;

/**
 * 函数式接口：Monkey 语言中的内置函数统一接口。
 * <p>
 * 所有内置函数（如 len、first、last 等）都应实现该接口，
 * 并通过 {@link com.linewell.monkey.object.imp.MonkeyBuiltin} 封装。
 * </p>
 *
 * @author axin
 */
@FunctionalInterface
public interface BuiltinFunction {

    /**
     * 执行内置函数逻辑。
     *
     * @param args 传入的参数（可变参数形式）
     * @return 执行结果，类型为 {@link MonkeyObject}
     */
    MonkeyObject execute(MonkeyObject... args);
}
