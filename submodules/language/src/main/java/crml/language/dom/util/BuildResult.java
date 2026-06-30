package crml.language.dom.util;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

public abstract class BuildResult {
    public static class SingleBuildResult<T extends EObject> extends BuildResult{
        public final T result;
        private SingleBuildResult(T result) {
            this.result = result;
        }

        @SuppressWarnings("unchecked")
        public <D extends EObject> D result(){
            return (D) result; // This is ugly, but I have no idea have to make it type safe
        }
    }

    public static class MultiBuildResult<T extends EObject> extends BuildResult{
        public final List<T> results;
        public MultiBuildResult(T... result) {
            this.results = Arrays.asList(result);
        }

        @SuppressWarnings("unchecked")
        public <D extends EObject> List<D> results(){
            return (List<D>) results; // This is ugly, but I have no idea have to make it type safe
        }
        
    }

    public static <T extends EObject> BuildResult wrap(T... result){
        if(result.length == 0){
            return null;
        }
        if(result.length == 1){
            return new SingleBuildResult<T>(result[1]);
        }
        return new MultiBuildResult<T>(result);
    }
}
