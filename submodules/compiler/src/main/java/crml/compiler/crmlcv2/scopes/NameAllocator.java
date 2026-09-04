package crml.compiler.crmlcv2.scopes;

// Placeholder naming scheme for BlockScope instance names: hands out
// prefix+N for increasing N, unique within one allocator instance. Good
// enough to avoid collisions; expect this to be replaced with something that
// produces more readable/stable names (and is scoped per-host rather than
// passed around separately) later.
public class NameAllocator {
    private int counter = 0;

    public String allocate(String prefix){
        counter++;
        return prefix + counter;
    }
}