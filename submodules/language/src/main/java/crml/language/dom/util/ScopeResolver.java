package crml.language.dom.util;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import crml.model.language.Class;
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
        if(obj instanceof Class){
            return getCandidate((Class) obj, id, targetType);
        }

        return null;
    }

    private EObject getCandidate(Class class1, String id, EClass targetType){
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

    private boolean isReadCompatible(TypeReference variable, EClass required){
        return true;
        //TODO:  this is bad and has to be fixed.
    }
}
