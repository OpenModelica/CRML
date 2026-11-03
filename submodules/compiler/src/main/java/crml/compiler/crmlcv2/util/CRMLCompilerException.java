package crml.compiler.crmlcv2.util;

import crml.compiler.crmlcv2.Diagnostic;

public abstract class CRMLCompilerException extends RuntimeException{
    protected CRMLCompilerException(String message){
        super(message);
    }

    public static Unsupported unsupported(){
        return new Unsupported();
    }
    public static Unsupported unsupported(String message){
        return new Unsupported(message);
    }
    public static class Unsupported extends CRMLCompilerException{
        public Unsupported() {
            super("This feature is not supported.");
        }
        public Unsupported(String message) {
            super(message);
        }
    }

    public static UnsupportedConstruct unsupported(Diagnostic diagnostic){
        return new UnsupportedConstruct(diagnostic);
    }
    public static class UnsupportedConstruct extends CRMLCompilerException {
        private final transient Diagnostic diagnostic;

        protected  UnsupportedConstruct(Diagnostic diagnostic) {
            super(diagnostic.message());
            this.diagnostic = diagnostic;
        }

        public Diagnostic diagnostic() {
            return diagnostic;
        }
    }

    public static Unreachable unreachable(){
        return new Unreachable();
    }
    public static class Unreachable extends CRMLCompilerException{
        protected Unreachable() {
            super("This line should be unreachable.");
        }
    }
}
