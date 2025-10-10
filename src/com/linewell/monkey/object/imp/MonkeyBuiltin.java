package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.BuiltinFunction;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

import java.util.HashMap;
import java.util.Map;

/**
 * 内置函数对象（MonkeyBuiltin）类。
 * <p>
 * 该类用于封装 Monkey 语言中的内置函数（Built-in Functions），
 * 如 {@code len()} 等。每个内置函数通过 {@link BuiltinFunction} 定义逻辑，
 * 并注册到静态 {@code BUILTINS} 表中。
 * </p>
 *
 * <p>示例：</p>
 * <pre>
 * MonkeyBuiltin lenFn = MonkeyBuiltin.getBUILTINS().get("len");
 * MonkeyObject result = lenFn.call(new MonkeyString("hello"));
 * </pre>
 */
public class MonkeyBuiltin implements MonkeyObject {

    /** 内置函数的实际执行逻辑（函数式接口实现） */
    private BuiltinFunction fn;

    /** 所有已注册的内置函数映射表（函数名 → MonkeyBuiltin 实例） */
    private static Map<String, MonkeyBuiltin> BUILTINS = new HashMap<>();

    // === 内置函数注册区域 ===
    static {
        // 注册 len 函数：计算字符串长度
        BUILTINS.put("len", new MonkeyBuiltin(args -> {
            // 参数数量检查：len 仅接受一个参数
            if (args == null || args.length != 1) {
                return new MonkeyError(String.format(
                        "wrong number of arguments. got=%d, want=1",
                        args != null ? args.length : 0));
            }

            MonkeyObject arg = args[0];

            // 参数类型检查：目前仅支持字符串类型
            if (arg instanceof MonkeyString) {
                MonkeyString monkeyString = (MonkeyString) arg;
                String value = monkeyString.getValue();
                // 返回字符串长度对应的整数对象
                return new MonkeyInteger(value.length());
            } else {
                // 非字符串类型，返回错误对象
                return new MonkeyError(String.format(
                        "argument to 'len' not supported, got='%s'",
                        arg.type()
                ));
            }
        }));
    }

    /**
     * 获取所有注册的内置函数映射表。
     *
     * @return 内置函数名到实例的映射
     */
    public static Map<String, MonkeyBuiltin> getBUILTINS() {
        return BUILTINS;
    }

    /**
     * 获取内置函数的函数式接口。
     *
     * @return {@link BuiltinFunction} 实例
     */
    public BuiltinFunction getFn() {
        return fn;
    }

    /**
     * 设置内置函数的函数式接口。
     *
     * @param fn 内置函数逻辑定义
     */
    public void setFn(BuiltinFunction fn) {
        this.fn = fn;
    }

    public MonkeyBuiltin() {
    }

    public MonkeyBuiltin(BuiltinFunction fn) {
        this.fn = fn;
    }

    /**
     * 调用内置函数。
     *
     * @param args 调用时传入的参数
     * @return 执行结果
     */
    public MonkeyObject call(MonkeyObject... args) {
        return fn.execute(args);
    }

    @Override
    public ObjectType type() {
        return ObjectType.BUILTIN_OBJ;
    }

    @Override
    public String inspect() {
        return "builtin function";
    }
}
