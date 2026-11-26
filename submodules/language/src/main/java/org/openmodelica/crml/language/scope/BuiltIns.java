package org.openmodelica.crml.language.scope;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.openmodelica.crml.model.metamodel.BuiltinBinaryOperatorKind;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.BuiltinUnaryOperatorKind;

/**
 * The built-in types and operators, as written in CRML. The grammar knows
 * them as keywords; the DOM builders map them with these tables, and
 * {@link Scopes#list} proposes them in {@link ScopeMode#CONTENT_ASSIST} mode.
 * They are never resolved by name.
 */
public final class BuiltIns {

    /** The built-in types by their CRML spelling. */
    public static final Map<String, BuiltinType> TYPES;
    /** The built-in binary operators by their CRML spelling. */
    public static final Map<String, BuiltinBinaryOperatorKind> BINARY_OPERATORS;
    /** The built-in unary operators by their CRML spelling. */
    public static final Map<String, BuiltinUnaryOperatorKind> UNARY_OPERATORS;

    static {
        Map<String, BuiltinType> types = new LinkedHashMap<>();
        types.put("String", BuiltinType.STRING);
        types.put("Boolean", BuiltinType.BOOLEAN);
        types.put("Integer", BuiltinType.INTEGER);
        types.put("Real", BuiltinType.REAL);
        types.put("Event", BuiltinType.EVENT);
        types.put("Clock", BuiltinType.CLOCK);
        types.put("Period", BuiltinType.PERIOD);
        types.put("Periods", BuiltinType.PERIODS);
        types.put("Requirement", BuiltinType.REQUIREMENT);
        TYPES = Collections.unmodifiableMap(types);

        Map<String, BuiltinBinaryOperatorKind> binary = new LinkedHashMap<>();
        binary.put("<", BuiltinBinaryOperatorKind.LT);
        binary.put("<=", BuiltinBinaryOperatorKind.LE);
        binary.put(">", BuiltinBinaryOperatorKind.GT);
        binary.put(">=", BuiltinBinaryOperatorKind.GE);
        binary.put("at", BuiltinBinaryOperatorKind.AT);
        binary.put("==", BuiltinBinaryOperatorKind.EQ);
        binary.put("<>", BuiltinBinaryOperatorKind.NEQ);
        binary.put("and", BuiltinBinaryOperatorKind.AND);
        binary.put("*", BuiltinBinaryOperatorKind.MUL);
        binary.put("/", BuiltinBinaryOperatorKind.DIV);
        binary.put("+", BuiltinBinaryOperatorKind.ADD);
        binary.put("-", BuiltinBinaryOperatorKind.SUB);
        binary.put("or", BuiltinBinaryOperatorKind.OR);
        binary.put("mod", BuiltinBinaryOperatorKind.MOD);
        binary.put("^", BuiltinBinaryOperatorKind.POW);
        binary.put("filter", BuiltinBinaryOperatorKind.FILTER);
        BINARY_OPERATORS = Collections.unmodifiableMap(binary);

        Map<String, BuiltinUnaryOperatorKind> unary = new LinkedHashMap<>();
        unary.put("sin", BuiltinUnaryOperatorKind.SIN);
        unary.put("asin", BuiltinUnaryOperatorKind.ASIN);
        unary.put("cos", BuiltinUnaryOperatorKind.COS);
        unary.put("acos", BuiltinUnaryOperatorKind.ACOS);
        unary.put("log10", BuiltinUnaryOperatorKind.LOG10);
        unary.put("log", BuiltinUnaryOperatorKind.LOG);
        unary.put("exp", BuiltinUnaryOperatorKind.EXP_OP);
        unary.put("not", BuiltinUnaryOperatorKind.NOT);
        unary.put("-", BuiltinUnaryOperatorKind.SUB);
        unary.put("+", BuiltinUnaryOperatorKind.ADD);
        unary.put("start", BuiltinUnaryOperatorKind.START);
        unary.put("end", BuiltinUnaryOperatorKind.END);
        unary.put("tick", BuiltinUnaryOperatorKind.TICK);
        unary.put("card", BuiltinUnaryOperatorKind.CARD);
        UNARY_OPERATORS = Collections.unmodifiableMap(unary);
    }

    private BuiltIns() {
    }

    /** The spellings of the built-in operators, binary first, each once. */
    public static Set<String> operatorNames() {
        Set<String> names = new LinkedHashSet<>(BINARY_OPERATORS.keySet());
        names.addAll(UNARY_OPERATORS.keySet());
        return Collections.unmodifiableSet(names);
    }
}
