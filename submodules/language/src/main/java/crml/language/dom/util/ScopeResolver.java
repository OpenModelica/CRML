package crml.language.dom.util;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import crml.model.language.Class;
import crml.model.language.Model;
import crml.model.language.TypeReference;
import crml.model.language.Variable;

public class ScopeResolver {
    public boolean link(EObject target, EStructuralFeature feature, String id, EClass targetType){
        EObject candidate = getCandidate(target.eContainer(), id, targetType);
        if(candidate != null){
            if(feature.isMany()){
                ((EList) target.eGet(feature)).add(candidate);
            } else {
                target.eSet(feature, candidate);
            }
            return true;
        }

        return false;
    }

    private EObject getCandidate(EObject obj, String id, EClass targetType){
        // Idea is to have separate functions to resolve a variable from different contexts (Model, Class, etc)
        // We move up level by level to find the closest match in a parent container
        // Termination happens if a Resource/REsource set is reached
        // If the current container is not in the processed one, we move a level up. 
        //     E.g., if the container is a Varaible or Operator (that cannot have childrent) then we move to its container
        System.err.println("Resolve (EObject): "+ id+ " in "+obj);
        if(obj instanceof Class){
            return getCandidate((Class) obj, id, targetType);
        } else if (obj instanceof Model) {
            return getCandidate((Model) obj, id, targetType);
        } else if (obj instanceof Resource || obj instanceof ResourceSet) {
            return null; 
        } else {
            return getCandidate(obj.eContainer(), id, targetType);
        }
    }

    private EObject getCandidate(Class class1, String id, EClass targetType){
        System.err.println("Resolve (Class): "+ id+ " in "+class1);
        if(class1 == null){
            return null;
        }

        if(class1.getName().equals(id)){
            return class1;
        }

        for(Variable v : class1.getVaraibles()){
            if(v.getName().equals(id) && isReadCompatible(v.getDomain(), targetType)){
                return v;
            }
        }
        return getCandidate(class1.eContainer(), id, targetType);
    }

    private EObject getCandidate(Model model, String id, EClass targetType){
        System.err.println("Resolve (Model): "+ id+ " in "+model);
        if(model == null){
            return null;
        }

        for(Variable v : model.getVaraibles()){
            System.err.println("Test variable (Model): "+ v.getName());
            if(v.getName().equals(id) && isReadCompatible(v.getDomain(), targetType)){
                System.err.println("Test class (Model): MATCHES");
                return v;
            }
        }

        for(Class v : model.getClasses()){
            System.err.println("Test class (Model): "+ v.getName());
            if(v.getName().equals(id) && isReadCompatible(v, targetType)){
                System.err.println("Test class (Model): MATCHES");
                return v;
            }
        }
        return null; // Models do not have a parent (they are root resources)
    }

    private boolean isReadCompatible(TypeReference variable, EClass required){
        return true;
        //TODO:  this is bad and has to be fixed.
    }

    private boolean isReadCompatible(Class variable, EClass required){
        return true;
        //TODO:  this is bad and has to be fixed.
    }
}
