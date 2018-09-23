import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/*
 We can't perform unit-testing to a particular function directly.
 Thus, every test will call the lexer with an input that evokes only the tested
 */
class SwiftLexerTest {

    @org.junit.jupiter.api.Test
    void getInterpolatedExpressions() {
    }

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
    }

    @org.junit.jupiter.api.Test
    void getNumberLiteral() {
    }

    @org.junit.jupiter.api.Test
    void getExpressionLiteral() {
    }

    @org.junit.jupiter.api.Test
    void getOperatorLiteral() {
    }
}