// Initially called SourceSource, but that sounded a bit dumb
public interface Source {
    int consume();

    int getPosition();

    int peek();

    void reset();
}
