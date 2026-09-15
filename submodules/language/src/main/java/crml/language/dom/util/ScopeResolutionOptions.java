package crml.language.dom.util;

public class ScopeResolutionOptions {
    public final ResolutionStrategy strategy;

    public ScopeResolutionOptions(){
        this(ResolutionStrategy.DEFAULT);
    }

    
    public ScopeResolutionOptions(ResolutionStrategy strategy){
        this.strategy = strategy;
    }

    public static enum ResolutionStrategy {
        DEFAULT,
        CONSTRUCTOR_BINDING
    }
    
}
