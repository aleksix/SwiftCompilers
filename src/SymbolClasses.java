// Convinience class to deal with unicode.
public class SymbolClasses {
    public static boolean isWhitespace(int character) {
        return (character == 0x0000 || character == 0x0009 || character == 0x000B ||
                character == 0x000C || character == 0x0020 || isLinebreak(character));
    }

    public static boolean isLinebreak(int character) {
        // NOTE: This code doesn't check for \r\n, which shouldn't be a problem, but potentially might be?
        return (character == 0x000A || character == 0x000D);
    }

    public static boolean isIdentifierSymbol(int character) {
        return false;
    }

    public static boolean isOperatorSymbol(int character) {
        return false;
    }

    // Not sure if the following functions are needed, just copied the official docs.
    public static boolean isIdentifierHead(int chracter) {
        return false;
    }

    public static boolean isValidTextItem(int character) {
        return false;
    }

    public static boolean isOperatorHead(int character) {
        return false;
    }
}
