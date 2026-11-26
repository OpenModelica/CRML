package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Binding;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Encoding;

/**
 * The known ways the reference models (the CRML library, simulated in Dymola)
 * record what the compiler generates under another name.
 *
 * <p>A rule may only match a signal rooted at the output's own component in the
 * reference, {@code <output>.<member>}, where the member's meaning is fixed by
 * that component's type in the CRML library (a record field, a block's output).
 * The one assumption all rules share is that the component named after a CRML
 * variable is that variable. Anything else is an alias, written down with the
 * reason it is right: a name that only contains the output's name (a
 * {@code show_<c>} block), a name the simulator makes up
 * ({@code _Clocks.BaseClock_<n>}), or a value computed further from the
 * component named after the variable.
 *
 * <p>To add a rule, add one entry to {@link #RULES}.
 */
final class Conventions {

    /** One known way the reference records a generated signal: a member of the output's component. */
    private static final class Rule {
        final String description;
        final Encoding encoding;
        /** (output, generated signal) to the signal to look for, or null if the rule does not apply. */
        final BiFunction<String, Binding, String> candidate;

        Rule(String description, Encoding encoding, BiFunction<String, Binding, String> candidate) {
            this.description = description;
            this.encoding = encoding;
            this.candidate = candidate;
        }
    }

    private static final List<Rule> RULES = Arrays.asList(

        // A Boolean given by a block of the CRML library (BooleanTable, BooleanConstant):
        // the value is the block's output y, a plain Boolean. Where the reference builds
        // the value further from that block (phi1 in DecideOver_no_ext: the table's y
        // and-ed with undecided), its alias file points at the value instead.
        new Rule("block output", Encoding.BOOLEAN,
            (output, generated) -> generated.encoding() == Encoding.BOOLEAN4 ? generated.signal() + ".y" : null),

        // A period given as a time locator (TimeLocatorConnector): its open flag is timePeriod.
        new Rule("time locator", Encoding.VALUE,
            (output, generated) -> generated.signal().equals(output + ".is_open") ? output + ".timePeriod" : null)
    );

    private Conventions() {
    }

    /** The first rule's counterpart of {@code generated} in {@code names}, or null. */
    static Binding locate(String output, Binding generated, Set<String> names) {
        for (Rule rule : RULES) {
            String candidate = rule.candidate.apply(output, generated);
            if (candidate != null && names.contains(candidate)) {
                if (rule.encoding.form() != generated.encoding().form()) {
                    throw new IllegalStateException("rule '" + rule.description + "' gives " + rule.encoding
                        + " for " + generated + ": not comparable");
                }
                return Binding.reference(candidate, rule.encoding, "rule: " + rule.description);
            }
        }
        return null;
    }

    /** What the rules look for, for a message when nothing is found. */
    static List<String> lookingFor(String output, Binding generated) {
        List<String> candidates = new ArrayList<>();
        for (Rule rule : RULES) {
            String candidate = rule.candidate.apply(output, generated);
            if (candidate != null) {
                candidates.add(candidate);
            }
        }
        return candidates;
    }
}
