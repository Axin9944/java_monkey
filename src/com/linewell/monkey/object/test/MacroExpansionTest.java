package com.linewell.monkey.object.test;

import com.linewell.monkey.ast.Node;
import com.linewell.monkey.ast.imp.Identifier;
import com.linewell.monkey.ast.imp.Program;
import com.linewell.monkey.evaluator.MacroExpansion;
import com.linewell.monkey.lexer.Lexer;
import com.linewell.monkey.object.Environment;
import com.linewell.monkey.object.MonkeyObject;
import com.linewell.monkey.object.imp.MonkeyMacro;
import com.linewell.monkey.parser.Parser;

import java.util.*;

public class MacroExpansionTest {

    public static void main(String[] args) {
         // testDefineMacros();
         testExpandMacros();
    }

    public static void testDefineMacros() {
        String input = "let number = 1;\n" +
                "       let function = fn(x, y) { x + y;};\n" +
                "       let mymacro = macro(x, y) { x + y;};";

        Environment env = new Environment();
        Program program = testParseProgram(input);

        MacroExpansion macroExpansion = new MacroExpansion();
        macroExpansion.defineMacros(program, env);

        if (program.getStatements().size() != 2) {
            System.err.println("Wrong number of statements.got=" + program.getStatements().size());
            return;
        }

        MonkeyObject numberObj = env.get("number");
        if (numberObj != null) {
            System.err.println("number should not be defined");
            return;
        }

        MonkeyObject functionObj = env.get("function");
        if (functionObj != null) {
            System.err.println("function should not be defined");
            return;
        }

        MonkeyObject mymacroObj = env.get("mymacro");
        if (mymacroObj == null) {
            System.err.println("mymacro should not in environment");
            return;
        }

        if (!(mymacroObj instanceof MonkeyMacro)) {
            System.err.println("object is not Macro. got=" + mymacroObj.getClass().getSimpleName());
            return;
        }

        MonkeyMacro macro = (MonkeyMacro) mymacroObj;

        if (macro.getParameters().size() != 2) {
            System.err.println("Wrong number of macro parameters.got=" +  macro.getParameters().size());
        }

        List<Identifier> parameters = macro.getParameters();
        if (!"x".equals(parameters.get(0).toString())) {
            System.err.println("parameter is not 'x'. got=" + parameters.get(0).toString());
            return;
        }

        if (!"y".equals(parameters.get(1).toString())) {
            System.err.println("parameter is not 'y'. got=" + parameters.get(1).toString());
            return;
        }

        String expectedBody = "(x + y)";
        if (!expectedBody.equals(macro.getBody().toString())) {
            System.err.println("body is not " + expectedBody + ". got=" +
                    macro.getBody().toString());
        }

        System.out.println("[Parse] =====>" + parameters.get(0).toString() + " + " +
                parameters.get(1).toString() + " body: " +  macro.getBody().toString());

    }

    public static void testExpandMacros() {
        List<Map<String, String>> tests = new ArrayList<Map<String, String>>(){{
            add(new HashMap<String, String>() {{
                put("let infixExpression = macro() { quote(1 + 2); };\n" +
                        "infixExpression();", "(1 + 2)");
            }});
            add(new HashMap<String, String>() {{
                put("let reverse = macro(a, b) { quote(unquote(b) - unquote(a)); };\n" +
                        "reverse(2 + 2, 10 - 5)", "(10 - 5) - (2 + 2)");
            }});
            add(new  HashMap<String, String>() {{
                put("let unless = macro(condition, consequence, alternative) {\n" +
                        "   quote(if (!(unquote(condition))){\n" +
                        "       unquote(consequence);\n" +
                        "   } else {\n" +
                        "       unquote(alternative));\n" +
                        "})\n" +
                        "}\n" +
                        "unless(10 > 5, puts(\"not greater\"), puts(\"greater\"));",
                        "if (!(10 > 5)) { puts(\"not greater\") } else { puts(\"greater\") }");
            }});
        }};

        for (Map<String, String> test : tests) {
            Set<String> strings = test.keySet();
            Iterator<String> iterator = strings.iterator();
            if (!iterator.hasNext()) {
                return;
            }
            String epted = iterator.next();
            Program expected = testParseProgram(test.get(epted));
            Program program = testParseProgram(epted);

            Environment env = new Environment();
            MacroExpansion macroExpansion = new MacroExpansion();
            macroExpansion.defineMacros(program, env);
            Node expanded = macroExpansion.expandMacros(program, env);

            if (!expanded.toString().equals(expected.toString())) {
                System.err.println("not equal. want=" + expected.toString()
                        + "got=" + expanded.toString());
                return;
            }

            System.out.println("[Parse] ====> " );
        }
    }

    private static Program testParseProgram(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        return parser.parseProgram();
    }
}
