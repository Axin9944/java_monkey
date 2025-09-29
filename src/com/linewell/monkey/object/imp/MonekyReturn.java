package com.linewell.monkey.object.imp;

import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.ObjectType;

/**
 * {@code MonekyReturn} 表示 Monkey 语言中的返回值对象。
 *
 * <p>在解释执行过程中，当遇到 {@code return} 语句时，
 * 会将其计算结果包装成 {@code MonekyReturn} 对象，
 * 从而在执行块语句或函数体时，能够正确地中断执行并返回结果。</p>
 *
 * <p>它本质上是一个对真实返回值的包装（wrapper），
 * 内部保存一个 {@link MonkeyObject} 类型的值。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>封装函数或语句块中的返回值</li>
 *   <li>通过 {@link #type()} 标记对象类型为 {@code RETURN_VALUE_OBJ}</li>
 *   <li>通过 {@link #inspect()} 输出其内部值的字符串形式</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>
 * MonkeyInteger resultValue = new MonkeyInteger(10);
 * MonekyReturn returnObj = new MonekyReturn(resultValue);
 *
 * System.out.println(returnObj.type());    // 输出：RETURN_VALUE
 * System.out.println(returnObj.inspect()); // 输出："10"
 * </pre>
 *
 * @see MonkeyObject
 * @see ObjectType#RETURN_VALUE_OBJ
 */
public class MonekyReturn implements MonkeyObject {

    /** 封装的返回值对象 */
    private MonkeyObject value;

    /**
     * 获取封装的返回值。
     *
     * @return 内部的 {@link MonkeyObject}
     */
    public MonkeyObject getValue() {
        return value;
    }

    /**
     * 设置封装的返回值。
     *
     * @param value 新的返回值对象
     */
    public void setValue(MonkeyObject value) {
        this.value = value;
    }

    /**
     * 构造函数，创建一个 {@code MonekyReturn} 对象。
     *
     * @param value 返回值对象
     */
    public MonekyReturn(MonkeyObject value) {
        this.value = value;
    }

    /**
     * 获取对象的类型标记。
     *
     * @return {@link ObjectType#RETURN_VALUE_OBJ}
     */
    @Override
    public ObjectType type() {
        return ObjectType.RETURN_VALUE_OBJ;
    }

    /**
     * 返回内部值的字符串表示。
     *
     * @return 内部 {@link MonkeyObject} 的 {@code inspect()} 结果
     */
    @Override
    public String inspect() {
        return value.inspect();
    }
}
