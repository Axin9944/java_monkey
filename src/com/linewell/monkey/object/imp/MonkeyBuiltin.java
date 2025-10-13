package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.BuiltinFunction;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;
import com.sun.javafx.binding.StringFormatter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 内置函数对象（MonkeyBuiltin）类。
 * <p>
 * 该类用于封装 Monkey 语言中的内置函数（Built-in Functions），
 * 例如 {@code len()}、{@code first()}、{@code last()}、{@code rest()}、{@code push()} 等。
 * 每个内置函数通过 {@link BuiltinFunction} 定义具体逻辑，
 * 并在解释器初始化阶段注册到静态 {@code BUILTINS} 表中。
 * </p>
 *
 * <p><b>支持的内置函数及功能：</b></p>
 * <ul>
 *   <li>{@code len(arg)}：
 *       返回字符串或数组的长度，若参数类型不支持则返回 {@link MonkeyError}。</li>
 *   <li>{@code first(arr)}：
 *       返回数组 {@code arr} 的第一个元素，如果数组为空则返回 {@code null}；
 *       参数非数组时返回错误对象。</li>
 *   <li>{@code last(arr)}：
 *       返回数组 {@code arr} 的最后一个元素，如果数组为空则返回 {@code null}；
 *       参数非数组时返回错误对象。</li>
 *   <li>{@code rest(arr)}：
 *       返回一个新的数组对象，包含原数组中除第一个元素外的所有元素；
 *       若数组为空则返回 {@code null}，参数非数组时返回错误对象。</li>
 *   <li>{@code push(arr, obj)}：
 *       返回一个新的数组对象，将 {@code obj} 添加到数组 {@code arr} 的末尾；
 *       参数非数组时返回错误对象。</li>
 * </ul>
 *
 * <p><b>典型用途：</b></p>
 * <ul>
 *   <li>在解释器初始化阶段注册常用内置函数。</li>
 *   <li>在标识符求值（{@code evalIdentifier}）时从内置函数表中查找函数对象。</li>
 *   <li>在函数调用求值（{@code applyFunction}）时直接执行内置函数逻辑。</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>
 * MonkeyBuiltin lenFn = MonkeyBuiltin.getBUILTINS().get("len");
 * MonkeyObject result = lenFn.call(new MonkeyString("hello")); // 返回 5
 *
 * MonkeyBuiltin firstFn = MonkeyBuiltin.getBUILTINS().get("first");
 * result = firstFn.call(new MonkeyArray(new MonkeyObject[]{new MonkeyInteger(1), new MonkeyInteger(2)})); // 返回 1
 * </pre>
 *
 * <p>说明：该类仅封装内置函数逻辑，不依赖用户自定义函数。</p>
 *
 * @author axin
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
              // 新增支持数组类型
            } else if(arg instanceof MonkeyArray){
                MonkeyArray arr = (MonkeyArray) arg;
                // 返回数组长度对象的整数对像
                return new MonkeyInteger(arr.getElements().length);
            } else {
                // 非字符串类型，返回错误对象
                return new MonkeyError(String.format(
                        "argument to 'len' not supported, got='%s'",
                        arg.type()
                ));
            }
        }));
        // 注册 first 函数：返回数组第一个对象
        BUILTINS.put("first", new MonkeyBuiltin(args -> {
            // 参数数量检查：first 仅接受一个参数
            if (args == null || args.length != 1) {
                return new MonkeyError(String.format("wrong number of arguments. got=%d, want=1",
                        args != null ? args.length : 0));
            }

            MonkeyObject arg = args[0];

            if (!(arg instanceof MonkeyArray)) {
                return new MonkeyError(String.format("argument to `first` must be ARRAY, got %s",
                        arg.type()));
            }

            MonkeyArray arr = (MonkeyArray) arg;
            if (arr.getElements().length > 0) {
                return arr.getElements()[0];
            }

            return null;
        }));
        // 注册last 函数， 返回数组最后一个对象
        BUILTINS.put("last", new MonkeyBuiltin(args -> {
            // 参数数量检查：last 仅接受一个参数
            if (args == null || args.length != 1) {
                return new MonkeyError(String.format("wrong number of arguments. got=%d, want=1",
                        args != null ? args.length : 0));
            }

            MonkeyObject arg = args[0];

            if (!(arg instanceof MonkeyArray)) {
                return new MonkeyError(String.format("argument to `last` must be ARRAY, got %s",
                        arg.type()));
            }

            MonkeyArray arr = (MonkeyArray) arg;
            if (arr.getElements().length > 0) {
                return arr.getElements()[arr.getElements().length - 1];
            }

            return null;
        }));
        // 注册 rest 函数，返回一个新的数组，包含原数组中除了第一个元素以外的所有元素。
        BUILTINS.put("rest", new MonkeyBuiltin(args -> {
            // 参数数量检查：rest 仅接受一个参数
            if (args == null || args.length != 1) {
                return new MonkeyError(String.format("wrong number of arguments. got=%d, want=1",
                        args != null ? args.length : 0));
            }

            MonkeyObject arg = args[0];

            if (!(arg instanceof MonkeyArray)) {
                return new MonkeyError(String.format("argument to `rest` must be ARRAY, got %s",
                        arg.type()));
            }

            MonkeyArray arr = (MonkeyArray) arg;
            MonkeyObject[] elements = arr.getElements();
            int len = elements.length;
            if (len > 0) {
                MonkeyObject[] newElements = Arrays.copyOfRange(elements, 1, len);
                return new MonkeyArray(newElements);
            }

            return null;
        }));
        // 注册 push 函数，将元素加入至数组的最后一位
        BUILTINS.put("push", new MonkeyBuiltin(args -> {
            // 参数数量检查：push 接受2个参数
            if (args == null || args.length != 2) {
                return new MonkeyError(String.format("wrong number of arguments. got=%d, want=1",
                        args != null ? args.length : 0));
            }

            MonkeyObject arg = args[0];

            if (!(arg instanceof MonkeyArray)) {
                return new MonkeyError(String.format("argument to `push` must be ARRAY, got %s",
                        arg.type()));
            }

            MonkeyArray arr = (MonkeyArray) arg;
            MonkeyObject[] elements = arr.getElements();
            int len = elements.length;

            MonkeyObject[] newElements = Arrays.copyOf(elements, len + 1);
            newElements[len] = args[1];

            return new MonkeyArray(newElements);
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
