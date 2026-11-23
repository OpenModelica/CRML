package org.openmodelica.crml.model.base.typing.operators.binary;

import static org.openmodelica.crml.model.base.typing.operators.util.SignatureTable.oneOf;
import static org.openmodelica.crml.model.metamodel.BuiltinType.BOOLEAN;
import static org.openmodelica.crml.model.metamodel.BuiltinType.CLOCK;
import static org.openmodelica.crml.model.metamodel.BuiltinType.INTEGER;
import static org.openmodelica.crml.model.metamodel.BuiltinType.PERIOD;
import static org.openmodelica.crml.model.metamodel.BuiltinType.REAL;
import static org.openmodelica.crml.model.metamodel.BuiltinType.STRING;

import org.openmodelica.crml.model.base.typing.operators.util.SignatureTable;
import org.openmodelica.crml.model.base.typing.operators.util.TableBinaryRule;

/**
 * {@code +}, {@code -}, {@code *}, {@code /}, {@code mod} and {@code ^}.
 *
 * <p>On numbers they follow Modelica: {@code +}, {@code -}, {@code *} and
 * {@code mod} give an Integer for two Integers and a Real otherwise, while
 * {@code /} and {@code ^} always give a Real ({@code 7 / 2} is {@code 3.5}).
 * In addition, {@code +}, {@code -} and {@code *} work on two Booleans
 * (four-valued logic), {@code +} joins a String with a String or a number,
 * and {@code +} shifts a Clock or a Period by a time.
 */
public final class ArithmeticRule extends TableBinaryRule {

    public static final ArithmeticRule ADD = new ArithmeticRule(integral()
        .row(BOOLEAN, oneOf(BOOLEAN), oneOf(BOOLEAN))
        .row(STRING, oneOf(STRING), oneOf(INTEGER, REAL, STRING))
        .row(STRING, oneOf(INTEGER, REAL, STRING), oneOf(STRING))
        .row(CLOCK, oneOf(PERIOD, CLOCK), oneOf(REAL))
        .row(CLOCK, oneOf(CLOCK), oneOf(INTEGER)));

    public static final ArithmeticRule SUB = new ArithmeticRule(integral()
        .row(BOOLEAN, oneOf(BOOLEAN), oneOf(BOOLEAN)));

    public static final ArithmeticRule MUL = new ArithmeticRule(integral()
        .row(BOOLEAN, oneOf(BOOLEAN), oneOf(BOOLEAN)));

    public static final ArithmeticRule MOD = new ArithmeticRule(integral());

    public static final ArithmeticRule DIV = new ArithmeticRule(real());

    public static final ArithmeticRule POW = new ArithmeticRule(real());

    private ArithmeticRule(SignatureTable.Builder table) {
        super(table.build());
    }

    /** Integer with Integer gives an Integer, and Real with any number a Real. */
    private static SignatureTable.Builder integral() {
        return SignatureTable.builder(2)
            .row(INTEGER, oneOf(INTEGER), oneOf(INTEGER))
            .row(REAL, oneOf(REAL), oneOf(INTEGER, REAL))
            .row(REAL, oneOf(INTEGER), oneOf(REAL));
    }

    /** Any two numbers give a Real, as Modelica's {@code /} and {@code ^} do. */
    private static SignatureTable.Builder real() {
        return SignatureTable.builder(2)
            .row(REAL, oneOf(INTEGER, REAL), oneOf(INTEGER, REAL));
    }
}
