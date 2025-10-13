package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 表示 Monkey 语言中的数组对象（Array Object）。
 * <p>
 * 数组是 Monkey 语言中的一种复合数据类型，能够保存多个 {@link MonkeyObject} 元素。
 * 例如，以下 Monkey 代码：
 * <pre>
 *     let arr = [1, 2 * 2, fn(x) { x + 1 }];
 * </pre>
 * 在解释执行时会被解析为一个 {@code MonkeyArray} 实例，
 * 其中 {@code elements} 数组保存三个不同类型的元素（整数与函数对象）。
 *
 * <p>
 * 本类主要用于解释器（Evaluator）的运行时表示，用于：
 * <ul>
 *     <li>在求值数组字面量（array literal）表达式时构建数组对象；</li>
 *     <li>在执行数组索引操作（如 {@code arr[0]}）时访问对应的元素；</li>
 *     <li>在调试或打印时通过 {@link #inspect()} 生成可读的字符串表示。</li>
 * </ul>
 *
 * <p>
 * 例如：
 * <pre>
 *     输入: [1, true, "hello"]
 *     输出: [1, true, "hello"]
 * </pre>
 *
 * @author axin
 * @since 1.0
 */
public class MonkeyArray implements MonkeyObject {

    private MonkeyObject[] elements;

    public MonkeyObject[] getElements() {
        return elements;
    }

    public void setElements(MonkeyObject[] elements) {
        this.elements = elements;
    }

    public MonkeyArray(MonkeyObject[] elements) {
        this.elements = elements;
    }

    public MonkeyArray() {
    }

    @Override
    public ObjectType type() {
        return ObjectType.ARRAY_OBJ;
    }

    @Override
    public String inspect() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        if (elements != null && elements.length > 0) {
            String collect = Arrays.stream(elements)
                    .map(MonkeyObject::inspect)
                    .collect(Collectors.joining(", "));

            sb.append(collect);
        }
        sb.append("]");

        return sb.toString();
    }
}
