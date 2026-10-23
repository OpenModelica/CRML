package crml.modelica.print;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import crml.model.modelica.ArrayConstructor;
import crml.model.modelica.ArrayDimension;
import crml.model.modelica.BinaryExpression;
import crml.model.modelica.BinaryOperatorKind;
import crml.model.modelica.BooleanLiteral;
import crml.model.modelica.Causality;
import crml.model.modelica.ClassDefinition;
import crml.model.modelica.ClassKind;
import crml.model.modelica.ComponentDeclaration;
import crml.model.modelica.ComponentReference;
import crml.model.modelica.Equation;
import crml.model.modelica.Expression;
import crml.model.modelica.ExtendsClause;
import crml.model.modelica.FunctionCall;
import crml.model.modelica.IfExpression;
import crml.model.modelica.IntegerLiteral;
import crml.model.modelica.ModelicaElement;
import crml.model.modelica.ModificationElement;
import crml.model.modelica.ParenthesizedExpression;
import crml.model.modelica.Placeholder;
import crml.model.modelica.Reference;
import crml.model.modelica.RealLiteral;
import crml.model.modelica.ReferencePart;
import crml.model.modelica.Statement;
import crml.model.modelica.StringLiteral;
import crml.model.modelica.UnaryExpression;
import crml.model.modelica.UnaryOperatorKind;
import crml.model.modelica.Variability;
import crml.model.modelica.Visibility;

/**
 * Serializes the Modelica object model to {@code .mo} text.
 *
 * <p>Output depends only on the tree: no hash codes, no unordered iteration, no
 * timestamps, so printing the same tree twice is byte-identical.
 *
 * <p>Any node can be printed, not just a class: the trace report and the unit
 * tests both render single expressions.
 */
public final class ModelicaPrinter {

    private final IndentingWriter writer;

    private ModelicaPrinter(PrinterOptions options) {
        this.writer = new IndentingWriter(options);
    }

    public static String print(ModelicaElement node) {
        return print(node, PrinterOptions.DEFAULT);
    }

    public static String print(ModelicaElement node, PrinterOptions options) {
        ModelicaPrinter printer = new ModelicaPrinter(options);
        printer.printElement(node);
        return printer.writer.toString();
    }

    // --- dispatch -----------------------------------------------------------

    private void printElement(ModelicaElement node) {
        if (node == null) {
            return;
        }
        if (node instanceof ClassDefinition) {
            printClass((ClassDefinition) node);
        } else if (node instanceof ExtendsClause) {
            printExtends((ExtendsClause) node);
        } else if (node instanceof ComponentDeclaration) {
            printComponent((ComponentDeclaration) node);
        } else if (node instanceof Equation) {
            printEquation((Equation) node);
        } else if (node instanceof Statement) {
            printStatement((Statement) node);
        } else if (node instanceof Placeholder) {
            printPlaceholder((Placeholder) node);
        } else if (node instanceof Expression) {
            writer.append(expression((Expression) node));
        } else if (node instanceof ModificationElement) {
            writer.append(modification((ModificationElement) node));
        } else if (node instanceof ReferencePart) {
            writer.append(referencePart((ReferencePart) node));
        } else if (node instanceof ArrayDimension) {
            writer.append(arrayDimension((ArrayDimension) node));
        } else {
            throw new IllegalArgumentException(
                "No Modelica syntax for " + node.eClass().getName());
        }
    }

    // --- classes ------------------------------------------------------------

    private void printClass(ClassDefinition cls) {
        String name = Identifiers.quote(cls.getName());

        StringBuilder header = new StringBuilder();
        if (Boolean.TRUE.equals(cls.getPartial())) {
            header.append("partial ");
        }
        header.append(keyword(cls.getKind())).append(' ').append(name);
        if (cls.getComment() != null && !cls.getComment().isEmpty()) {
            header.append(" \"").append(Identifiers.escapeStringBody(cls.getComment())).append('"');
        }
        writer.line(header.toString());

        writer.indent();
        for (ExtendsClause clause : cls.getExtendsClauses()) {
            printExtends(clause);
        }
        for (ClassDefinition nested : cls.getNestedClasses()) {
            printClass(nested);
        }
        printComponents(cls.getComponents(), Visibility.PUBLIC, null);
        printComponents(cls.getComponents(), Visibility.PROTECTED, "protected");
        writer.outdent();

        printEquations(cls.getEquations());
        printAlgorithm(cls.getStatements());

        if (!cls.getPlaceholders().isEmpty()) {
            writer.indent();
            for (Placeholder placeholder : cls.getPlaceholders()) {
                printPlaceholder(placeholder);
            }
            writer.outdent();
        }

        writer.line("end " + name + ";");
    }

    private void printComponents(List<ComponentDeclaration> components, Visibility visibility, String sectionKeyword) {
        boolean sectionOpened = false;
        for (ComponentDeclaration component : components) {
            if (visibilityOf(component) != visibility) {
                continue;
            }
            if (sectionKeyword != null && !sectionOpened) {
                writer.outdent();
                writer.line(sectionKeyword);
                writer.indent();
                sectionOpened = true;
            }
            printComponent(component);
        }
    }

    private static Visibility visibilityOf(ComponentDeclaration component) {
        return component.getVisibility() == null ? Visibility.PUBLIC : component.getVisibility();
    }

    private static String keyword(ClassKind kind) {
        if (kind == null) {
            return "model";
        }
        switch (kind) {
            case MODEL: return "model";
            case RECORD: return "record";
            case BLOCK: return "block";
            case FUNCTION: return "function";
            case PACKAGE: return "package";
            default: throw new IllegalArgumentException("Unknown class kind " + kind);
        }
    }

    private void printExtends(ExtendsClause clause) {
        writer.line("extends " + name(clause, clause.getSuperClass()) + ";");
    }

    /**
     * The text for a reference, as written at {@code context}.
     *
     * <p>A raw reference prints its stored text verbatim - quoting it would
     * break {@code CRMLtoModelica.Functions.and4} and turn {@code der} into
     * {@code 'der'}. A resolved reference has its path recomputed from live
     * containment and quoted, so it cannot drift from the declaration it names
     * and cannot disagree with it about quoting.
     */
    private static String name(ModelicaElement context, Reference<?> reference) {
        if (reference == null) {
            return "";
        }
        if (reference.getTarget() == null) {
            return reference.getRawText() == null ? "" : reference.getRawText();
        }
        return Identifiers.quotePath(path(context, reference.getTarget()));
    }

    /**
     * The dotted path naming {@code target} from {@code context}: the names of
     * the elements from just below their nearest common container down to the
     * target. In this metamodel that is almost always a single segment, since
     * the tree is one model class with a flat layer of nested classes.
     */
    private static String path(ModelicaElement context, ModelicaElement target) {
        Set<EObject> contextChain = new HashSet<EObject>();
        for (EObject e = context; e != null; e = e.eContainer()) {
            contextChain.add(e);
        }
        List<String> segments = new ArrayList<String>();
        for (EObject e = target; e != null; e = e.eContainer()) {
            if (contextChain.contains(e)) {
                break;
            }
            String segment = nameOf(e);
            if (segment != null) {
                segments.add(0, segment);
            }
        }
        if (segments.isEmpty()) {
            String own = nameOf(target);
            return own == null ? "" : own;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            if (i > 0) {
                builder.append('.');
            }
            builder.append(segments.get(i));
        }
        return builder.toString();
    }

    private static String nameOf(EObject element) {
        if (element instanceof ClassDefinition) {
            return ((ClassDefinition) element).getName();
        }
        if (element instanceof ComponentDeclaration) {
            return ((ComponentDeclaration) element).getName();
        }
        return null;
    }

    private void printPlaceholder(Placeholder placeholder) {
        String message = placeholder.getMessage() == null ? "" : placeholder.getMessage();
        for (String line : message.split("\r\n|\r|\n", -1)) {
            writer.line("// " + line);
        }
    }

    // --- components ---------------------------------------------------------

    private void printComponent(ComponentDeclaration component) {
        StringBuilder builder = new StringBuilder();
        if (component.getVariability() == Variability.CONSTANT) {
            builder.append("constant ");
        }
        if (component.getCausality() == Causality.INPUT) {
            builder.append("input ");
        } else if (component.getCausality() == Causality.OUTPUT) {
            builder.append("output ");
        }
        builder.append(name(component, component.getDeclaredType()))
               .append(' ').append(Identifiers.quote(component.getName()));

        for (ArrayDimension dimension : component.getArrayDimensions()) {
            builder.append(arrayDimension(dimension));
        }
        if (!component.getModifications().isEmpty()) {
            builder.append('(');
            for (int i = 0; i < component.getModifications().size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(modification(component.getModifications().get(i)));
            }
            builder.append(')');
        }
        if (component.getBinding() != null) {
            builder.append(" = ").append(expression(component.getBinding()));
        }
        appendComment(builder, component.getComment());
        builder.append(';');
        writer.line(builder.toString());
    }

    private String arrayDimension(ArrayDimension dimension) {
        return "[" + (dimension.getSize() == null ? ":" : expression(dimension.getSize())) + "]";
    }

    private String modification(ModificationElement modification) {
        StringBuilder builder = new StringBuilder(Identifiers.quote(modification.getName()));
        if (modification.getValue() != null) {
            builder.append(" = ").append(expression(modification.getValue()));
        }
        return builder.toString();
    }

    // --- sections -----------------------------------------------------------

    private void printEquations(List<Equation> equations) {
        // No equations, no section: the keyword is only written when something
        // follows it.
        if (equations.isEmpty()) {
            return;
        }
        writer.line("equation");
        writer.indent();
        for (Equation equation : equations) {
            printEquation(equation);
        }
        writer.outdent();
    }

    private void printAlgorithm(List<Statement> statements) {
        if (statements.isEmpty()) {
            return;
        }
        writer.line("algorithm");
        writer.indent();
        for (Statement statement : statements) {
            printStatement(statement);
        }
        writer.outdent();
    }

    private void printEquation(Equation equation) {
        StringBuilder builder = new StringBuilder()
            .append(expression(equation.getLhs()))
            .append(" = ")
            .append(expression(equation.getRhs()));
        appendComment(builder, equation.getComment());
        builder.append(';');
        writer.line(builder.toString());
    }

    private void printStatement(Statement statement) {
        StringBuilder builder = new StringBuilder()
            .append(expression(statement.getTarget()))
            .append(" := ")
            .append(expression(statement.getValue()));
        appendComment(builder, statement.getComment());
        builder.append(';');
        writer.line(builder.toString());
    }

    /** Modelica's descriptive string sits before the terminating semicolon. */
    private static void appendComment(StringBuilder builder, String comment) {
        if (comment != null && !comment.isEmpty()) {
            builder.append(" \"").append(Identifiers.escapeStringBody(comment)).append('"');
        }
    }

    // --- expressions --------------------------------------------------------

    /** Renders an expression; the sole place parentheses are decided. */
    public String expression(Expression expression) {
        if (expression == null) {
            return "";
        }
        if (expression instanceof ComponentReference) {
            return componentReference((ComponentReference) expression);
        }
        if (expression instanceof BinaryExpression) {
            return binary((BinaryExpression) expression);
        }
        if (expression instanceof UnaryExpression) {
            return unary((UnaryExpression) expression);
        }
        if (expression instanceof FunctionCall) {
            return functionCall((FunctionCall) expression);
        }
        if (expression instanceof IfExpression) {
            return ifExpression((IfExpression) expression);
        }
        if (expression instanceof ArrayConstructor) {
            return arrayConstructor((ArrayConstructor) expression);
        }
        if (expression instanceof ParenthesizedExpression) {
            return "(" + expression(((ParenthesizedExpression) expression).getInner()) + ")";
        }
        if (expression instanceof IntegerLiteral) {
            Integer value = ((IntegerLiteral) expression).getValue();
            return value == null ? "0" : value.toString();
        }
        if (expression instanceof RealLiteral) {
            RealLiteral real = (RealLiteral) expression;
            if (real.getLiteral() != null && !real.getLiteral().isEmpty()) {
                return real.getLiteral();
            }
            return real.getValue() == null ? "0.0" : real.getValue().toString();
        }
        if (expression instanceof StringLiteral) {
            return "\"" + Identifiers.escapeStringBody(((StringLiteral) expression).getValue()) + "\"";
        }
        if (expression instanceof BooleanLiteral) {
            return Boolean.TRUE.equals(((BooleanLiteral) expression).getValue()) ? "true" : "false";
        }
        throw new IllegalArgumentException(
            "No Modelica syntax for expression kind " + expression.eClass().getName());
    }

    private String componentReference(ComponentReference reference) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < reference.getParts().size(); i++) {
            if (i > 0) {
                builder.append('.');
            }
            builder.append(referencePart(reference.getParts().get(i)));
        }
        return builder.toString();
    }

    private String referencePart(ReferencePart part) {
        StringBuilder builder = new StringBuilder(Identifiers.quote(part.getName()));
        if (!part.getSubscripts().isEmpty()) {
            builder.append('[');
            for (int i = 0; i < part.getSubscripts().size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(expression(part.getSubscripts().get(i)));
            }
            builder.append(']');
        }
        return builder.toString();
    }

    private String binary(BinaryExpression expression) {
        BinaryOperatorKind operator = expression.getOperator();
        String lhs = operand(expression.getLhs(), operator, true);
        String rhs = operand(expression.getRhs(), operator, false);
        return lhs + " " + symbol(operator) + " " + rhs;
    }

    private String operand(Expression operand, BinaryOperatorKind operator, boolean leftSide) {
        String text = expression(operand);
        return Precedence.needsParens(operator, operand, leftSide) ? "(" + text + ")" : text;
    }

    private static String symbol(BinaryOperatorKind operator) {
        if (operator == null) {
            throw new IllegalArgumentException("Binary expression has no operator");
        }
        switch (operator) {
            case ADD: return "+";
            case SUB: return "-";
            case MUL: return "*";
            case DIV: return "/";
            case POW: return "^";
            case AND: return "and";
            case OR: return "or";
            case EQ: return "==";
            case NEQ: return "<>";
            case LT: return "<";
            case LE: return "<=";
            case GT: return ">";
            case GE: return ">=";
            default: throw new IllegalArgumentException("Unknown binary operator " + operator);
        }
    }

    private String unary(UnaryExpression expression) {
        String symbol = expression.getOperator() == UnaryOperatorKind.MINUS ? "-" : "+";
        String operand = expression(expression.getOperand());
        if (Precedence.unaryOperandNeedsParens(expression.getOperand())) {
            operand = "(" + operand + ")";
        }
        return symbol + operand;
    }

    private String functionCall(FunctionCall call) {
        StringBuilder builder = new StringBuilder(name(call, call.getFunction())).append('(');
        for (int i = 0; i < call.getArguments().size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(expression(call.getArguments().get(i)));
        }
        return builder.append(')').toString();
    }

    private String ifExpression(IfExpression expression) {
        String condition = expression(expression.getCondition());
        if (Precedence.conditionNeedsParens(expression.getCondition())) {
            condition = "(" + condition + ")";
        }
        return "if " + condition
            + " then " + expression(expression.getThenExpression())
            + " else " + expression(expression.getElseExpression());
    }

    private String arrayConstructor(ArrayConstructor constructor) {
        StringBuilder builder = new StringBuilder("{");
        for (int i = 0; i < constructor.getElements().size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(expression(constructor.getElements().get(i)));
        }
        return builder.append('}').toString();
    }
}
