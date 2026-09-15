package crml.language.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import crml.model.language.BinaryOperator;
import crml.model.language.BooleanConstant;
import crml.model.language.ComputedValue;
import crml.model.language.ConstructorValue;
import crml.model.language.DurationValue;
import crml.model.language.IfValue;
import crml.model.language.IntegerConstant;
import crml.model.language.IntegrateValue;
import crml.model.language.PeriodsValue;
import crml.model.language.ProjectionValue;
import crml.model.language.RealConstant;
import crml.model.language.StringConstant;
import crml.model.language.TimeValue;
import crml.model.language.UnaryOperator;
import crml.model.language.Value;
import crml.model.language.VariableReference;
import crml.model.language.util.LanguageSwitch;

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
    public static final LanguageSwitch<Boolean> foo = new LanguageSwitch<Boolean>() {
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
