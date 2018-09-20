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

        if (SymbolClasses.isIdentifierHead(currentSymbol))
            return getIdentifier();

        switch (currentSymbol) {

        }

        return null;
    }

    Token getIdentifier() {
        return null;
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
