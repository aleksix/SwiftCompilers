// Convenience class, to remove clutter from the file with the lexer
public class SwiftSpecials {
    static final String[] keywords = {
            // Declarations
            "class", "deinit", "enum", "extension", "func",
            "import", "init", "internal", "let", "operator",
            "private", "protocol", "public", "static", "struct",
            "subscript", "typealias", "var", "open", "inout",
            "fileprivate",
            // Statements
            "break", "case", "continue", "default", "do",
            "else", "fallthrough", "for", "if", "in",
            "return", "switch", "where", "while",
            "defer", "guard", "repeat",
            // Expressions
            "as", "dynamicType", "is", "self", "Self",
            "super", "Any", "catch", "rethrows", "throw",
            "throws", "try"
    };

    static final String[] expressionLiterals = {
            "#keyPath", "#line", "#selector", "#file",
            "#column", "#function", "#dsohandle", "#sourceLocation", "#warning",
            "#error", "#if", "#else", "#elseif", "#endif",
            "#available", "#fileLiteral", "#imageLiteral", "#colorLiteral"
    };

    static final String[] contextSensitive = {
            "associativity", "convenience", "dynamic", "didSet", "final",
            "get", "infix", "indirect", "lazy", "left",
            "mutating", "none", "nonmutating", "optional", "override",
            "postfix", "precedence", "prefix", "Protocol", "required",
            "right", "set", "Type", "unowned", "weak",
            "willSet"
    };


    private static boolean isContainedIn(String identifier, String[] array) {
        for (String item : array) {
            if (item.equals(identifier))
                return true;
        }
        return false;
    }

    public static boolean isKeyword(String identifier) {
        return isContainedIn(identifier, keywords);
    }

    public static boolean isExpressionLiteral(String identifier) {
        return isContainedIn(identifier, expressionLiterals);
    }

    public static boolean isContextSensitive(String identifier) {
        return isContainedIn(identifier, contextSensitive);
    }
}
