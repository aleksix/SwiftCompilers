public class StringSource implements Source {
    private String input;
    private int position;

    StringSource(String input) {
        this.input = input;
        position = 0;
    }

    @Override
    public int consume() {
        if (position < input.length() - 1) {
            return input.charAt(++position);
        }
        position++;
        return -1;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public int peek() {
        if (position < input.length())
            return input.charAt(position);
        return -1;
    }
}
