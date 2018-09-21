import java.io.File;

public abstract class Lexer {
    protected Source input;
    protected int current = 0;

    protected File input_file;

    Lexer(Source input) {
        this.input = input;
    }

    abstract Token getToken();
}
