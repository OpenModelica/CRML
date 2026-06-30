package crml.language.pretty;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public class PrettyPrint {
    public static void process(EObject root, int indentation, StringBuilder builder){
        String indent =  "  ".repeat(indentation);
        String indent2 =  "  ".repeat(indentation+1);
        builder.append(indent).append(root.hashCode()).append(" (").append(root.eClass().getName()).append(")");
        builder.append(System.lineSeparator());
        for(EAttribute attr: root.eClass().getEAllAttributes()){
            builder.append(indent2).append("attr: ").append(attr.getName()).append(": ").append(root.eGet(attr).toString());
            builder.append(System.lineSeparator());
        }

        for(EReference ref: root.eClass().getEAllReferences()){
            if(ref.isContainment()){
                process(root, indentation+1, builder);
            } else {
                builder.append(indent2).append("ref: ").append(ref.getName()).append(": ").append(root.eGet(ref).hashCode());
            }
            
            builder.append(System.lineSeparator());
        }
    }

    public static String prettyPrint(EObject root){
        StringBuilder builder = new StringBuilder();
        process(root, 0, builder);
        return builder.toString();
    }
}
