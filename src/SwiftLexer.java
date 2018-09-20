public class SwiftLexer extends Lexer {
    private int lastPos;
    private int currentSymbol;
    private int prevSymbol;

    SwiftLexer(Source input) {
        super(input);
        currentSymbol = -1;
        lastPos = -1;
        prevSymbol = -1;
    }

    @Override
    Token getToken() throws LexingError {
        lastPos = input.getPosition();
        prevSymbol = currentSymbol;
        currentSymbol = input.peek();

        while (SymbolClasses.isWhitespace(currentSymbol) || currentSymbol == '/') {
            if (currentSymbol == '/') {
                prevSymbol = currentSymbol;
                currentSymbol = input.consume();
                if (currentSymbol == '/') {
                    while (!SymbolClasses.isLinebreak(currentSymbol)) {
                        prevSymbol = currentSymbol;
                        currentSymbol = input.consume();
                    }
                    currentSymbol = input.consume();
                } else if (currentSymbol == '*') {
                    while (prevSymbol != '*' || currentSymbol != '/') {
                        prevSymbol = currentSymbol;
                        currentSymbol = input.consume();
                    }
                    currentSymbol = input.consume();
                } else {
                    return getOperatorLiteral();
                }
            } else {
                while (SymbolClasses.isWhitespace(currentSymbol)) {
                    prevSymbol = currentSymbol;
                    currentSymbol = input.consume();
                }
            }
        }

        lastPos = input.getPosition();
        prevSymbol = currentSymbol;
        currentSymbol = input.peek();

        if (SymbolClasses.isIdentifierHead(currentSymbol) || currentSymbol == '`' || currentSymbol == '$')
            return getIdentifier();

        switch (currentSymbol) {

        }

        return null;
    }

    Token getIdentifier() throws LexingError {
        StringBuilder builder = new StringBuilder();
        // Swift allows to use keywords as identifiers if they are surrounded by backticks ('`')
        boolean backtick = (currentSymbol == '`');
        Token.TokenType type = Token.TokenType.IDENTIFIER;

        if (backtick) {
            builder.append((char) currentSymbol);
            currentSymbol = input.consume();
        }
        while (SymbolClasses.isIdentifierSymbol(currentSymbol)) {
            builder.append((char) currentSymbol);
            currentSymbol = input.consume();
        }

        if (backtick) {
            if (currentSymbol == '`' && builder.length() > 1) {
                builder.append((char) currentSymbol);
                currentSymbol = input.consume();
            } else
                throw new LexingError();
        } else {
            String contents = builder.toString();
            if (SwiftSpecials.isKeyword(contents))
                type = Token.TokenType.KEYWORD;
            else if (SwiftSpecials.isContextSensitive(contents))
                type = Token.TokenType.CONTEXT_KEYWORD;
            else if (contents.equals("true") || contents.equals("false"))
                type = Token.TokenType.BOOLEAN_LITERAL;
            else if (contents.equals("nil"))
                type = Token.TokenType.NIL_LITERAL;
        }

        return new Token(type, lastPos, builder.toString());
    }

    Token getStringLiteral() {
        return null;
    }

    Token getNumberLiteral() {
        return null;
    }

    Token getExpressionLiteral() {
        return null;
    }

    Token getOperatorLiteral() {
        return null;
    }
}
