import java.io.*;

public class FileSource implements Source {
    private BufferedReader input;
    private int position = 0;
    private int next_character;

    FileSource(BufferedReader reader) throws IOException {
        input = reader;
        next_character = -1;
    }

    FileSource(File input) throws IOException {
        this(new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8")));
    }

    FileSource(String filename) throws IOException {
        this(new File(filename));
    }

    @Override
    public int consume() {
        try {
            next_character = input.read();
            position++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return next_character;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public int peek() {
        if (next_character == -1)
            consume();
        return next_character;
    }

    @Override
    public void reset() {
        try {
            input.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        position = 0;
    }
}
