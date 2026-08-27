package crml.compiler.omcv2.scopes;

// A self-contained inline expression built from a function/operator
// application (e.g. "sin(x)", "(a + b)", "not4(x)"). Behaves like
// RawstringScope - it just wraps text - but the name marks that the text is
// a complete standalone expression with nothing to hoist out, as opposed to
// a BlockScope which requires separate declarations on a host ModelScope.
public class FunctionScope implements Scope {
    private final String content;

    public FunctionScope(String content){
        this.content = content;
    }

    @Override
    public String toModelica(int indent) {
        return indent(indent)+content;
    }

    @Override
    public String reference() {
        return content;
    }
}
