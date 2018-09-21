import java.util.ArrayList;

public class SwiftLexer extends Lexer {
    // Needed as a sort of a small state machine
    private enum NumType {
        INTEGER,
        HEX_INTEGER,
        BINARY_INTEGER,
        OCTAL_INTEGER,
        FLOAT_DOT,
        FLOAT_EXPONENT,
        FLOAT_COMBO,
        HEX_FLOAT
    }

    private int lastPos;
    private int currentSymbol;
    private int prevSymbol;
    private ArrayList<Pair<Token, Token[]>> interpolations;

    SwiftLexer(Source input) {
        super(input);
        currentSymbol = -1;
        lastPos = -1;
        prevSymbol = -1;
        interpolations = new ArrayList<>();
    }

    // Utility functions
    private boolean escapable(int character) {
        return (character == '0' || character == '\\' || character == 't' || character == 'n' || character == 'r'
                || character == '"' || character == '\'');
    }

    public Token[] getInterpolatedExpression(Token interpolatedString) {
        for (Pair<Token, Token[]> expression : interpolations) {
            if (expression.first.equals(interpolatedString))
                return expression.second;
        }
        return null;
    }

    // More reliable than remembering to always do these 2 things
    private void advance() {
        prevSymbol = currentSymbol;
        currentSymbol = input.consume();
    }

    // Lexing functions
    @Override
    Token getToken() throws LexingError {
        lastPos = input.getPosition();
        prevSymbol = currentSymbol;
        currentSymbol = input.peek();

        while (SymbolClasses.isWhitespace(currentSymbol) || currentSymbol == '/') {
            if (currentSymbol == '/') {
                advance();
                if (currentSymbol == '/') {
                    while (!SymbolClasses.isLinebreak(currentSymbol)) {
                        advance();
                    }
                    advance();
                } else if (currentSymbol == '*') {
                    while (prevSymbol != '*' || currentSymbol != '/') {
                        advance();
                    }
                    advance();
                } else {
                    return getOperatorLiteral();
                }
            } else {
                while (SymbolClasses.isWhitespace(currentSymbol)) {
                    advance();
                }
            }
        }

        lastPos = input.getPosition();
        prevSymbol = currentSymbol;
        currentSymbol = input.peek();

        if (SymbolClasses.isIdentifierHead(currentSymbol) || currentSymbol == '`' || currentSymbol == '$')
            return getIdentifier();

        if (Character.isDigit(currentSymbol))
            return getNumberLiteral();

        switch (currentSymbol) {
            case '"':
                return getStringLiteral();
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
            advance();
        }
        while (SymbolClasses.isIdentifierSymbol(currentSymbol)) {
            builder.append((char) currentSymbol);
            advance();
        }

        if (backtick) {
            if (currentSymbol == '`' && builder.length() > 1) {
                builder.append((char) currentSymbol);
                advance();
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

    // Not sure if we should throw an exception or do something else in case of unescaped sequences.
    Token getStringLiteral() throws LexingError {
        // TODO: Find a way to deal with interpolated strings
        return null;
    }

    Token getNumberLiteral() throws LexingError {
        final String intSymbols = "0123456789";
        final String hexSymbols = "0123456789abcdefABCDEF";
        final String octSymbols = "01234567";
        final String binSymbols = "01";

        final String exponentPostfix = "+-";

        String curSymbols = intSymbols;
        String optionalPostfix = "";

        // We assume that we are working with integers at first. Check for float later.
        NumType numType = NumType.INTEGER;
        Token.TokenType tokType = Token.TokenType.INT_LITERAL;

        StringBuilder builder = new StringBuilder();
        builder.append((char) currentSymbol);
        advance();
        // Variable for mandatory symbols after special such as 0b... or e+...
        boolean checkSymbol = true;
        //Check the beginning prefix
        switch (currentSymbol) {
            case 'x':
                numType = NumType.HEX_INTEGER;
                curSymbols = hexSymbols;
                break;
            case 'o':
                numType = NumType.OCTAL_INTEGER;
                curSymbols = octSymbols;
                break;
            case 'b':
                numType = NumType.BINARY_INTEGER;
                curSymbols = binSymbols;
                break;
            default:
                checkSymbol = false;
                break;
        }

        if (prevSymbol != '0' && numType != NumType.INTEGER)
            // TODO: ELABORATE
            throw new LexingError();

        // NOTE : Not sure if good for the future. Numbers probably won't change, but still
        // This one assumes that all prefixed numbers need checking. Might not be the case later.
        if (checkSymbol) {
            builder.append((char) input.peek());
            advance();
        }

        boolean done = false;
        prevSymbol = -1;

        while (!done) {
            prevSymbol = currentSymbol;
            currentSymbol = input.peek();
            if (checkSymbol)
                if (optionalPostfix.indexOf(currentSymbol) == -1)
                    if (curSymbols.indexOf(currentSymbol) == -1)
                        // TODO : ELABORATE
                        throw new LexingError();
                    else
                        checkSymbol = false;
                else
                    optionalPostfix = "";

            if (curSymbols.indexOf(currentSymbol) != -1 || currentSymbol == '_') {
                // If everything is okay, just add the symbol
                builder.append((char) currentSymbol);
                advance();
            } else if (currentSymbol == 'e' || currentSymbol == 'E') {
                // Check if we are dealing with a float now
                // In this case - E is not a part of the symbols
                if (numType == NumType.INTEGER)
                    numType = NumType.FLOAT_EXPONENT;
                else if (numType == NumType.FLOAT_DOT)
                    numType = NumType.FLOAT_COMBO;
                else
                    throw new LexingError();
                builder.append((char) currentSymbol);
                advance();
                optionalPostfix = exponentPostfix;
                checkSymbol = true;
            } else if (currentSymbol == 'p' || currentSymbol == 'P') {
                // Check if we are dealing with a hex float now
                if (numType == NumType.HEX_INTEGER)
                    numType = NumType.HEX_FLOAT;
                else
                    throw new LexingError();
                builder.append((char) currentSymbol);
                advance();
                optionalPostfix = exponentPostfix;
                checkSymbol = true;
            } else if (currentSymbol == '.') {
                checkSymbol = true;
                // Check if we are dealing with a float now
                if (numType == NumType.INTEGER)
                    numType = NumType.FLOAT_DOT;
                else if (numType == NumType.HEX_INTEGER)
                    numType = NumType.HEX_FLOAT;
                else
                    throw new LexingError();
                builder.append((char) currentSymbol);
                advance();
            } else if (currentSymbol == '-' || currentSymbol == '+') {
                checkSymbol = true;
                // Check the stuff for the exponent
                if (numType == NumType.FLOAT_EXPONENT || numType == NumType.FLOAT_COMBO) {
                    if (prevSymbol != 'e' && prevSymbol != 'E')
                        throw new LexingError();
                } else if (numType == NumType.HEX_FLOAT) {
                    if (prevSymbol != 'p' && prevSymbol != 'P')
                        throw new LexingError();
                } else
                    throw new LexingError();
                builder.append((char) currentSymbol);
                advance();
            } else
                done = true;
        }

        switch (numType) {
            case INTEGER:
            case HEX_INTEGER:
            case OCTAL_INTEGER:
            case BINARY_INTEGER:
                tokType = Token.TokenType.INT_LITERAL;
                break;
            case FLOAT_DOT:
            case FLOAT_EXPONENT:
            case FLOAT_COMBO:
            case HEX_FLOAT:
                tokType = Token.TokenType.FLOAT_LITERAL;
                break;
            default:
                // More of a debug case, really.
                tokType = Token.TokenType.ERROR;
                break;
        }

        return new Token(tokType, lastPos, builder.toString());
    }

    Token getExpressionLiteral() {
        return null;
    }

    Token getOperatorLiteral() {
        StringBuilder builder = new StringBuilder();
        boolean dotStart = false;
        boolean prefix = false, postfix = false;

        prefix = prevSymbol != ' ' && prevSymbol != '\r' &&
                prevSymbol != '\n' && prevSymbol != '\t' &&
                prevSymbol != '(' && prevSymbol != '[' &&
                prevSymbol != '{' && prevSymbol != ',' &&
                prevSymbol != ';' && prevSymbol != ':' &&
                prevSymbol != '\0';


        if (SymbolClasses.isOperatorHead(currentSymbol)) {
            if (currentSymbol == '.') {
                dotStart = true;
            }
            builder.append((char) currentSymbol);
            currentSymbol = input.consume();
        }
        while (SymbolClasses.isOperatorSymbol(currentSymbol)) {
            if (currentSymbol == '!' || currentSymbol == '?') {
                break;
            } else if (currentSymbol == '.' && !dotStart) {
                break;
            } else if (builder.length() > 2) {
                if (currentSymbol == '/' || currentSymbol == '*' && prevSymbol == '/') break;
            } else {
                builder.append((char) currentSymbol);
                currentSymbol = input.consume();
            }
        }
        int nextSymbol = input.consume();
        postfix = nextSymbol != ' ' && nextSymbol != '\r' &&
                nextSymbol != '\n' && nextSymbol != '\t' &&
                nextSymbol != ')' && nextSymbol != ']' &&
                nextSymbol != '}' && nextSymbol != ',' &&
                nextSymbol != ';' && nextSymbol != ':' && (nextSymbol != '.' || !prefix);

        String operator = builder.toString();
        if (operator.length() == 1) {
            if (operator == "=") {
                if (prefix != postfix) {
                    return new Token(Token.TokenType.EQUAL, lastPos, operator);
                }
            }
            if (operator == "&") {
                if (prefix == postfix || prefix) {
                    return new Token(Token.TokenType.AMPERSAND, lastPos, operator);
                }
            }

        }

        return null;
    }
}
