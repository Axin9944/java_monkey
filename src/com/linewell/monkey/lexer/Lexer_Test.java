package com.linewell.monkey.lexer;

import com.linewell.monkey.token.Token;
import com.linewell.monkey.token.TokenType;

public class Lexer_Test {
    public static void main(String[] args) {
        System.out.println("Running Lexer TestCase...\n");
        TestNextToken();
    }

    public static void TestNextToken() {
        String input = "let five = 5;\n" +
                "let ten = 10;\n" +
                "let add = fn(x, y){\n" +
                "   x + y;\n" +
                "};\n" +
                "let result = add(five, ten);\n" +
                "!-/*5;\n" +
                "5 < 10 > 5;\n" +
                "if (5 < 10){\n" +
                "   return true;\n" +
                "} else {\n" +
                "   return false;\n" +
                "}\n" +
                "10 == 10;\n" +
                "10 != 9;\n" +
                "\"foobar\"\n" +
                "\"foo bar\"\n" +
                "[1, 2];\n" +
                "{\"foo\": \"bar\"}\n";

        TestCase[] tests = {new TestCase(TokenType.LET, "let"),
                new TestCase(TokenType.IDENT, "five"),
                new TestCase(TokenType.ASSIGN, "="),
                new TestCase(TokenType.INT, "5"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.LET, "let"),
                new TestCase(TokenType.IDENT, "ten"),
                new TestCase(TokenType.ASSIGN, "="),
                new TestCase(TokenType.INT, "10"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.LET, "let"),
                new TestCase(TokenType.IDENT, "add"),
                new TestCase(TokenType.ASSIGN, "="),
                new TestCase(TokenType.FUNCTION, "fn"),
                new TestCase(TokenType.LPAREN, "("),
                new TestCase(TokenType.IDENT, "x"),
                new TestCase(TokenType.COMMA, ","),
                new TestCase(TokenType.IDENT, "y"),
                new TestCase(TokenType.RPAREN, ")"),
                new TestCase(TokenType.LBRACE, "{"),
                new TestCase(TokenType.IDENT, "x"),
                new TestCase(TokenType.PLUS, "+"),
                new TestCase(TokenType.IDENT, "y"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.RBRACE, "}"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.LET, "let"),
                new TestCase(TokenType.IDENT, "result"),
                new TestCase(TokenType.ASSIGN, "="),
                new TestCase(TokenType.IDENT, "add"),
                new TestCase(TokenType.LPAREN, "("),
                new TestCase(TokenType.IDENT, "five"),
                new TestCase(TokenType.COMMA, ","),
                new TestCase(TokenType.IDENT, "ten"),
                new TestCase(TokenType.RPAREN, ")"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.BANG, "!"),
                new TestCase(TokenType.MINUS, "-"),
                new TestCase(TokenType.SLASH, "/"),
                new TestCase(TokenType.MULT, "*"),
                new TestCase(TokenType.INT, "5"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.INT, "5"),
                new TestCase(TokenType.LT, "<"),
                new TestCase(TokenType.INT, "10"),
                new TestCase(TokenType.GT, ">"),
                new TestCase(TokenType.INT, "5"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.IF, "if"),
                new TestCase(TokenType.LPAREN, "("),
                new TestCase(TokenType.INT, "5"),
                new TestCase(TokenType.LT, "<"),
                new TestCase(TokenType.INT, "10"),
                new TestCase(TokenType.RPAREN, ")"),
                new TestCase(TokenType.LBRACE, "{"),
                new TestCase(TokenType.RETURN, "return"),
                new TestCase(TokenType.TRUE, "true"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.RBRACE, "}"),
                new TestCase(TokenType.ELSE, "else"),
                new TestCase(TokenType.LBRACE, "{"),
                new TestCase(TokenType.RETURN, "return"),
                new TestCase(TokenType.FALSE, "false"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.RBRACE, "}"),
                new TestCase(TokenType.INT, "10"),
                new TestCase(TokenType.EQ, "=="),
                new TestCase(TokenType.INT, "10"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.INT, "10"),
                new TestCase(TokenType.NOT_EQ, "!="),
                new TestCase(TokenType.INT, "9"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.STRING, "foobar"),
                new TestCase(TokenType.STRING, "foo bar"),
                new TestCase(TokenType.LBRACKET, "["),
                new TestCase(TokenType.INT, "1"),
                new TestCase(TokenType.COMMA, ","),
                new TestCase(TokenType.INT, "2"),
                new TestCase(TokenType.RBRACKET, "]"),
                new TestCase(TokenType.SEMICOLON, ";"),
                new TestCase(TokenType.LBRACE, "{"),
                new TestCase(TokenType.STRING, "foo"),
                new TestCase(TokenType.COLON, ":"),
                new TestCase(TokenType.STRING, "bar"),
                new TestCase(TokenType.RBRACE, "}"),
                new TestCase(TokenType.EOF, ""),};

        Lexer lexer = new Lexer(input);

        for (int i = 0; i < tests.length; i++) {
            Token token = lexer.nextToken();

            if (token.getType() != tests[i].expectedTokenType) {
                System.err.println("tests[" + i + "] - " +
                        "tokentype wrong. expected=" + tests[i].expectedTokenType +
                        ", got= " + token.getType());
            }

            if (!token.getLiteral().equals(tests[i].expectedLiteral)) {
                System.err.println("tests[" + i + "] - " +
                        "literal wrong. expected=" + tests[i].expectedLiteral +
                        ", got= " + token.getLiteral());
            }

        }

        System.out.println("[Parse] ====> " + input);
    }

    private static class TestCase {
        public TokenType expectedTokenType;
        public String expectedLiteral;

        public TestCase(TokenType expectedTokenType, String expectedLiteral) {
            this.expectedTokenType = expectedTokenType;
            this.expectedLiteral = expectedLiteral;
        }
    }
}


