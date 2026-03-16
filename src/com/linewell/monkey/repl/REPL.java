package com.linewell.monkey.repl;

import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.imp.Program;
import com.linewell.monkey.evaluator.Evaluator;
import com.linewell.monkey.evaluator.MacroExpansion;
import com.linewell.monkey.lexer.Lexer;
import com.linewell.monkey.object.Environment;
import com.linewell.monkey.object.MonkeyObject;
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
 *     <li>调用 {@link Evaluator} 对 AST 求值</li>
 *     <li>将求值结果输出到指定的 Writer</li>
 *     <li>在遇到语法错误时打印 {@link #MONKEY_FACE} 和错误信息</li>
 * </ul>
 *
 * <p>示例：
 * <pre>
 * REPL.start(System.in, System.out); // 启动标准输入输出 REPL
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
     * <p>该方法会不断读取用户输入，将其交给词法分析器（Lexer）和语法分析器（Parser），
     * 生成抽象语法树（AST），再调用 {@link Evaluator#eval} 对 AST 求值。
     * <p>求值结果会立即输出；若存在语法错误，则输出错误提示和 {@link #MONKEY_FACE}。
     *
     * @param in  输入流，例如 {@code System.in}
     * @param out 输出流，例如 {@code System.out}
     */
    public static void start(Reader in, Writer out) {
        Scanner scanner = new Scanner(new BufferedReader(in));
        Environment env = new Environment();
        Environment macroEnv = new Environment();
        MacroExpansion evaluator = new MacroExpansion();

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

                evaluator.defineMacros(program, macroEnv);
                Node expanded = evaluator.expandMacros(program, macroEnv);

                MonkeyObject eval = Evaluator.eval(expanded, env);

                if (eval != null) {
                    System.out.println(eval.inspect());
                }


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
