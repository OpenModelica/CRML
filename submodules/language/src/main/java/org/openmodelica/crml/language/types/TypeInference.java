package org.openmodelica.crml.language.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import org.openmodelica.crml.model.metamodel.BinaryOperator;
import org.openmodelica.crml.model.metamodel.BooleanConstant;
import org.openmodelica.crml.model.metamodel.ComputedValue;
import org.openmodelica.crml.model.metamodel.ConstructorValue;
import org.openmodelica.crml.model.metamodel.DurationValue;
import org.openmodelica.crml.model.metamodel.IfValue;
import org.openmodelica.crml.model.metamodel.IntegerConstant;
import org.openmodelica.crml.model.metamodel.IntegrateValue;
import org.openmodelica.crml.model.metamodel.PeriodsValue;
import org.openmodelica.crml.model.metamodel.ProjectionValue;
import org.openmodelica.crml.model.metamodel.RealConstant;
import org.openmodelica.crml.model.metamodel.StringConstant;
import org.openmodelica.crml.model.metamodel.TimeValue;
import org.openmodelica.crml.model.metamodel.UnaryOperator;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.VariableReference;
import org.openmodelica.crml.model.metamodel.util.CRMLSwitch;

public class TypeInference {
    public void perform(EObject root){
        List<Value> tasks = new ArrayList<>();

        for(EObject content :root.eContents()){
            if(content instanceof Value){
                tasks.add((Value) content);
            } 
            perform(content);
        }
        
        tasks.removeIf(foo::doSwitch);
    }
    public static final CRMLSwitch<Boolean> foo = new CRMLSwitch<Boolean>() {
        @Override public Boolean caseUnaryOperator(UnaryOperator object) { return false; }
        @Override public Boolean caseBinaryOperator(BinaryOperator object) { return false; }

        @Override public Boolean caseVariableReference(VariableReference object) { return false; }
        @Override public Boolean caseComputedValue(ComputedValue object) { return false; }
        @Override public Boolean casePeriodsValue(PeriodsValue object) { return false; }
        @Override public Boolean caseIntegrateValue(IntegrateValue object) { return false; }
        @Override public Boolean caseDurationValue(DurationValue object) { return false; }
        @Override public Boolean caseProjectionValue(ProjectionValue object) { return false; }
        @Override public Boolean caseIfValue(IfValue object) { return false; }
        @Override public Boolean caseConstructorValue(ConstructorValue object) { return false; }
        
        @Override public Boolean caseBooleanConstant(BooleanConstant object) { return true; }
        @Override public Boolean caseIntegerConstant(IntegerConstant object) { return true; }
        @Override public Boolean caseRealConstant(RealConstant object) { return true; }
        @Override public Boolean caseTimeValue(TimeValue object) { return true; }
        @Override public Boolean caseStringConstant(StringConstant object) { return true; }

    };
}
