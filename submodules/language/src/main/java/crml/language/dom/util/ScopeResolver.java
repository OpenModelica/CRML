package crml.language.dom.util;


import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.google.common.base.Objects;

import crml.language.dom.util.ScopeResolutionOptions.ResolutionStrategy;
import crml.model.language.Class;
import crml.model.language.ConstructorValue;
import crml.model.language.Domain;
import crml.model.language.Model;
import crml.model.language.OperatorHeaderElement;
import crml.model.language.Template;
import crml.model.language.TypeReference;
import crml.model.language.UserOperator;
import crml.model.language.UserTypereference;
import crml.model.language.Variable;

public class ScopeResolver {
    public boolean link(EObject target, EStructuralFeature feature, String id, ScopeResolutionOptions options) {
        if (options != null && options.strategy == ResolutionStrategy.CONSTRUCTOR_BINDING) {
            System.err.println("Binding resolution");
            EObject container = target.eContainer();
            while ((!(container instanceof ConstructorValue)) && (!(container instanceof Resource)) && (!(container instanceof ResourceSet))){
                container =  container.eContainer();
            }
            System.err.println("Container:" + container);
            if(container instanceof ConstructorValue){
                System.err.println("Waypoint 1:");
                ConstructorValue constructor = (ConstructorValue) container;
                TypeReference typeref = constructor.getDomain();
                if(typeref==null) return false;
                if(typeref instanceof UserTypereference){
                    System.err.println("Waypoint 2:");
                    UserTypereference userref = (UserTypereference) typeref;
                    Domain domain = userref.getDomain();
                    if(domain==null) return false;
                    if(domain instanceof Class){
                        System.err.println("Waypoint 3:");
                        Class cls = (Class) domain;
                        for(Variable v :cls.getVariables()){
                            System.err.println("Testing:" +v.getName());
                            if(id.equals(v.getName())){
                                System.err.println("MATCHES");
                                if (feature.isMany()) {
                                    ((EList) target.eGet(feature)).add(v);
                                } else {
                                    target.eSet(feature, v);
                                }
                                return true;
                            }
                        }

                    }
                    
                }
            }
            return false;
        } else {
            EObject candidate = getCandidate(target.eContainer(), id, options);
            if (candidate != null) {
                if (feature.isMany()) {
                    ((EList) target.eGet(feature)).add(candidate);
                } else {
                    target.eSet(feature, candidate);
                }
                return true;
            }

            return false;
        }
    }

    private EObject getCandidate(EObject obj, String id, ScopeResolutionOptions options) {
        // Idea is to have separate functions to resolve a variable from different
        // contexts (Model, Class, etc)
        // We move up level by level to find the closest match in a parent container
        // Termination happens if a Resource/REsource set is reached
        // If the current container is not in the processed one, we move a level up.
        // E.g., if the container is a Varaible or Operator (that cannot have childrent)
        // then we move to its container
        System.err.println("Resolve (EObject): " + id + " in " + obj);
        if (obj instanceof Class) {
            return getCandidate((Class) obj, id, options);
        } else if (obj instanceof Model) {
            return getCandidate((Model) obj, id, options);
        } else if (obj instanceof UserOperator) {
            return getCandidate((UserOperator) obj, id, options);
        } else if (obj instanceof Template) {
            return getCandidate((Template) obj, id, options);
        } else if (obj instanceof Resource || obj instanceof ResourceSet) {
            return null;
        } else {
            return getCandidate(obj.eContainer(), id, options);
        }
    }

    private EObject getCandidate(Class class1, String id, ScopeResolutionOptions options) {
        System.err.println("Resolve (Class): " + id + " in " + class1);
        if (class1 == null) {
            return null;
        }

        if (class1.getName().equals(id)) {
            return class1;
        }

        for (Variable v : class1.getVariables()) {
            if (v.getName().equals(id) && isReadCompatible(v.getDomain(), null)) {
                return v;
            }
        }
        return getCandidate(class1.eContainer(), id, null);
    }

    private EObject getCandidate(Model model, String id, ScopeResolutionOptions options) {
        System.err.println("Resolve (Model): " + id + " in " + model);
        if (model == null) {
            return null;
        }

        for (Variable v : model.getVariables()) {
            System.err.println("Test variable (Model): " + v.getName());
            if (v.getName().equals(id) && isReadCompatible(v.getDomain(), null)) {
                System.err.println("Test class (Model): MATCHES");
                return v;
            }
        }

        for (Class v : model.getClasses()) {
            System.err.println("Test class (Model): " + v.getName());
            if (v.getName().equals(id) && isReadCompatible(v, null)) {
                System.err.println("Test class (Model): MATCHES");
                return v;
            }
        }
        return null; // Models do not have a parent (they are root resources)
    }

    private EObject getCandidate(UserOperator op, String id, ScopeResolutionOptions options) {
        System.err.println("Resolve (UserOperator): " + id + " in " + op);
        if (op == null) {
            return null;
        }

        for (OperatorHeaderElement h : op.getHeader()) {
            if (h instanceof Variable) {
                Variable v = (Variable) h;
                if(Objects.equal(id, v.getName()) && isReadCompatible(v.getDomain(), null) ){
                    return v;
                };
            }
        }
        return getCandidate(op.eContainer(), id, null);
    }

    private EObject getCandidate(Template op, String id, ScopeResolutionOptions options) {
        System.err.println("Resolve (Template): " + id + " in " + op);
        if (op == null) {
            return null;
        }

        for (OperatorHeaderElement h : op.getHeader()) {
            if (h instanceof Variable) {
                Variable v = (Variable) h;
                if(Objects.equal(id, v.getName()) && isReadCompatible(v.getDomain(), null) ){
                    return v;
                };
            }
        }
        return getCandidate(op.eContainer(), id, null);
    }


    private boolean isReadCompatible(TypeReference variable, EClass required) {
        return true;
        // TODO: this is bad and has to be fixed.
    }

    private boolean isReadCompatible(Class variable, EClass required) {
        return true;
        // TODO: this is bad and has to be fixed.
    }
}
