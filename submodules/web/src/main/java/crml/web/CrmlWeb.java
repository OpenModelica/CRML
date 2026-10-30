package crml.web;

import org.eclipse.emf.ecore.EObject;
import org.teavm.jso.JSExport;

import crml.compiler.crmlcv2.OMCv2;
import crml.language.dom.DOMVisitor;
import crml.language.dom.util.BuildResult;
import crml.language.util.Parser;
import crml.model.language.Model;

/** Translates CRML text to Modelica in the browser with the omcv2 pipeline. */
public class CrmlWeb {
  public static void main(String[] args) { }

  /**
   * Returns the Modelica text, or a text starting with "ERROR " when the
   * translation fails.
   */
  @JSExport
  public static String translate(String crml, String within) {
    try {
      Parser.ParserResult parsed = new Parser().parse(crml);
      if (parsed.ast() == null || parsed.syntax().hasErrors()) {
        return "ERROR unable to parse";
      }
      DOMVisitor visitor = new DOMVisitor();
      BuildResult result = visitor.build(parsed.ast());
      EObject dom = result instanceof BuildResult.SingleBuildResult
          ? ((BuildResult.SingleBuildResult<?>) result).result() : null;
      visitor.linker();
      visitor.resolveOperatorCalls();
      if (!(dom instanceof Model)) {
        return "ERROR unable to build the object model";
      }
      return "within " + within + ";\n" + new OMCv2().translate((Model) dom) + "\n";
    } catch (Exception e) {
      return "ERROR " + e;
    }
  }
}
