package crml.compiler;

import java.util.ArrayList;
import java.util.List;

import crml.language.grammar.crmlParser;
import crml.language.grammar.crmlParser.Class_defContext;
import crml.language.grammar.crmlParser.Class_var_defContext;
import crml.language.grammar.crmlParser.DefinitionContext;
import crml.language.grammar.crmlParser.Element_defContext;
import crml.language.grammar.crmlParser.TypeContext;
import crml.language.grammar.crmlParser.Var_defContext;

/**
 * Writes the external variables, the external sets and the requirements of a CRML model as JSON.
 *
 * The bindings editor of OMEdit reads this file to know which classes declare external variables,
 * which external sets have to be filled by a set binding, and which requirements the generated
 * verification model has to report. It works on the parse tree only, so it does not depend on the
 * translation to Modelica succeeding.
 */
public class ExternalsExport {

  /** One declared variable: name, element type, set marker, source line. */
  static class Variable {
    String name;
    String type;
    boolean isSet;
    int line;

    Variable(Var_defContext v) {
      name = v.id().getText();
      type = elementType(v.type());
      isSet = v.type().empty_set() != null;
      line = v.getStart().getLine();
    }
  }

  static class ClassInfo {
    String name;
    int line;
    List<Variable> externals = new ArrayList<>();
    List<Variable> requirements = new ArrayList<>();
  }

  private String modelName = "";
  private final List<ClassInfo> classes = new ArrayList<>();
  private final List<Variable> externals = new ArrayList<>();
  private final List<Variable> sets = new ArrayList<>();
  private final List<Variable> requirements = new ArrayList<>();
  private final List<Variable> instances = new ArrayList<>();

  /** The element type of a declaration, without the set marker {}. */
  static String elementType(TypeContext t) {
    return t.builtin_type() != null ? t.builtin_type().getText() : t.id().getText();
  }

  static boolean isRequirementType(String type) {
    return type.equals("Boolean") || type.equals("Requirement");
  }

  public ExternalsExport(crmlParser.DefinitionContext definition) {
    collect(definition);
  }

  private void collect(DefinitionContext def) {
    modelName = def.id().getText();
    for (Element_defContext e : def.element_def()) {
      if (e.class_def() != null) {
        classes.add(collectClass(e.class_def()));
      } else if (e.var_def() != null) {
        Var_defContext v = e.var_def();
        Variable var = new Variable(v);
        if (v.is_external != null) {
          if (var.isSet)
            sets.add(var);
          else
            externals.add(var);
        } else if (v.exp() != null && !var.isSet && isRequirementType(var.type)) {
          requirements.add(var);
        } else if (!var.isSet && isClass(var.type)) {
          instances.add(var);
        }
      }
    }
  }

  private boolean isClass(String type) {
    for (ClassInfo c : classes)
      if (c.name.equals(type))
        return true;
    return false;
  }

  private static ClassInfo collectClass(Class_defContext c) {
    ClassInfo info = new ClassInfo();
    info.name = c.id(0).getText();
    info.line = c.getStart().getLine();
    for (Class_var_defContext cv : c.class_var_def()) {
      Var_defContext v = cv.var_def();
      if (v == null)
        continue;
      Variable var = new Variable(v);
      if (v.is_external != null)
        info.externals.add(var);
      else if (v.exp() != null && !var.isSet && isRequirementType(var.type))
        info.requirements.add(var);
    }
    return info;
  }

  private static String quote(String s) {
    StringBuilder b = new StringBuilder("\"");
    for (char ch : s.toCharArray()) {
      switch (ch) {
        case '"': b.append("\\\""); break;
        case '\\': b.append("\\\\"); break;
        case '\n': b.append("\\n"); break;
        case '\r': b.append("\\r"); break;
        case '\t': b.append("\\t"); break;
        default:
          if (ch < 0x20)
            b.append(String.format("\\u%04x", (int) ch));
          else
            b.append(ch);
      }
    }
    return b.append('"').toString();
  }

  private static String variables(List<Variable> vars, String indent, boolean withSize) {
    if (vars.isEmpty())
      return "[]";
    StringBuilder b = new StringBuilder("[\n");
    for (int i = 0; i < vars.size(); i++) {
      Variable v = vars.get(i);
      b.append(indent).append("  {\"name\": ").append(quote(v.name))
          .append(", \"type\": ").append(quote(v.type));
      if (withSize)
        b.append(", \"sizeParameter\": ").append(quote(v.name + "_n"));
      b.append(", \"line\": ").append(v.line).append("}");
      b.append(i + 1 < vars.size() ? ",\n" : "\n");
    }
    return b.append(indent).append("]").toString();
  }

  /** The JSON text, schema crml-externals/0.1. */
  public String toJson(String sourceFile) {
    StringBuilder b = new StringBuilder();
    b.append("{\n");
    b.append("  \"schema\": \"crml-externals/0.1\",\n");
    b.append("  \"model\": ").append(quote(modelName)).append(",\n");
    b.append("  \"source\": ").append(quote(sourceFile)).append(",\n");
    b.append("  \"classes\": [");
    for (int i = 0; i < classes.size(); i++) {
      ClassInfo c = classes.get(i);
      b.append(i == 0 ? "\n" : ",\n");
      b.append("    {\"name\": ").append(quote(c.name)).append(", \"line\": ").append(c.line).append(",\n");
      b.append("     \"externals\": ").append(variables(c.externals, "     ", false)).append(",\n");
      b.append("     \"requirements\": ").append(variables(c.requirements, "     ", false)).append("}");
    }
    b.append(classes.isEmpty() ? "],\n" : "\n  ],\n");
    b.append("  \"externals\": ").append(variables(externals, "  ", false)).append(",\n");
    b.append("  \"sets\": ").append(variables(sets, "  ", true)).append(",\n");
    b.append("  \"instances\": ").append(variables(instances, "  ", false)).append(",\n");
    b.append("  \"requirements\": ").append(variables(requirements, "  ", false)).append("\n");
    b.append("}\n");
    return b.toString();
  }
}
