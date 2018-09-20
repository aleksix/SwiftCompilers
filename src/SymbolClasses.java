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
    public static boolean isIdentifierHead(int character) {
        //Ranges
        // A(U+0041)-Za-z(U+007A)
        if (character >= 0x0041 && character <= 0x007A)
            return true;
        else if (character >= 0x00B2 && character <= 0x00B5)
            return true;
        else if (character >= 0x00B7 && character <= 0x00BA)
            return true;
        else if (character >= 0x00BC && character <= 0x00BE)
            return true;
        else if (character >= 0x00C0 && character <= 0x00D6)
            return true;
        else if (character >= 0x00D8 && character <= 0x00F6)
            return true;
        else if (character >= 0x00F8 && character <= 0x00FF)
            return true;
        else if (character >= 0x0100 && character <= 0x02FF)
            return true;
        else if (character >= 0x0370 && character <= 0x167F)
            return true;
        else if (character >= 0x1681 && character <= 0x180D)
            return true;
        else if (character >= 0x180F && character <= 0x1DBF)
            return true;
        else if (character >= 0x1E00 && character <= 0x1FFF)
            return true;
        else if (character >= 0x200B && character <= 0x200D)
            return true;
        else if (character >= 0x202A && character <= 0x202E)
            return true;
        else if (character >= 0x203F && character <= 0x2040)
            return true;
        else if (character >= 0x2060 && character <= 0x206F)
            return true;
        else if (character >= 0x2070 && character <= 0x20CF)
            return true;
        else if (character >= 0x2100 && character <= 0x218F)
            return true;
        else if (character >= 0x2460 && character <= 0x24FF)
            return true;
        else if (character >= 0x2776 && character <= 0x2793)
            return true;
        else if (character >= 0x2C00 && character <= 0x2DFF)
            return true;
        else if (character >= 0x2E80 && character <= 0x2FFF)
            return true;
        else if (character >= 0x3004 && character <= 0x3007)
            return true;
        else if (character >= 0x3021 && character <= 0x302F)
            return true;
        else if (character >= 0x3031 && character <= 0x303F)
            return true;
        else if (character >= 0x3040 && character <= 0xD7FF)
            return true;
        else if (character >= 0xF900 && character <= 0xFD3D)
            return true;
        else if (character >= 0xFD40 && character <= 0xFDCF)
            return true;
        else if (character >= 0xFDF0 && character <= 0xFE1F)
            return true;
        else if (character >= 0xFE30 && character <= 0xFE44)
            return true;
        else if (character >= 0xFE47 && character <= 0xFFFD)
            return true;
        else if (character >= 0x10000 && character <= 0x1FFFD)
            return true;
        else if (character >= 0x20000 && character <= 0x2FFFD)
            return true;
        else if (character >= 0x30000 && character <= 0x3FFFD)
            return true;
        else if (character >= 0x40000 && character <= 0x4FFFD)
            return true;
        else if (character >= 0x50000 && character <= 0x5FFFD)
            return true;
        else if (character >= 0x60000 && character <= 0x6FFFD)
            return true;
        else if (character >= 0x70000 && character <= 0x7FFFD)
            return true;
        else if (character >= 0x80000 && character <= 0x8FFFD)
            return true;
        else if (character >= 0x90000 && character <= 0x9FFFD)
            return true;
        else if (character >= 0xA0000 && character <= 0xAFFFD)
            return true;
        else if (character >= 0xB0000 && character <= 0xBFFFD)
            return true;
        else if (character >= 0xC0000 && character <= 0xCFFFD)
            return true;
        else if (character >= 0xD0000 && character <= 0xDFFFD)
            return true;
        else if (character >= 0xE0000 && character <= 0xEFFFD)
            return true;

        //Individual characters
        switch (character) {
            case 0x00A8:
            case 0x00AA:
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
        return false;
    }
}
