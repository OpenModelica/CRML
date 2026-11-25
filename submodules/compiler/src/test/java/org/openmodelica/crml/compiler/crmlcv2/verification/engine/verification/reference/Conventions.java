package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Binding;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Encoding;

/**
 * The known ways the reference models (the CRML library, simulated in Dymola)
 * record what the compiler generates under another name. Each rule follows from
 * how the CRML library builds a value, so it holds for every case; a mapping that
 * only one reference needs belongs in that reference's alias file instead.
 *
 * <p>To add a rule, add one entry to {@link #RULES}.
 */
final class Conventions {

    /** One known way the reference records a generated signal. */
    private abstract static class Rule {
        final String description;

        Rule(String description) {
            this.description = description;
        }

        /** The reference's counterpart of {@code generated}, or null if this rule finds none. */
        abstract Binding locate(String output, Binding generated, Set<String> names);

        /** What this rule looks for, for a message when nothing is found, or null if it does not apply. */
        abstract String lookingFor(String output, Binding generated);
    }

    /** A rule that looks for one signal, named after the output. */
    private abstract static class Named extends Rule {
        private final Encoding encoding;

        Named(String description, Encoding encoding) {
            super(description);
            this.encoding = encoding;
        }

        /** The signal to look for, or null if the rule does not apply to {@code generated}. */
        abstract String candidate(String output, Binding generated);

        @Override
        Binding locate(String output, Binding generated, Set<String> names) {
            String candidate = candidate(output, generated);
            return candidate != null && names.contains(candidate)
                ? Binding.reference(candidate, encoding, "rule: " + description) : null;
        }

        @Override
        String lookingFor(String output, Binding generated) {
            return candidate(output, generated);
        }
    }

    private static final List<Rule> RULES = Arrays.<Rule>asList(

        // A Boolean given by a block of the CRML library (BooleanTable, BooleanConstant):
        // the value is the block's output y, a plain Boolean. Where the reference builds
        // the value further from that block (phi1 in DecideOver_no_ext: the table's y
        // and-ed with undecided), its alias file points at the value instead.
        new Named("block output", Encoding.BOOLEAN) {
            @Override
            String candidate(String output, Binding generated) {
                return generated.encoding() == Encoding.BOOLEAN4 ? generated.signal() + ".y" : null;
            }
        },

        // A period built by a time locator: its open flag is timePeriod.
        new Named("time locator", Encoding.VALUE) {
            @Override
            String candidate(String output, Binding generated) {
                return generated.signal().equals(output + ".is_open") ? output + ".timePeriod" : null;
            }
        },

        // A clock kept as a synchronous Modelica Clock: its ticks are the activation
        // count of the partition that holds the harness's show_<c> or showEvent_<c> block.
        new Rule("showEvent_ block") {
            @Override
            Binding locate(String output, Binding generated, Set<String> names) {
                if (generated.encoding() != Encoding.PULSES) {
                    return null;
                }
                Pattern show = Pattern.compile("^(_Clocks\\.BaseClock_\\d+\\.SubClock_\\d+)\\.(?:show|showEvent)_"
                    + Pattern.quote(output) + "\\.y$");
                for (String name : names) {
                    Matcher matcher = show.matcher(name);
                    if (matcher.matches() && names.contains(matcher.group(1) + ".activationCount")) {
                        return Binding.reference(matcher.group(1) + ".activationCount", Encoding.COUNTER,
                            "rule: " + description);
                    }
                }
                return null;
            }

            @Override
            String lookingFor(String output, Binding generated) {
                return generated.encoding() == Encoding.PULSES ? "a show_" + output + " or showEvent_" + output + " block" : null;
            }
        }
    );

    private Conventions() {
    }

    /** The first rule's counterpart of {@code generated} in {@code names}, or null. */
    static Binding locate(String output, Binding generated, Set<String> names) {
        for (Rule rule : RULES) {
            Binding found = rule.locate(output, generated, names);
            if (found != null) {
                if (found.encoding().form() != generated.encoding().form()) {
                    throw new IllegalStateException("rule '" + rule.description + "' gives " + found.encoding()
                        + " for " + generated + ": not comparable");
                }
                return found;
            }
        }
        return null;
    }

    /** What the rules look for, for a message when nothing is found. */
    static List<String> lookingFor(String output, Binding generated) {
        List<String> candidates = new ArrayList<>();
        for (Rule rule : RULES) {
            String candidate = rule.lookingFor(output, generated);
            if (candidate != null) {
                candidates.add(candidate);
            }
        }
        return candidates;
    }
}
