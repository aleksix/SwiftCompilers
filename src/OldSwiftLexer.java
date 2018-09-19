public class OldSwiftLexer extends Lexer {
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

    OldSwiftLexer(Source input) {
        super(input);
        currentSymbol = -1;
        lastPos = -1;
    }

    @Override
    Token getToken() throws LexingError {
        lastPos = input.getPosition();
        currentSymbol = input.peek();

        if (currentSymbol != -1) {
            // Whitespace/comment block
            while (currentSymbol == '/' || Character.isWhitespace(currentSymbol)) {
                if (currentSymbol == '/') {
                    currentSymbol = input.consume();
                    if (currentSymbol == '/') {
                        while (input.consume() != '\n') {
                        }
                        currentSymbol = input.consume();
                    } else if (currentSymbol == '*') {
                        int prevSymbol = -1;
                        while (prevSymbol != '*' || currentSymbol != '/') {
                            prevSymbol = currentSymbol;
                            currentSymbol = input.consume();
                        }
                        currentSymbol = input.consume();
                    } else
                        return new Token(Token.TokenType.DIVIDE, lastPos, Character.toString((char) currentSymbol));
                } else {
                    StringBuilder builder = new StringBuilder();
                    while (Character.isWhitespace(currentSymbol))
                        currentSymbol = input.consume();
                }
            }

            // Block with actual things in it
            lastPos = input.getPosition();

            if (Character.isAlphabetic(currentSymbol) || (char) currentSymbol == '_' || (char) currentSymbol == '`')
                return getIdentifier();

            if (Character.isDigit(currentSymbol)) {
                return checkNumber();
            }

            switch ((char) currentSymbol) {
                case '=':
                    return checkDblEqual();
                case '"':
                    return checkStringLiteral();
                case '#':
                    return checkHash();
                case '(':
                    input.consume();
                    return new Token(Token.TokenType.BRACKET_L, lastPos, Character.toString((char) currentSymbol));
                case ')':
                    input.consume();
                    return new Token(Token.TokenType.BRACKET_R, lastPos, Character.toString((char) currentSymbol));
                case ';':
                    input.consume();
                    return new Token(Token.TokenType.DELIMITER, lastPos, Character.toString((char) currentSymbol));
            }
        }

        return null;
    }

    private Token getIdentifier() throws LexingError {
        StringBuilder builder = new StringBuilder();
        Token.TokenType type = Token.TokenType.IDENTIFIER;

        // Swift allows the use of reserved keywords if they are surrounded by backticks ('`')
        boolean backtick = (currentSymbol == '`');
        if (backtick) {
            builder.append((char) currentSymbol);
            currentSymbol = input.consume();
        }

        while (Character.isAlphabetic(currentSymbol) || Character.isDigit(currentSymbol) || currentSymbol == '_') {
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
            if (SwiftSpecials.isKeyword(builder.toString()))
                type = Token.TokenType.KEYWORD;
        }

        return new Token(type, lastPos, builder.toString());
    }

    private Token checkDblEqual() {
        if (input.consume() == '=')
            return new Token(Token.TokenType.DBL_EQUAL, lastPos, "==");
        return new Token(Token.TokenType.EQUAL, lastPos, "=");
    }

    private Token checkStringLiteral() {
        StringBuilder builder = new StringBuilder();
        builder.append((char) currentSymbol);
        while (input.consume() != '"') {
            builder.append((char) input.peek());
        }
        builder.append((char) input.peek());
        input.consume();
        return new Token(Token.TokenType.STRING_LITERAL, lastPos, builder.toString());
    }

    // NOTE: I could go with simple and straightforward solution of checking the prefix and parsing accordingly, with
    // repetition of code or write a somewhat convoluted but elegant code with next to no repetitions.
    // I chose the latter.
    private Token checkNumber() throws LexingError {
        final String intSymbols = "0123456789";
        final String hexSymbols = "0123456789abcdefABCDEF";
        final String octSymbols = "01234567";
        final String binSymbols = "01";

        final String exponentPostfix = "+-";

        String curSymbols = intSymbols;
        String optionalPostfix = "";

        // We assume that we are working with integers at first. Check for float later.
        NumType type = NumType.INTEGER;
        Token.TokenType tokType = Token.TokenType.INT_LITERAL;

        int prevSymbol = -1;

        StringBuilder builder = new StringBuilder();
        builder.append((char) currentSymbol);
        prevSymbol = currentSymbol;
        input.consume();
        // Variable for mandatory symbols after special such as 0b... or e+...
        boolean checkSymbol = true;
        //Check the beginning prefix
        switch (input.peek()) {
            case 'x':
                type = NumType.HEX_INTEGER;
                curSymbols = hexSymbols;
                break;
            case 'o':
                type = NumType.OCTAL_INTEGER;
                curSymbols = octSymbols;
                break;
            case 'b':
                type = NumType.BINARY_INTEGER;
                curSymbols = binSymbols;
                break;
            default:
                checkSymbol = false;
                break;
        }

        if (prevSymbol != '0' && type != NumType.INTEGER)
            // TODO: ELABORATE
            throw new LexingError();

        // NOTE : Not sure if good for the future. Numbers probably won't change, but still
        // This one assumes that all prefixed numbers need checking. Might not be the case later.
        if (checkSymbol) {
            builder.append((char) input.peek());
            input.consume();
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
                input.consume();
            } else if (currentSymbol == 'e' || currentSymbol == 'E') {
                // Check if we are dealing with a float now
                // In this case - E is not a part of the symbols
                if (type == NumType.INTEGER)
                    type = NumType.FLOAT_EXPONENT;
                else if (type == NumType.FLOAT_DOT)
                    type = NumType.FLOAT_COMBO;
                else
                    throw new LexingError();
                builder.append((char) currentSymbol);
                input.consume();
                optionalPostfix = exponentPostfix;
                checkSymbol = true;
            } else if (currentSymbol == 'p' || currentSymbol == 'P') {
                // Check if we are dealing with a hex float now
                if (type == NumType.HEX_INTEGER)
                    type = NumType.HEX_FLOAT;
                else
                    throw new LexingError();
                builder.append((char) currentSymbol);
                input.consume();
                optionalPostfix = exponentPostfix;
                checkSymbol = true;
            } else if (currentSymbol == '.') {
                checkSymbol = true;
                // Check if we are dealing with a float now
                if (type == NumType.INTEGER)
                    type = NumType.FLOAT_DOT;
                else if (type == NumType.HEX_INTEGER)
                    type = NumType.HEX_FLOAT;
                else
                    throw new LexingError();
                builder.append((char) currentSymbol);
                input.consume();
            } else if (currentSymbol == '-' || currentSymbol == '+') {
                checkSymbol = true;
                // Check the stuff for the exponent
                if (type == NumType.FLOAT_EXPONENT || type == NumType.FLOAT_COMBO) {
                    if (prevSymbol != 'e' && prevSymbol != 'E')
                        throw new LexingError();
                } else if (type == NumType.HEX_FLOAT) {
                    if (prevSymbol != 'p' && prevSymbol != 'P')
                        throw new LexingError();
                } else
                    throw new LexingError();
                builder.append((char) currentSymbol);
                input.consume();
            } else
                done = true;
        }

        switch (type) {
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

    private Token checkHash() {
        return null;
    }
}
