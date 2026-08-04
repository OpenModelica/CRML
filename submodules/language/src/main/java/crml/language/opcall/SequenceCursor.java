package crml.language.opcall.ai;

import crml.model.language.Keyword;
import crml.model.language.Sequence;
import crml.model.language.SequenceKeyword;

public class SequenceCursor {
    private Sequence current;
    private int position; // For error reporting

    public SequenceCursor(Sequence start) {
        this.current  = start;
        this.position = 0;
    }

    public Sequence peek() {
        return current;
    }

    public boolean isExhausted() {
        return current == null;
    }

    public int getPosition() {
        return position;
    }

    public Sequence consume() {
        if (current == null) {
            throw new IllegalStateException("Unexpected end of sequence at position "+ position);
        }
        Sequence c = current;
        current = c.getNext();
        position++;
        return c;
    }

    public boolean tryKeyword(Keyword token) {
        if (current instanceof SequenceKeyword) {
            SequenceKeyword kw = (SequenceKeyword) current;
            if (kw.getKeyword().equals(token.getKeyword())) {
                current = kw.getNext();
                position++;
                return true;
            }
        }
        return false;
    }

    // ── Backtracking ─────────────────────────────────────────────────────────

    public Checkpoint save() {
        return new Checkpoint(current, position);
    }

    public void restore(Checkpoint checkpoint) {
        this.current  = checkpoint.sequence;
        this.position = checkpoint.position;
    }

    public static final class Checkpoint {
        private final Sequence sequence;
        private final int      position;

        private Checkpoint(Sequence sequence, int position) {
            this.sequence = sequence;
            this.position = position;
        }
    }
}
