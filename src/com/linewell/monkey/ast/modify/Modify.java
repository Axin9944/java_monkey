package com.linewell.monkey.ast.modify;

import com.linewell.monkey.ast.Expression;
import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code Modify} 类实现了 Monkey 语言 AST（抽象语法树）的通用修改器。
 * <p>
 * 它提供一个静态方法 {@link #modify(Node, ModifierFunc)}，可以对任意 AST 节点及其子节点递归地应用一个 {@link ModifierFunc}。
 * 这类似于 Go 版本 Monkey 解释器中的“树遍历 + 节点替换”逻辑。
 * <p>
 * 典型用途包括：
 * <ul>
 *     <li>对语法树中的某些节点进行批量替换或重写（如宏展开、常量折叠）。</li>
 *     <li>对 AST 中的所有表达式节点执行某种统一操作。</li>
 * </ul>
 */
public class Modify {

    /**
     * 递归遍历并修改 AST 节点。
     *
     * <p>该方法会递归访问 {@code node} 的所有子节点（如表达式、语句块、函数体等），
     * 并在遍历完每个子节点后，将 {@link ModifierFunc} 应用于当前节点。
     * <p>
     * 类似于 Go 中的：
     * <pre>
     * func Modify(node Node, modifier ModifierFunc) Node
     * </pre>
     *
     * @param node     当前要处理的 AST 节点
     * @param modifier 对节点执行修改的函数式接口
     * @return 修改后的节点（或原节点）
     */
    public static Node modify(Node node, ModifierFunc modifier) {

        // 如果是 Program 节点，遍历其中的每个语句并递归修改
        if (node instanceof Program) {
            Program program = (Program) node;
            List<Statement> statements = program.getStatements();
            for (int i = 0; i < statements.size(); i++) {
                statements.set(i, (Statement) modify(statements.get(i), modifier));
            }
        }

        // 如果是表达式语句（ExpressionStatement），递归修改其表达式部分
        else if (node instanceof ExpressionStatement) {
            ExpressionStatement expStmt = (ExpressionStatement) node;
            expStmt.setExpression((Expression) modify(expStmt.getExpression(), modifier));
        }

        // 如果是中缀表达式（如 a + b），递归修改左右两侧的表达式
        else if (node instanceof InfixExpression) {
            InfixExpression inExp = (InfixExpression) node;
            inExp.setLeft((Expression) modify(inExp.getLeft(), modifier));
            inExp.setRight((Expression) modify(inExp.getRight(), modifier));
        }

        // 如果是前缀表达式（如 -a 或 !b），递归修改右侧的表达式
        else if (node instanceof PrefixExpression) {
            PrefixExpression pExp = (PrefixExpression) node;
            pExp.setRight((Expression) modify(pExp.getRight(), modifier));
        }

        // 如果是索引表达式（如 arr[0]），递归修改被索引对象与索引本身
        else if (node instanceof IndexExpression) {
            IndexExpression indexExp = (IndexExpression) node;
            indexExp.setLeft((Expression) modify(indexExp.getLeft(), modifier));
            indexExp.setIndex((Expression) modify(indexExp.getIndex(), modifier));
        }

        // 如果是 if 表达式，递归修改条件、then 块和 else 块
        else if (node instanceof IfExpression) {
            IfExpression ifExpression = (IfExpression) node;
            ifExpression.setCondition((Expression) modify(ifExpression.getCondition(), modifier));
            ifExpression.setConsequence((BlockStatement)  modify(ifExpression.getConsequence(), modifier));
            if (ifExpression.getAlternative() != null) {
                ifExpression.setAlternative((BlockStatement) modify(ifExpression.getAlternative(), modifier));
            }
        }

        // 如果是语句块（BlockStatement），递归修改其中的每条语句
        else if (node instanceof BlockStatement) {
            BlockStatement blockStmt = (BlockStatement) node;
            List<Statement> statements = blockStmt.getStatements();
            for (int i = 0; i < statements.size(); i++) {
                statements.set(i, (Statement) modify(statements.get(i), modifier));
            }
        }

        // 如果是 return 语句，递归修改其返回值表达式
        else if (node instanceof ReturnStatement) {
            ReturnStatement returnStmt = (ReturnStatement) node;
            returnStmt.setReturnValue((Expression) modify(returnStmt.getReturnValue(), modifier));
        }

        // 如果是 let 语句（变量定义），递归修改其右值表达式
        else if (node instanceof LetStatement) {
            LetStatement letStmt = (LetStatement) node;
            letStmt.setValue((Expression)  modify(letStmt.getValue(), modifier));
        }

        // 如果是函数定义，递归修改参数列表与函数体
        else if (node instanceof FunctionLiteral) {
            FunctionLiteral functionLiteral = (FunctionLiteral) node;
            List<Identifier> parameters = functionLiteral.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                parameters.set(i, (Identifier) modify(parameters.get(i), modifier));
            }
            functionLiteral.setBody((BlockStatement) modify(functionLiteral.getBody(), modifier));
        }

        // 如果是数组字面量（[a, b, c]），递归修改其中的每个元素
        else if (node instanceof ArrayLiteral) {
            ArrayLiteral arrayLiteral = (ArrayLiteral) node;
            List<Expression> elements = arrayLiteral.getElements();
            for (int i = 0; i < elements.size(); i++) {
                elements.set(i, (Expression) modify(elements.get(i), modifier));
            }
        }

        // 如果是哈希字面量（{key: value}），递归修改所有键和值
        else if (node instanceof HashLiteral) {
            HashLiteral hashLiteral = (HashLiteral) node;
            Map<Expression, Expression> newPairs = new HashMap<>();
            Map<Expression, Expression> expression = hashLiteral.getPairs();
            for (Map.Entry<Expression, Expression> entry : expression.entrySet()) {
                Expression newKey = (Expression) modify(entry.getKey(), modifier);
                Expression newVal = (Expression)  modify(entry.getValue(), modifier);
                newPairs.put(newKey, newVal);
            }
            // 将修改后的键值对重新设置回 HashLiteral
            hashLiteral.setPairs(newPairs);
        }

        // 最后，对当前节点本身应用 modifier 函数并返回结果
        return modifier.modifierFunc(node);
    }

}
