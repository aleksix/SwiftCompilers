import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws LexingError {
        Lexer lex = new OldSwiftLexer(new StringSource(
                "/* My first program in Swift 4 */\r\n" +
                        "// With comments!\r\n" +
                        "var myString = \"Hello, World!\"\r\n" +
                        "var `class` = 5\r\n" +
                        "\r\n" +
                        "print(myString)\r\n" +
                        "print(15e10)\r\n"
        ));

        ArrayList<Token> toks = new ArrayList<>();
        Token t = null;
        do {
            t = lex.getToken();
            if (t != null)
                toks.add(t);
        } while (t != null);
        System.out.println();
    }
}


