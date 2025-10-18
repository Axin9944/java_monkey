package com.linewell.monkey.ast.test;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.imp.*;
import com.linewell.monkey.ast.modify.ModifierFunc;
import com.linewell.monkey.ast.modify.Modify;

import java.util.*;

/**
 * {@code Modify_Test} 是针对 {@link Modify#modify(Node, ModifierFunc)} 方法的单元测试类。
 * <p>
 * 它通过构造各种类型的 AST 节点（如 {@link InfixExpression}, {@link IfExpression}, {@link ArrayLiteral} 等），
 * 测试 Modify 方法是否能递归遍历并正确地对节点进行修改。
 * <p>
 * 测试的核心逻辑：
 * <ul>
 *     <li>定义一个修改函数 {@code turnOneIntoTwo}：将整数值为 1 的 {@link IntegerLiteral} 替换为值为 2 的节点。</li>
 *     <li>对不同类型的 AST 节点调用 {@code Modify.modify()}。</li>
 *     <li>验证修改结果是否与预期的 AST 结构一致。</li>
 * </ul>
 *
 * <p>原始逻辑参考自 Go 版本 Monkey 解释器的 `ast/modify_test.go`。
 *
 * @author axin
 */
public class Modify_Test {

    /**
     * 主入口函数，用于执行 {@link #testModify()}。
     */
    public static void main(String[] args) {
        testModify();
    }

    /**
     * 该方法测试 {@link Modify#modify(Node, ModifierFunc)} 的正确性。
     * <p>
     * 包括以下几类测试：
     * <ol>
     *     <li>基本表达式节点的修改（如 IntegerLiteral）。</li>
     *     <li>复合表达式（如 Infix、Prefix、If、Array、FunctionLiteral 等）。</li>
     *     <li>哈希结构（HashLiteral）的键值递归修改。</li>
     * </ol>
     */
    private static void testModify() {

        // 定义一个函数式接口，用于生成值为 1 的 IntegerLiteral
        TestModifyTestCase one = () -> {
            IntegerLiteral integerLiteral = new IntegerLiteral();
            integerLiteral.setValue(1);
            return integerLiteral;
        };

        // 定义一个函数式接口，用于生成值为 2 的 IntegerLiteral
        TestModifyTestCase two = () -> {
            IntegerLiteral integerLiteral = new IntegerLiteral();
            integerLiteral.setValue(2);
            return integerLiteral;
        };

        /**
         * 定义一个 ModifierFunc：将所有值为 1 的 IntegerLiteral 节点修改为值为 2。
         * 其他节点保持不变。
         */
        ModifierFunc turnOneIntoTwo = node -> {
            if (node instanceof IntegerLiteral) {
                IntegerLiteral integerLiteral = (IntegerLiteral) node;

                // 如果值不是 1，直接返回原节点
                if (integerLiteral.getValue() != 1) {
                    return node;
                }

                // 否则将值改为 2
                integerLiteral.setValue(2);
                return integerLiteral;
            }
            // 非 IntegerLiteral 节点不做修改
            return node;
        };

        /**
         * 构造多组测试用例，每个用例包含一个输入 AST（input）和预期结果（expected）
         */
        List<ModifyTestCase> testCases = Arrays.asList(
                // 直接修改单个 IntegerLiteral 节点
                new ModifyTestCase(one.get(), two.get()),

                // 修改 Program -> ExpressionStatement -> IntegerLiteral
                new ModifyTestCase(
                        new Program(Arrays.asList(new ExpressionStatement(one.get()))),
                        new Program(Arrays.asList(new ExpressionStatement(two.get())))
                ),

                // 修改中缀表达式（左侧 1 → 2）
                new ModifyTestCase(
                        new InfixExpression(one.get(), "+", two.get()),
                        new InfixExpression(two.get(), "+", two.get())
                ),

                // 修改中缀表达式（右侧 1 → 2）
                new ModifyTestCase(
                        new InfixExpression(two.get(), "+", one.get()),
                        new InfixExpression(two.get(), "+", two.get())
                ),

                // 修改前缀表达式（-1 → -2）
                new ModifyTestCase(
                        new PrefixExpression("-", one.get()),
                        new PrefixExpression("-", two.get())
                ),

                // 修改索引表达式（[1][1] → [2][2]）
                new ModifyTestCase(
                        new IndexExpression(one.get(), one.get()),
                        new IndexExpression(two.get(), two.get())
                ),

                // 修改 if 表达式的条件和分支体
                new ModifyTestCase(
                        new IfExpression(
                          one.get(), new BlockStatement(
                                  Arrays.asList(
                                          new ExpressionStatement(one.get())
                                  )
                        ), new BlockStatement(
                                Arrays.asList(
                                        new ExpressionStatement(one.get())
                                )
                        )
                        ), new IfExpression(
                                two.get(), new BlockStatement(
                                        Arrays.asList(
                                                new ExpressionStatement(
                                                        two.get()
                                                )
                                        )
                ), new BlockStatement(Arrays.asList(new ExpressionStatement(two.get())))
                )),

                // 修改 return 语句
                new ModifyTestCase(new ReturnStatement(one.get()),
                        new ReturnStatement(two.get())),

                // 修改 let 语句的值
                new ModifyTestCase(new LetStatement(one.get()),
                        new LetStatement(two.get())),

                // 修改函数体中的表达式
                new ModifyTestCase(
                        new FunctionLiteral(new ArrayList<>(),
                                new BlockStatement(
                                        Arrays.asList(
                                                new ExpressionStatement(one.get())
                                        )
                                )),
                        new FunctionLiteral(new ArrayList<>(),
                                new BlockStatement(
                                        Arrays.asList(
                                                new ExpressionStatement(two.get())
                                        )
                                ))),

                // 修改数组字面量
                new ModifyTestCase(
                        new ArrayLiteral(Arrays.asList(
                                one.get(),
                                one.get()
                        )),
                        new ArrayLiteral(Arrays.asList(
                                two.get(),
                                two.get()
                        )))
        );

        /**
         * 遍历所有测试用例，调用 Modify.modify 并检查结果是否与预期一致。
         */
        for (ModifyTestCase testCase : testCases) {
            Node modify = Modify.modify(testCase.input, turnOneIntoTwo);

            // 获取类名（检查节点类型是否一致）
            String modifyName = modify.getClass().getSimpleName();
            String expectedName = testCase.expected.getClass().getSimpleName();

            if (!modifyName.equals(expectedName)) {
                System.err.println("not equal. got=" + modifyName + ", want=" +
                        expectedName);
                return;
            }

            // 新增：比较两个节点是否内容完全一致（依赖 Node.equals() 实现）
            if (!modify.equals(testCase.expected)) {
                System.err.println("not equal");
            }
        }

        /**
         * 测试 HashLiteral 节点（哈希结构），确保键和值都能递归被修改。
         */
        Map<Expression, Expression> map = new HashMap<>();
        map.put(one.get(), one.get());
        map.put(two.get(), two.get());

        HashLiteral hashLiteral = new HashLiteral();
        hashLiteral.setPairs(map);

        // 调用 Modify.modify 对哈希节点进行修改
        Modify.modify(hashLiteral, turnOneIntoTwo);

        // 检查哈希键和值是否被修改为 2
        for (Map.Entry<Expression, Expression> entry : map.entrySet()) {
            Expression key = entry.getKey();
            if (!(key instanceof IntegerLiteral)) {
                System.err.println("key is not Integer. got=" +
                        key.getClass().getSimpleName());
                return;
            }

            IntegerLiteral integerKey = (IntegerLiteral) key;

            if (integerKey.getValue() != 2) {
                System.err.println("value is not " + 2 + ", got=" +
                        integerKey.getValue());
            }

            Expression value = entry.getValue();
            if (!(value instanceof IntegerLiteral)) {
                System.err.println("value is not Integer. got=" +
                        value.getClass().getSimpleName());
                return;
            }

            IntegerLiteral integerValue = (IntegerLiteral) value;
            if (integerValue.getValue() != 2) {
                System.err.println("value is not " + 2 + ", got=" +
                        integerValue.getValue());
            }
        }

        System.out.println("[Parse] ===> testModify");
    }
}

/**
 * 函数式接口：用于生成测试节点（例如生成 IntegerLiteral(1) 或 IntegerLiteral(2)）。
 */
@FunctionalInterface
interface TestModifyTestCase{

    Expression get();
}

/**
 * 测试用例类，包含一个输入节点（input）和一个期望节点（expected）。
 */
class ModifyTestCase{
    public Node input;
    public Node expected;

    public ModifyTestCase(Node input, Node expected) {
        this.input = input;
        this.expected = expected;
    }
}
