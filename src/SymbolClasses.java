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
        if (isIdentifierHead(character))
            return true;
            // 0(U+0030) - 9 (U+0039)
        else if (inRange(character, 0x0030, 0x0039))
            return true;
        else if (inRange(character, 0x0300, 0x036F))
            return true;
        else if (inRange(character, 0x1DC0, 0x1DFF))
            return true;
        else if (inRange(character, 0x20D0, 0x20FF))
            return true;
        else if (inRange(character, 0xFE20, 0xFE2F))
            return true;
        return false;
    }

    public static boolean isOperatorSymbol(int character) {
        if (isOperatorHead(character))
            return true;
        else if (inRange(character, 0x0300, 0x036F))
            return true;
        else if (inRange(character, 0x1DC0, 0x1DFF))
            return true;
        else if (inRange(character, 0x20D0, 0x20FF))
            return true;
        else if (inRange(character, 0xFE00, 0xFE0F))
            return true;
        else if (inRange(character, 0xFE20, 0xFE2F))
            return true;
        else if (inRange(character, 0xE0100, 0xE01EF))
            return true;
        return false;
    }

    // Not sure if the following functions are needed, just copied the official docs.
    public static boolean isIdentifierHead(int character) {
        //Ranges
        // A(U+0041)-Z(U+005A)
        if (inRange(character, 0x0041, 0x005A))
            return true;
            // a(U+0061)-z(U+007A)
        else if (inRange(character, 0x0061, 0x007A))
            return true;
        else if (inRange(character, 0x00B2, 0x00B5))
            return true;
        else if (inRange(character, 0x00B7, 0x00BA))
            return true;
        else if (inRange(character, 0x00BC, 0x00BE))
            return true;
        else if (inRange(character, 0x00C0, 0x00D6))
            return true;
        else if (inRange(character, 0x00D8, 0x00F6))
            return true;
        else if (inRange(character, 0x00F8, 0x00FF))
            return true;
        else if (inRange(character, 0x0100, 0x02FF))
            return true;
        else if (inRange(character, 0x0370, 0x167F))
            return true;
        else if (inRange(character, 0x1681, 0x180D))
            return true;
        else if (inRange(character, 0x180F, 0x1DBF))
            return true;
        else if (inRange(character, 0x1E00, 0x1FFF))
            return true;
        else if (inRange(character, 0x200B, 0x200D))
            return true;
        else if (inRange(character, 0x202A, 0x202E))
            return true;
        else if (inRange(character, 0x203F, 0x2040))
            return true;
        else if (inRange(character, 0x2060, 0x206F))
            return true;
        else if (inRange(character, 0x2070, 0x20CF))
            return true;
        else if (inRange(character, 0x2100, 0x218F))
            return true;
        else if (inRange(character, 0x2460, 0x24FF))
            return true;
        else if (inRange(character, 0x2776, 0x2793))
            return true;
        else if (inRange(character, 0x2C00, 0x2DFF))
            return true;
        else if (inRange(character, 0x2E80, 0x2FFF))
            return true;
        else if (inRange(character, 0x3004, 0x3007))
            return true;
        else if (inRange(character, 0x3021, 0x302F))
            return true;
        else if (inRange(character, 0x3031, 0x303F))
            return true;
        else if (inRange(character, 0x3040, 0xD7FF))
            return true;
        else if (inRange(character, 0xF900, 0xFD3D))
            return true;
        else if (inRange(character, 0xFD40, 0xFDCF))
            return true;
        else if (inRange(character, 0xFDF0, 0xFE1F))
            return true;
        else if (inRange(character, 0xFE30, 0xFE44))
            return true;
        else if (inRange(character, 0xFE47, 0xFFFD))
            return true;
        else if (inRange(character, 0x10000, 0x1FFFD))
            return true;
        else if (inRange(character, 0x20000, 0x2FFFD))
            return true;
        else if (inRange(character, 0x30000, 0x3FFFD))
            return true;
        else if (inRange(character, 0x40000, 0x4FFFD))
            return true;
        else if (inRange(character, 0x50000, 0x5FFFD))
            return true;
        else if (inRange(character, 0x60000, 0x6FFFD))
            return true;
        else if (inRange(character, 0x70000, 0x7FFFD))
            return true;
        else if (inRange(character, 0x80000, 0x8FFFD))
            return true;
        else if (inRange(character, 0x90000, 0x9FFFD))
            return true;
        else if (inRange(character, 0xA0000, 0xAFFFD))
            return true;
        else if (inRange(character, 0xB0000, 0xBFFFD))
            return true;
        else if (inRange(character, 0xC0000, 0xCFFFD))
            return true;
        else if (inRange(character, 0xD0000, 0xDFFFD))
            return true;
        else if (inRange(character, 0xE0000, 0xEFFFD))
            return true;

        //Individual characters
        switch (character) {
            case 0x00A8:
            case 0x00AA:
            case 0x005F:
            case 0x00AD:
            case 0x00AF:
            case 0x2054:
                return true;
        }
        return false;
    }

    public static boolean isValidTextItem(int character) {
        return false;
    }

    public static boolean isOperatorHead(int character) {
        // Ranges
        if (inRange(character, 0x00A1, 0x00A7))
            return true;
        else if (inRange(character, 0x2020, 2027))
            return true;
        else if (inRange(character, 0x2030, 0x203E))
            return true;
        else if (inRange(character, 0x2041, 0x2053))
            return true;
        else if (inRange(character, 0x2055, 0x205E))
            return true;
        else if (inRange(character, 0x2190, 0x23FF))
            return true;
        else if (inRange(character, 0x2500, 0x2775))
            return true;
        else if (inRange(character, 0x2794, 0x2BFF))
            return true;
        else if (inRange(character, 0x2E00, 0x2E7F))
            return true;
        else if (inRange(character, 0x3001, 0x3003))
            return true;
        else if (inRange(character, 0x3008, 0x3020))
            return true;

        // Individual characters
        switch (character) {
            case '/':
            case '=':
            case '-':
            case '+':
            case '!':
            case '*':
            case '%':
            case '<':
            case '>':
            case '&':
            case '|':
            case '^':
            case '~':
            case '?':
            case 0x3030:
            case 0x2016:
            case 0x2017:
            case 0x00A9:
            case 0x00AB:
            case 0x00AC:
            case 0x00AE:
            case 0x00B0:
            case 0x00B1:
            case 0x00B6:
            case 0x00BB:
            case 0x00BF:
            case 0x00D7:
            case 0x00F7:

                return true;
        }
        return false;
    }

    private static boolean inRange(int character, int min, int max) {
        if (character >= min && character <= max)
            return true;
        return false;
    }
}
