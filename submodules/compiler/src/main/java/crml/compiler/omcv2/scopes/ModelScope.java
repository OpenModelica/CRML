package crml.compiler.omcv2.scopes;

import java.util.ArrayList;
import java.util.List;

public class ModelScope implements Scope {
    private String name;
    private List<Scope> variables = new ArrayList<>();
    private List<Scope> equations = new ArrayList<>();

    public ModelScope(String name) {
        this.name = name;
    }

    public void addVariable(Scope variable){
        variables.add(variable);
    };
    public void addVariable(List<Scope> variable){
        variables.addAll(variable);
    };
    public void addEquation(Scope eq){
        equations.add(eq);
    };
    public void addEquation(List<Scope> eq){
        equations.addAll(eq);
    };

    @Override
    public String toModelica(int i) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(i)+"model '"+name+"'").append(System.lineSeparator());
        for(Scope scp : variables){
            builder.append(scp.toModelica(i+1)).append(System.lineSeparator());
        }
        builder.append(indent(i)+"equation").append(System.lineSeparator());
        for(Scope scp : equations){
            builder.append(scp.toModelica(i+1)).append(System.lineSeparator());
        }
        builder.append(indent(i)+"end "+name+";");
        return builder.toString();
    }
}
