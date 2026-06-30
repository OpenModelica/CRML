package crml.language.dom;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import crml.language.dom.builders.RootBuilder;
import crml.language.dom.util.BuildResult;
import crml.language.grammar.crmlBaseVisitor;
import crml.language.grammar.crmlParser.DefinitionContext;

public class DOMVisitor extends crmlBaseVisitor<BuildResult> implements BuildContext {
    private final RootBuilder root = new RootBuilder(this);
    //private final StatementBuilder  statements  = new StatementBuilder(this);
    //private final DeclarationBuilder decls      = new DeclarationBuilder(this);

    // recursion funnels through here, so dispatch stays in one place
    @Override public BuildResult build(ParseTree n) { return visit(n); }
    @Override public <T extends BuildResult> T build(ParseTree n, Class<T> type) {
        return type.cast(visit(n));
    }

    // routing — mechanical, no instanceof
    @Override public BuildResult visitDefinition(DefinitionContext c) { return BuildResult.wrap(root.root(c)); }
    @Override
    public void link(EObject host, EStructuralFeature reference, String id, EClass targetType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'link'");
    }
}
