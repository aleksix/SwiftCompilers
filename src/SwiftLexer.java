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

        switch (currentSymbol) {

        }

        return null;
    }

    void ignoreCommentsAndWhitespace() {
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

    Token getOperatorLiteral(int prevCharacter) {
        return null;
    }
}
