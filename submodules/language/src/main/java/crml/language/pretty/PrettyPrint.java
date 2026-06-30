package crml.language.pretty;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public class PrettyPrint {
    public static void process(EObject root, int indentation, StringBuilder builder){
        if(root==null){
            builder.append("null");
            return;
        }
        String indent =  repeat("  ", indentation);
        String indent2 =  repeat("  ", indentation + 1);
        builder.append(indent).append(root.hashCode()).append(" (").append(root.eClass().getName()).append(")");
        builder.append(System.lineSeparator());
        for(EAttribute attr: root.eClass().getEAllAttributes()){
            builder.append(indent2).append("attr: ").append(attr.getName()).append(": ").append(root.eGet(attr).toString());
            builder.append(System.lineSeparator());
        }

        for(EReference ref: root.eClass().getEAllReferences()){
            if(ref.isContainment()){
                builder.append(indent2).append("cont: ").append(ref.getName()).append(":");

                if(ref.isMany()){
                    EList<?> ls = (EList<?>) root.eGet(ref);
                    for(Object o: ls){
                        process((EObject) o, indentation+1, builder);
                    }
                } else {
                    EObject trg = (EObject) root.eGet(ref);
                    process(trg, indentation+1, builder);
                }
            } else {
                if(ref.isMany()){
                    EList<?> ls = (EList<?>) root.eGet(ref);
                    for(Object obj: ls){
                        builder.append(indent2).append("ref: ").append(ref.getName()).append(": ").append(obj.hashCode());
                    }
                } else {
                    builder.append(indent2).append("ref: ").append(ref.getName()).append(": ").append(root.eGet(ref).hashCode());
                }
                
            }
            
            builder.append(System.lineSeparator());
        }
    }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder(s.length() * n);
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }

    public static String prettyPrint(EObject root){
        if(root == null){
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        process(root, 0, builder);
        return builder.toString();
    }
}
