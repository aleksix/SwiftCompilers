import java.util.ArrayList;

/*
  This code MUST be documented, covered by unit-tests and refactored, in this particular order.
  Right now, everything mostly works, but it's horrible and dirty under the hood, which is unacceptable
 */
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
    private ArrayList<String> errors;

    SwiftLexer(Source input) {
        super(input);
        currentSymbol = -1;
        lastPos = -1;
        prevSymbol = -1;
        interpolations = new ArrayList<>();
        errors = new ArrayList<>();
    }

    /*
     * Utility functions
     */
    private boolean singleEscapable(int character) {
        return (multilineEscapable(character) || character == '"');
    }

    private boolean multilineEscapable(int character) {
        return (character == '0' || character == '\\' || character == 't' || character == 'n' || character == 'r'
                || character == '\'');
    }

    public ArrayList<Token[]> getInterpolatedExpression(Token interpolatedString) {
        ArrayList<Token[]> out = new ArrayList<>();
        for (Pair<Token, Token[]> expression : interpolations) {
            if (expression.first.equals(interpolatedString))
                out.add(expression.second);
        }
        return out;
    }

    // More reliable than remembering to always do these 2 things
    private void advance() {
        prevSymbol = currentSymbol;
        currentSymbol = input.consume();
    }

    private void parseInterpolation(Token curToken) {
        // Bracket counter, to know when to exit, currently just 1 bracket
        int brackets = 1;
        // Expression buffer
        StringBuilder expression = new StringBuilder();
        // Parsed tokens
        ArrayList<Token> parsed = new ArrayList<>();
        Token token = null;
        Lexer expressionLexer;

        while (brackets > 0 && currentSymbol != -1) {
            advance();
            if (currentSymbol == ')')
                brackets--;
            else
                expression.append((char) currentSymbol);
        }

        expressionLexer = new SwiftLexer(new StringSource(expression.toString()));

        do {
            token = expressionLexer.getToken();
            if (token != null)
                parsed.add(token);
        } while (token != null);

        interpolations.add(new Pair<Token, Token[]>(curToken, parsed.toArray(new Token[0])));
    }

    private int parseUnicodeEscape() {
        final String hexSymbols = "0123456789abcdefABCDEF";
        StringBuilder builder = new StringBuilder();
        while (currentSymbol != '}' && builder.length() < 8) {
            if (hexSymbols.contains(Character.toString((char) currentSymbol)))
                builder.append((char) currentSymbol);
            else
                return -1;
            advance();
        }
        return Integer.parseInt(builder.toString(), 16);
    }

    // Yes, I do hat myself for this. No, I don't have a better solution. Thanks, Java!
    private void skipTo(int position) {
        input.reset();
        while (input.getPosition() != position)
            input.consume();
    }

    /*
     * Lexing functions
     */
    @Override
    Token getToken() {
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
            case '#':
                return getExpressionLiteral();
        }

        return null;
    }

    Token getIdentifier() {
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
            } else {
                if (builder.length() == 1)
                    errors.add("Empty backtick identifier at " + Integer.toString(input.getPosition()));
                if (currentSymbol != '`') {
                    errors.add("No backtick at the end of an identifier starting with backtick at"
                            + Integer.toString(input.getPosition()));
                    // Attempt to fix the issue
                    builder.append('`');
                }
            }
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

    // TODO : According to Apple, there should be no trailing escaped newlines near the end of the multiline strings
    Token getStringLiteral() {
        StringBuilder builder = new StringBuilder();
        builder.append((char) currentSymbol);
        advance();
        // Yes, prevPrev. Can't think of a more elegant way for now.
        int prevPrevSymbol = -1;
        Token.TokenType tokType = Token.TokenType.STRING_LITERAL;
        // Needed for indexing into the interpolated string array. Not sure if that's a good idea
        Token out = new Token(tokType, lastPos);

        boolean multiline = false;

        // Check the type of string - multiline, single-line
        if (currentSymbol == '"') {
            // Multiline string
            builder.append((char) currentSymbol);
            advance();
            if (currentSymbol != '"') {
                // Empty single-line string, actually.
                return new Token(Token.TokenType.STRING_LITERAL, lastPos, builder.toString());
            }
            multiline = true;
            builder.append((char) currentSymbol);
            advance();
            // We need to ignore the first linebreak after the quotes
            if (currentSymbol == '\r') {
                advance();
                advance();
            } else if (currentSymbol == '\n') {
                advance();
            } else {
                errors.add("Multiline strings delimiter must be separated from the string by a newline at " +
                        Integer.toString(input.getPosition()));
            }
        }
        // First character
        builder.append((char) currentSymbol);
        while ((multiline && (prevPrevSymbol != '"' || prevSymbol != '"'))
                || (!multiline && prevSymbol == '\\') || currentSymbol != '"') {

            prevPrevSymbol = prevSymbol;
            advance();

            // EOF before the string is closed
            if (currentSymbol == -1) {
                errors.add("EOF before the string literal is read at " + Integer.toString(input.getPosition()));
                // We are always missing at least 1 quote
                builder.append('"');
                if (multiline) {
                    if (currentSymbol != '"')
                        builder.append('"');
                    if (prevSymbol != '"')
                        builder.append('"');
                }
                currentSymbol = '"';
                prevSymbol = '"';
                currentSymbol = '"';
            } else if (!multiline && (SymbolClasses.isLinebreak(currentSymbol)))
                // Single-line strings can't contain linefeeds and carriage returns
                errors.add("Single-line strings can't contain linefeeds and carriage returns at "
                        + Integer.toString(input.getPosition()));
            else {

                // For multi-line strings we need to ignore carriage returns, as per the standard
                if (prevSymbol == '\r' && currentSymbol == '\n') {
                    builder.deleteCharAt(builder.length() - 2);
                }

                if (currentSymbol == '\\') {
                    // Escaping characters
                    advance();
                    if (multiline && SymbolClasses.isLinebreak(currentSymbol)) {
                        // Putting a backslash at the end of the multiline string line ignores the linebreak
                        advance();
                        // CRLF uses 2 symbols, others use 1
                        if (prevSymbol == '\r' && currentSymbol == '\n')
                            advance();
                    } else if (currentSymbol == '(') {
                        // Interpolated string. Buffer the expression and lex it.
                        tokType = Token.TokenType.INTERPOLATED_STRING;
                        parseInterpolation(out);
                    } else if (currentSymbol == 'u') {
                        // Unicode value. Makes no sense to leave it as-is, so we parse and add it.
                        int unicodeValue = -1;

                        advance();
                        if (currentSymbol != '{') {
                            errors.add("{ expected after \\u at " + Integer.toString(input.getPosition()));
                        } else {
                            advance();
                        }
                        unicodeValue = parseUnicodeEscape();

                        if (unicodeValue == -1) {
                            errors.add("Incorrect unicode pattern at " + Integer.toString(input.getPosition()));
                            // Kinda bad, but at least helps a bit
                            unicodeValue = 32;
                        }
                        if (currentSymbol != '}')
                            errors.add("} expected at the end of \\u at " + Integer.toString(input.getPosition()));
                        else {
                            advance();
                        }

                        builder.append((char) unicodeValue);
                    } else {
                        // Check if the sequence requires escaping
                        if ((multiline && !multilineEscapable(currentSymbol))
                                || (!multiline && !singleEscapable(currentSymbol)))
                            errors.add("Unknown escape sequence at " + Integer.toString(input.getPosition()));
                        else
                            builder.append((char) currentSymbol);
                    }
                } else {
                    builder.append((char) currentSymbol);
                }
            }
        }
        // Consume the last quote
        advance();

        if (multiline) {
            // Get lines for processing indent
            String[] lines = builder.toString().split("\n");
            String last = lines[lines.length - 1];
            String indent = "";

            // Multiline strings MUST begin and end with newlines.
            if (!last.trim().startsWith("\"\"\"") ||
                    (last.trim().startsWith("\"\"\"") && last.trim().endsWith("\"\"\""))) {
                errors.add("Multiline string end delimiter must be separated by a newline at"
                        + Integer.toString(input.getPosition()));
                // Try to fix the problem and split the string again
                builder.insert(builder.length() - 3, '\n');
                lines = builder.toString().split("\n");
                last = lines[lines.length - 1];
            }

            indent = last.substring(0, last.indexOf("\"\"\""));

            if (indent.length() > 0) {
                int pos;
                for (int c = lines.length - 1; c >= 0; c--) {
                    pos = builder.indexOf(lines[c]);
                    if (lines[c].startsWith("\"\"\"")) {
                        lines[c] = lines[c].substring(3);
                        pos += 3;
                    }
                    if (lines[c].trim().length() > 0) {
                        if (lines[c].startsWith(indent)) {
                            builder.delete(pos, pos + indent.length());
                        }
                    }
                }
            }

            // A bit hacky, but much simpler. The last linebreak doesn't count too.
            int lastLineBreak = builder.lastIndexOf("\n");
            if (lastLineBreak != -1) {
                builder.delete(lastLineBreak, lastLineBreak + 1);
            }
        }
        out.type = tokType;
        out.value = builder.toString();
        return out;
    }

    Token getNumberLiteral() {
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

        if (prevSymbol != '0' && numType != NumType.INTEGER) {
            errors.add("Non-decimal numbers must start with a 0 at " + Integer.toString(input.getPosition()));
            builder.replace(0, 1, "0");
        }

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
                    if (curSymbols.indexOf(currentSymbol) == -1) {
                        errors.add("Incorrect digit at " + Integer.toString(input.getPosition()));
                        continue;
                    } else
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
                else {
                    errors.add("Incorrect decimal exponent symbol at " + Integer.toString(input.getPosition()));
                    continue;
                }
                builder.append((char) currentSymbol);
                advance();
                optionalPostfix = exponentPostfix;
                checkSymbol = true;
            } else if (currentSymbol == 'p' || currentSymbol == 'P') {
                // Check if we are dealing with a hex float now
                if (numType == NumType.HEX_INTEGER)
                    numType = NumType.HEX_FLOAT;
                else
                    errors.add("Incorrect hexadecimal exponent symbol at" + Integer.toString(input.getPosition()));
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
                    done = true;
                if (!done) {
                    builder.append((char) currentSymbol);
                    advance();
                }
            } else if (currentSymbol == '-' || currentSymbol == '+') {
                checkSymbol = true;
                // Check the stuff for the exponent
                if (numType == NumType.FLOAT_EXPONENT || numType == NumType.FLOAT_COMBO) {
                    if (prevSymbol != 'e' && prevSymbol != 'E')
                        return new Token(Token.TokenType.FLOAT_LITERAL, lastPos, builder.toString());
                } else if (numType == NumType.HEX_FLOAT) {
                    if (prevSymbol != 'p' && prevSymbol != 'P')
                        return new Token(Token.TokenType.FLOAT_LITERAL, lastPos, builder.toString());
                } else
                    done = true;
                if (!done) {
                    builder.append((char) currentSymbol);
                    advance();
                }
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
        StringBuilder builder = new StringBuilder();
        builder.append((char) currentSymbol);
        // Consume the leading pound
        advance();
        if (SymbolClasses.isIdentifierHead(currentSymbol)) {
            builder.append((char) currentSymbol);
            advance();
        }
        while (SymbolClasses.isIdentifierSymbol(currentSymbol)) {
            builder.append((char) currentSymbol);
            advance();
        }
        if (SwiftSpecials.isExpressionLiteral(builder.toString())) {
            return new Token(Token.TokenType.EXPRESSION_LITERAL, lastPos, builder.toString());
        }
        skipTo(lastPos + 1);
        // This method is somewhat bad, but that's what Apple uses in it's lexer, so it can be important
        return new Token(Token.TokenType.POUND, lastPos, "#");
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
