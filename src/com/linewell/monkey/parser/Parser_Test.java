package com.linewell.monkey.parser;

import com.linewell.monkey.ast.Statement;
import com.linewell.monkey.ast.imp.Identifier;
import com.linewell.monkey.ast.imp.LetStatement;
import com.linewell.monkey.ast.imp.Program;
import com.linewell.monkey.lexer.Lexer;

import java.util.List;

public class Parser_Test {

    public static void main(String[] args) {
        System.out.println("Running Parser Tests...\n");
        testLetStatements();
    }

    /**
     * 测试 let 语句的解析（包括错误处理）
     */
    public static void testLetStatements() {
        // 输入：故意写错的 Monkey 代码（缺少 '='）
        String input = "\"" +
                "let x = 5;" +
                "let = 10;" +
                "let foobar 838383;" +
                "\"";

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);

        // 解析整个程序
        Program program = parser.parseProgram();

        // 检查解析器是否有错误
        checkParserErrors(parser);

        if (program == null) {
            System.err.println("FAILED: ParseProgram() returned null");
            return;
        }

        List<Statement> statements = program.getStatements();
        if (statements.size() != 3) {
            System.err.println("FAILED: program.Statements should have 3 statements, but got " + statements.size());
            return;
        } else {
            System.out.println("PASSED: program has 3 statements");
        }

        // 预期的变量名
        String[] expectedIdentifiers = {"x", "y", "foobar"};

        boolean allPassed = true;
        for (int i = 0; i < expectedIdentifiers.length; i++) {
            Statement stmt = statements.get(i);
            if (!testLetStatement(stmt, expectedIdentifiers[i])) {
                System.err.println("FAILED: testLetStatement failed for expected identifier: " + expectedIdentifiers[i]);
                allPassed = false;
            }
        }

        if (allPassed) {
            System.out.println("✅ ALL TESTS PASSED");
        } else {
            System.out.println("❌ SOME TESTS FAILED");
        }
    }

    /**
     * 验证一个语句是否为 let 语句，且变量名正确
     *
     * @param statement 要验证的语句
     * @param name      预期的变量名
     * @return 是否通过验证
     */
    public static boolean testLetStatement(Statement statement, String name) {
        if (!"let".equals(statement.getToken().getLiteral())) {
            System.err.println("s.TokenLiteral not 'let'. got=" + statement
                    .getToken().getLiteral());
            return false;
        }

        if (!(statement instanceof LetStatement)) {
            System.err.println("s not instanceof LetStatement. got=" + statement.getClass().getSimpleName());
            return false;
        }

        LetStatement letStmt = (LetStatement) statement;
        Identifier nameIdentifier = letStmt.getName();

        if (nameIdentifier == null) {
            System.err.println("letStmt.getName() is null");
            return false;
        }

        if (!name.equals(nameIdentifier.getValue())) {
            System.err.println("letStmt.Name.Value not '" + name + "'. got=" + nameIdentifier.getValue());
            return false;
        }

        if (!name.equals(nameIdentifier.getToken().getLiteral())) {
            System.err.println("letStmt.Name.TokenLiteral not '" + name
                    + "'. got=" + nameIdentifier.getToken().getLiteral());
            return false;
        }

        System.out.println("PASSED: let " + name + " parsed correctly");
        return true;
    }

    /**
     * 检查解析器是否有错误
     *
     * @param parser 解析器实例
     */
    public static void checkParserErrors(Parser parser) {
        List<String> errors = parser.getErrors();
        if (errors.isEmpty()) {
            System.out.println("No parser errors.");
            return;
        }

        System.out.println("Parser has " + errors.size() + " errors:");
        for (String msg : errors) {
            System.err.println("Parser error: " + msg);
        }
    }
}
