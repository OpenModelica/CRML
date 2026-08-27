package crml.compiler.omcv2.scopes;

// This hierarchy (RawstringScope/FunctionScope/BlockScope/ModelScope) is a
// rough first cut and doesn't track Modelica's own class kinds (model,
// block, function, connector, record) directly. Expect it to be reworked to
// align with those more closely once BlockScope-style component
// instantiation sees real use.
public interface Scope {
    default String toModelica(){
        return toModelica(0);
    };
    String toModelica(int indent);

    // Short text to embed when this Scope's value is used inside another
    // expression, as opposed to toModelica() which emits everything needed
    // to define it (declarations, helper equations, ...). Null means this
    // Scope has nothing short to point at and cannot be embedded inline; a
    // Scope that declares helper variables/equations on a host ModelScope
    // should override this to return just the resulting variable's name.
    default String reference(){
        return null;
    }

    default String indent(int i){
        return "    ".repeat(i);
    }
}
