package crml.test;

public interface CustomHtmlReporter {
    Object report();

    /**
     * True for content too long to show expanded - code, logs, stack traces,
     * tree dumps. The report then shows it collapsed under its key.
     */
    default boolean collapsible() {
        return false;
    }

    /** A short note shown next to the key of a collapsed entry, such as its size; null for none. */
    default String summary() {
        return null;
    }
}
