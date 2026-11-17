package crml.web;

import org.eclipse.emf.ecore.EObject;
import org.teavm.jso.JSExport;

import org.openmodelica.crml.compiler.crmlcv2.OMCv2;
import org.openmodelica.crml.language.workspace.CrmlResource;
import org.openmodelica.crml.language.workspace.CrmlWorkspace;
import org.openmodelica.crml.model.metamodel.Model;

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
      CrmlWorkspace workspace = new CrmlWorkspace();
      CrmlResource resource = workspace.load(crml);
      if (resource.getState() == CrmlResource.State.SYNTAX_ERRORS) {
        return "ERROR unable to parse";
      }
      workspace.resolve();
      if (!resource.getErrors().isEmpty()) {
        return "ERROR " + resource.getErrors().get(0);
      }
      EObject dom = resource.getRoot();
      if (!(dom instanceof Model)) {
        return "ERROR unable to build the object model";
      }
      return "within " + within + ";\n" + new OMCv2().translate((Model) dom) + "\n";
    } catch (Exception e) {
      return "ERROR " + e;
    }
  }
}
