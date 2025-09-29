package com.linewell.monkey.repl;

import com.linewell.monkey.ast.imp.Program;
import com.linewell.monkey.lexer.Lexer;
import com.linewell.monkey.parser.Parser;

import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * 一个简单的交互式解释器（REPL, Read-Eval-Print Loop）实现。
 *
 * <p>该类提供：
 * <ul>
 *     <li>启动 REPL，读取用户输入</li>
 *     <li>使用 Lexer 和 Parser 解析输入的 Monkey 语言代码</li>
 *     <li>打印解析结果或语法错误信息</li>
 * </ul>
 *
 * <p>示例：
 * <pre>
 * REPL.start(); // 启动标准输入输出 REPL
 * </pre>
 */
public class REPL {

    // REPL 提示符，用于提示用户输入
    public static final String PROMPT = ">>";

    // ASCII 风格的“猴子脸”，用于打印解析错误时显示
    public static final String MONKEY_FACE = "            __,__\n" +
            "   .--.  .-\"     \"-.  .--.\n" +
            "  / .. \\/  .-. .-.  \\/ .. \\\n" +
            " | |  '|  /   Y   \\  |'  | |\n" +
            " | \\   \\  \\ 0 | 0 /  /   / |\n" +
            "  \\ '- ,\\.-\"\"\"\"\"\"\"-./, -' /\n" +
            "   ''-' /_   ^ ^   _\\ '-''\n" +
            "       |  \\._   _./  |\n" +
            "       \\   \\ '~' /   /\n" +
            "        '._ '-=-' _.'\n" +
            "           '-----'";

    /**
     * 启动交互式解释器（REPL）。
     *
     * <p>读取 {@code in} 输入流，解析输入并输出到 {@code out}。
     * 支持连续输入多行代码，遇到语法错误会打印 MONKEY_FACE 和错误信息。
     *
     * @param in  输入流，例如 {@code System.in}
     * @param out 输出流，例如 {@code System.out}
     */
    public static void start(Reader in, Writer out) {
        Scanner scanner = new Scanner(new BufferedReader(in));

        try {
            while (true) {
                // 输出提示符
                out.write(PROMPT);
                out.flush();

                // 读取一行
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine();

                // 创建词法分析器
                Lexer lexer = new Lexer(line);

                // 创建语法分析器
                Parser parser = new Parser(lexer);

                Program program = parser.parseProgram();

                if (!parser.getErrors().isEmpty()) {
                    printParserErrors(parser.getErrors());
                    continue;
                }

                System.out.println(program.toString());

                /*// 分词并输出
                Token tok;
                while ((tok = lexer.nextToken()).getType() != TokenType.EOF) {
                    out.write(tok.toString() + "\n");
                    out.flush();
                }*/
            }
        } catch (IOException e) {
            // 处理 IO 错误（如管道关闭）
            try {
                out.write("ERROR: " + e.getMessage() + "\n");
                out.flush();
            } catch (IOException ignored) {
            }
        } finally {
            try {
                out.flush();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 便捷入口：使用标准输入输出启动 REPL。
     *
     * <p>等价于调用 {@code start(new InputStreamReader(System.in), new OutputStreamWriter(System.out))}。
     */
    public static void start() {
        start(new InputStreamReader(System.in), new OutputStreamWriter(System.out));
    }

    /**
     * 打印解析器在语法分析过程中产生的错误信息。
     *
     * <p>输出格式包含一个预定义的 {@code MONKEY_FACE} 表情，
     * 以及固定提示信息 {@code "Woops! We ran into some monkey business here!"}，
     * 然后逐行输出错误详情。
     *
     * <p>典型输出示例：
     * <pre>
     *  (这里是 MONKEY_FACE 表情)
     *  Woops! We ran into some monkey business here!
     *   parser errors:
     *      expected next token to be =, got + instead
     *      no prefix parse function for ) found
     * </pre>
     *
     * @param errors 解析器产生的错误消息列表，每个元素是一条错误描述
     */
    public static void printParserErrors(List<String> errors) {
        System.out.println(MONKEY_FACE);
        System.out.println("Woops! We ran into some monkey business here!");
        System.out.println(" parser errors:");
        for (String msg : errors) {
            System.out.println("\t" + msg);
        }
    }
}
