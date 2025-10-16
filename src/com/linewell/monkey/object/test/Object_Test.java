package com.linewell.monkey.object.test;

import com.linewell.monkey.object.imp.MonkeyString;

/**
 * {@code Object_Test} 是一个用于测试 Monkey 语言对象系统中
 * {@link MonkeyString} 类型哈希键（HashKey）一致性的测试类。
 *
 * <p>主要验证内容包括：
 * <ul>
 *     <li>相同内容的字符串对象应当生成相同的哈希键；</li>
 *     <li>不同内容的字符串对象应当生成不同的哈希键。</li>
 * </ul>
 *
 * <p>该测试用例可用于确保 Monkey 字符串在用作哈希表键（例如在字典或环境变量中）
 * 时能够正确区分和匹配，从而保证解释器在运行期的正确性。
 *
 * <p>若测试输出错误信息（{@code System.err}），说明 {@link MonkeyString#HashKey()}
 * 的实现存在哈希冲突或一致性问题。
 *
 * @author axin
 * @version 1.0
 * @since 2025-10-16
 */
public class Object_Test {

    /**
     * 程序入口。
     * <p>执行字符串哈希键一致性测试。</p>
     *
     * @param args 命令行参数（在本测试中未使用）
     */
    public static void main(String[] args) {
        testStringHashKey();
    }

    /**
     * 测试 {@link MonkeyString} 的哈希键生成逻辑。
     * <p>具体测试内容如下：</p>
     * <ul>
     *     <li>验证两个内容相同的字符串生成的哈希键是否一致；</li>
     *     <li>验证两个不同内容的字符串生成的哈希键是否不同；</li>
     *     <li>若测试通过，将打印各个字符串对象的 {@code inspect()} 结果。</li>
     * </ul>
     *
     * <p>测试输出说明：</p>
     * <ul>
     *     <li>若打印错误信息（通过 {@code System.err.println}），说明哈希实现存在问题；</li>
     *     <li>若打印 "[Parse] ===>" 开头的输出，则说明所有断言均通过。</li>
     * </ul>
     */
    public static void testStringHashKey() {

        MonkeyString hello1 = new MonkeyString("Hello World");
        MonkeyString hello2 = new MonkeyString("Hello World");
        MonkeyString diff1 = new MonkeyString("My name is mazi");
        MonkeyString diff2 = new MonkeyString("My name is mazi");

        if (!hello1.HashKey().equals(hello2.HashKey())) {
            System.err.println("hello strings with same content have different hash keys");
            return;
        }

        if (!diff1.HashKey().equals(diff2.HashKey())) {
            System.err.println("strings with same content have different hash keys");
            return;
        }

        if (hello1.HashKey().equals(diff1.HashKey())) {
            System.err.println("strings with same content have same hash keys");
            return;
        }

        System.out.println("[Parse] ===> " + hello1.inspect() + ", " +
                hello2.inspect() + ", " + diff1.inspect() + ", "
                + diff2.inspect());
    }
}
