package crml.compiler.crmlcv2;

import crml.compiler.crmlcv2.scopes.Scope;
import crml.compiler.crmlcv2.templates.ModelGen;
import crml.model.language.Model;

public class OMCv2 {
    public String translate(Model model){
        ModelGen gen = new ModelGen();
        Scope modelica = gen.generate(model);
        return modelica.toModelica(0);
    }
}
