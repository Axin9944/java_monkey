import com.linewell.monkey.repl.REPL;

/**
 * Monkey 语言解释器的启动入口类。
 *
 * <p>该类提供一个命令行程序的入口 {@code main} 方法，用于启动交互式解释器（REPL）。
 * 程序启动后会打印欢迎信息，并进入 REPL 循环，等待用户输入 Monkey 语言代码。
 *
 * <p>示例：
 * <pre>
 * $ java Main
 * Hello your-username This is the monkey programming language!
 * Feel free to type in commands
 * >>
 * </pre>
 */
public class Main {
    public static void main(String[] args) {
        String username = System.getProperty("user.name");
        System.out.println("Hello " + username + " This is the monkey programming language!");
        System.out.println("Feel free to type in commands");
        REPL.start();
    }
}