package crml.compiler.translation;

public class Range {
    final Integer start;
    final Integer end;

    public Range(Integer start, Integer end) {
        this.start = start;
        this.end = end;
    }

    public Integer getStart() {
        return start;
    }
}