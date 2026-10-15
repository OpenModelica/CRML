package crml.web;

import java.util.ArrayList;

import org.teavm.jso.JSExport;

import crml.compiler.translation.Value;
import crml.compiler.translation.crmlVisitorImpl;
import crml.language.util.Parser;

/** Translates CRML text to Modelica in the browser, as CRMLC.parse_file does for a file. */
public class CrmlWeb {
  public static void main(String[] args) { }

  /**
   * Returns the Modelica text, or a text starting with "ERROR " when the
   * translation fails.
   */
  @JSExport
  public static String translate(String crml, String within) {
    try {
      Parser.ParserResult model = new Parser().parse(crml);
      if (model.ast() == null) {
        return "ERROR unable to parse";
      }
      crmlVisitorImpl visitor = new crmlVisitorImpl(model.parser(), false);
      Value result = visitor.visit(model.ast());
      if (result == null) {
        return "ERROR unable to translate";
      }
      return "within " + within + ";\n" + result.toModelica() + "\n";
    } catch (Exception e) {
      return "ERROR " + e;
    }
  }
}
