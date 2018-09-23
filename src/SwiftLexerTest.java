import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/*
 We can't perform unit-testing to a particular function directly.
 Thus, every test will call the lexer with an input that evokes only the tested
 */
class SwiftLexerTest {

    @org.junit.jupiter.api.Test
    void getToken() {
        // Check general execution
        Lexer lex = new SwiftLexer(new StringSource(""));
        assertNull(lex.getToken());

        // Check whitespace ignorance
        lex = new SwiftLexer(new StringSource("    "));
        assertNull(lex.getToken());

        // Check comment ignorance
        lex = new SwiftLexer(new StringSource("/* This is an ignored comment */"));
        assertNull(lex.getToken());
    }

    @org.junit.jupiter.api.Test
    void getIdentifier() {
        // Standard identifier
        Lexer lex = new SwiftLexer(new StringSource("hello"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.IDENTIFIER, 0, "hello"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        // Keyword
        lex = new SwiftLexer(new StringSource("var"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.KEYWORD, 0, "var"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        // Backticks
        lex = new SwiftLexer(new StringSource("`var`"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.IDENTIFIER, 0, "`var`"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        // Context-sensitive keyword - must be treated like a keyword only in certain contexts
        lex = new SwiftLexer(new StringSource("infix"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.CONTEXT_KEYWORD, 0, "infix"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        // Boolean literals are essentially special identifiers
        lex = new SwiftLexer(new StringSource("true"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.BOOLEAN_LITERAL, 0, "true"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        // Nil literal is also a special identifier
        lex = new SwiftLexer(new StringSource("nil"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.NIL_LITERAL, 0, "nil"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        // All identifiers are case-sensitive
        // Standard identifier
        lex = new SwiftLexer(new StringSource("Hello"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.IDENTIFIER, 0, "Hello"));
        assertNotEquals(lex.getToken(), new Token(Token.TokenType.IDENTIFIER, 0, "hello"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("Var"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.IDENTIFIER, 0, "Var"));
        assertNotEquals(lex.getToken(), new Token(Token.TokenType.KEYWORD, 0, "var"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("Infix"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.IDENTIFIER, 0, "Infix"));
        assertNotEquals(lex.getToken(), new Token(Token.TokenType.CONTEXT_KEYWORD, 0, "infix"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("True"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.IDENTIFIER, 0, "True"));
        assertNotEquals(lex.getToken(), new Token(Token.TokenType.BOOLEAN_LITERAL, 0, "true"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("Nil"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.IDENTIFIER, 0, "Nil"));
        assertNotEquals(lex.getToken(), new Token(Token.TokenType.NIL_LITERAL, 0, "nil"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        // Expected error - no backtick at the end
        lex = new SwiftLexer(new StringSource("`hello"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.IDENTIFIER, 0, "`hello`"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 1);
    }

    @org.junit.jupiter.api.Test
    void getStringLiteral() {
        // Single-line string
        Lexer lex = new SwiftLexer(new StringSource("\"hello\""));
        assertEquals(lex.getToken(), new Token(Token.TokenType.STRING_LITERAL, 0, "\"hello\""));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        // Multiline string
        lex = new SwiftLexer(new StringSource("\"\"\"\nhello\n\"\"\""));
        assertEquals(lex.getToken(), new Token(Token.TokenType.STRING_LITERAL, 0, "\"\"\"hello\"\"\""));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        // Interpolated string
        lex = new SwiftLexer(new StringSource("\"hello\\(3+2)\""));
        Token token = lex.getToken();
        ArrayList<Token[]> interpolation;
        assertEquals(token, new Token(Token.TokenType.INTERPOLATED_STRING, 0, "\"hello\\(3+2)\""));
        interpolation = ((SwiftLexer) lex).getInterpolatedExpressions(token);
        assertEquals(interpolation.size(), 1);
        assertEquals(interpolation.get(0)[0], new Token(Token.TokenType.INT_LITERAL, 7, "3"));
        assertEquals(interpolation.get(0)[1], new Token(Token.TokenType.BINARY_OPERATOR, 8, "+"));
        assertEquals(interpolation.get(0)[2], new Token(Token.TokenType.INT_LITERAL, 9, "2"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        // Expected error - string not ended at EOF
        lex = new SwiftLexer(new StringSource("\"hello"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.STRING_LITERAL, 0, "\"hello\""));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 1);

        // Expected error - newlines forbidden is single-line strings
        lex = new SwiftLexer(new StringSource("\"hello\n\""));
        assertEquals(lex.getToken(), new Token(Token.TokenType.STRING_LITERAL, 0, "\"hello\""));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 1);

        // Expected error - string not ended at EOF
        lex = new SwiftLexer(new StringSource("\"\"\"\nhello\n"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.STRING_LITERAL, 0, "\"\"\"hello\"\"\""));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 1);

        // Expected error - first delimiter must be separated by newline
        lex = new SwiftLexer(new StringSource("\"\"\"hello\n\"\"\""));
        assertEquals(lex.getToken(), new Token(Token.TokenType.STRING_LITERAL, 0, "\"\"\"hello\"\"\""));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 1);

        // Expected error - last delimiter must be separated by newline
        lex = new SwiftLexer(new StringSource("\"\"\"\nhello\"\"\""));
        assertEquals(lex.getToken(), new Token(Token.TokenType.STRING_LITERAL, 0, "\"\"\"hello\"\"\""));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 1);

        // Expected error - first delimiter must be separated by newline
        lex = new SwiftLexer(new StringSource("\"\"\"\nhello\"\"\""));
        assertEquals(lex.getToken(), new Token(Token.TokenType.STRING_LITERAL, 0, "\"\"\"hello\"\"\""));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 1);

        // Expected error - { expected after \\u
        lex = new SwiftLexer(new StringSource("\"hello\\u0021}\""));
        assertEquals(lex.getToken(), new Token(Token.TokenType.STRING_LITERAL, 0, "\"hello!\""));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 1);

        // Expected error - } expected after \\u sequence
        // Expected error - incorrect Unicode sequence
        lex = new SwiftLexer(new StringSource("\"hello\\u{0021\""));
        assertEquals(lex.getToken(), new Token(Token.TokenType.STRING_LITERAL, 0, "\"hello\""));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 2);

        // Expected error - unknown escape sequence
        lex = new SwiftLexer(new StringSource("\"hello\\x\""));
        assertEquals(lex.getToken(), new Token(Token.TokenType.STRING_LITERAL, 0, "\"hello\""));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 1);
    }

    @org.junit.jupiter.api.Test
    void getNumberLiteral() {
    }

    @org.junit.jupiter.api.Test
    void getExpressionLiteral() {
    }

    @org.junit.jupiter.api.Test
    void getOperatorLiteral() {

        Lexer lex = new SwiftLexer(new StringSource("a=b"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.EQUAL, 0, "="));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("&myVar"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.PREFIX_AMPERSAND, 0, "&"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("f1.f2"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.DOT, 2, "."));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource(".f"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.PREFIX_DOT, 0, "."));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("?"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.QUESTION_MARK, 0, "?"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("U?"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.POSTFIX_QUESTION, 1, "?"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("a +++ b"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.BINARY_OPERATOR, 2, "+++"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("a+++.b"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.POSTFIX_OPERATOR, 1, "+++."));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("a+++ b"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.POSTFIX_OPERATOR, 1, "+++"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("a +++b"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.PREFIX_OPERATOR, 2, "+++"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("@Application"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.AT, 2, "@"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("do { }"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.CURLY_L, 3, "{"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.CURLY_R, 5, "}"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("("));
        assertEquals(lex.getToken(), new Token(Token.TokenType.BRACKET_L, 0, "("));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource(")"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.BRACKET_R, 0, ")"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("["));
        assertEquals(lex.getToken(), new Token(Token.TokenType.SQUARE_L, 0, "["));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("]"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.SQUARE_R, 0, "]"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("&"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.AMPERSAND, 0, "&"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource(","));
        assertEquals(lex.getToken(), new Token(Token.TokenType.COMMA, 0, ","));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource(";"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.SEMICOLON, 0, ";"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource(":"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.COLON, 0, ":"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("lock->cmd"));
        assertEquals(lex.getToken(), new Token(Token.TokenType.ARROW, 4, "->"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 0);

        lex = new SwiftLexer(new StringSource("*/"));
        assertEquals(((SwiftLexer) lex).getErrors().size(), 1);
    }
}
