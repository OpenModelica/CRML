package org.openmodelica.crml.language.dom;

import java.util.List;

public abstract class CRMLLanguageException extends RuntimeException {
    
    protected CRMLLanguageException(String message) {
        super(message);
    }

    public static CRMLDomModificationFailure domFailure(List<?> errors){
        return new CRMLDomModificationFailure(errors);
    }
    public static class CRMLDomModificationFailure extends CRMLLanguageException{

        public CRMLDomModificationFailure(List<?> errors) {
            super("Unable to modify tasks:"+stringify(errors));
        }
    }

    public static CRMLInconsistentUse inconsistent(String message){
        return new CRMLInconsistentUse(message);
    }
    public static class CRMLInconsistentUse extends CRMLLanguageException{

        public CRMLInconsistentUse(String message) {
            super(message);
        }
    }

    public static CRMLLinkerFailure linkerFailure(List<?> errors){
        return new CRMLLinkerFailure(errors);
    }
    public static class CRMLLinkerFailure extends CRMLLanguageException{

        public CRMLLinkerFailure(List<?> errors) {
            super("Unable to resolve tasks:"+stringify(errors));
        }
    }

    private static String stringify(List<?> data){
            StringBuilder b = new StringBuilder();
            data.forEach(entry -> {
                b.append(System.lineSeparator());
                b.append(entry.toString());
            });
            return b.toString();
    }
}
