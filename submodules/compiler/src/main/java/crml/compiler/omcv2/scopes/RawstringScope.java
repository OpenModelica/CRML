package crml.compiler.omcv2.scopes;

public class RawstringScope implements Scope{
    private final String content;
    public RawstringScope(String content){
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
