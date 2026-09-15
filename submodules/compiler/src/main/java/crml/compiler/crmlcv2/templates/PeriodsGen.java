package crml.compiler.crmlcv2.templates;

import java.util.ArrayList;
import java.util.List;

import crml.compiler.crmlcv2.scopes.ModelScope;
import crml.compiler.crmlcv2.util.LineMerger;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BuiltinType;
import crml.model.language.OperatorHeaderElement;
import crml.model.language.PeriodsValue;

public class PeriodsGen {
    public static String generate(ModelScope host, PeriodsValue periods){
        List<String> lines = new ArrayList<>();

        lines.add(TypeResolver.resolve(BuiltinType.PERIOD) + "p"+periods.hashCode()+"(");
        lines.add("    isLeftBoundaryIncluded="+periods.getIsStartInclusive()+",");
        lines.add("    isRightBoundaryIncluded="+periods.getIsEndInclusive()+",");
        lines.add("    start="+ValueGen.generate(host, periods.getStartValue()).reference()+",");
        lines.add("    close_event="+ValueGen.generate(host, periods.getEndValue()).reference());
        lines.add(");");
        return LineMerger.merge(lines);
    }
}
