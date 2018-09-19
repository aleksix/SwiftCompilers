public class Token {
    public enum TokenType {
        KEYWORD,
        IDENTIFIER,
        CONTEXT_KEYWORD,            // Context-dependent keyword

        STRING_LITERAL,
        INT_LITERAL,
        FLOAT_LITERAL,
        EXPRESSION_LITERAL,
        BOOLEAN_LITERAL,
        NIL_LITERAL,

        BINARY_OPERATOR,
        POSTFIX_OPERATOR,
        PREFIX_OPERATOR,
        BRACKET_L,
        BRACKET_R,
        CURLY_L,
        CURLY_R,
        SQUARE_L,
        SQUARE_R,
        ARROW_R,
        ARROW_L,

        DOT,
        COMMA,
        COLON,
        SEMICOLON,
        EQUAL,
        AT,
        HASH,
        AMPERSAND,
        ARROW,
        BACKTICK,
        QUESTION_MARK,
        EXCLAMATION_MARK,

        ERROR
    }

    public String value;
    public long position;
    public TokenType type;

    public Token(TokenType type, long position) {
        value = null;
        this.position = position;
        this.type = type;
    }

    public Token(TokenType type, long position, String value) {
        this.value = value;
        this.position = position;
        this.type = type;
    }
}
