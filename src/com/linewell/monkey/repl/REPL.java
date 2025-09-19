package com.linewell.monkey.repl;

import com.linewell.monkey.lexer.Lexer;
import com.linewell.monkey.token.Token;
import com.linewell.monkey.token.TokenType;

import java.io.*;
import java.util.Scanner;

public class REPL {

    public static final String PROMPT = ">>";

    /***
     * 启动交互式解释器
     * @param in    输入流 （System.in）
     * @param out   输出流 （System.out）
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

                // 分词并输出
                Token tok;
                while ((tok = lexer.nextToken()).getType() != TokenType.EOF) {
                    out.write(tok.toString() + "\n");
                    out.flush();
                }
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

    /***
     * (便捷入口：直接使用 System.in / System.out)
     * 使用标准输入输出启动 REPL
     */
    public static void start() {
        start(new InputStreamReader(System.in), new OutputStreamWriter(System.out));
    }
}
