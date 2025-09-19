import com.linewell.monkey.repl.REPL;

public class Main {
    public static void main(String[] args) {
        String username = System.getProperty("user.name");
        System.out.println("Hello " + username + " This is the Monkey programming language!");
        System.out.println("Feel free to type in commands");
        REPL.start();
    }
}