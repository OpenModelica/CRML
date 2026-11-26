package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.reference;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Binding;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Encoding;

/**
 * The alias file of one reference: where it records a generated signal that no
 * name ties to it. One line per signal, {@code #} starts a comment:
 * <pre>
 * # generated signal   reference signal                                  encoding
 * c1.out               _Clocks.BaseClock_0.SubClock_1.activationCount    COUNTER
 * </pre>
 * The generated signal is the name in the compiled model's result, as the
 * "compared as" column of the case's summary.txt shows it; the encoding is one
 * of {@link Encoding}, and must decode into the same form as the generated one.
 *
 * <p>A wrong alias file throws an {@link IllegalArgumentException} naming the
 * file and line: a line that does not parse, an unknown encoding, a second alias
 * for a signal, a reference signal the reference does not record, an encoding not
 * comparable with the generated one, and an alias for a signal the compiled model
 * does not generate.
 */
final class Aliases {

    private static final class Alias {
        final String target;
        final Encoding encoding;
        final int line;

        Alias(String target, Encoding encoding, int line) {
            this.target = target;
            this.encoding = encoding;
            this.line = line;
        }
    }

    private final Path file;
    private final Map<String, Alias> aliases = new LinkedHashMap<>();
    private final Set<String> used = new LinkedHashSet<>();

    private Aliases(Path file) {
        this.file = file;
    }

    /** The aliases in {@code file}; none if there is no such file. */
    static Aliases load(Path file) {
        Aliases aliases = new Aliases(file);
        if (!Files.exists(file)) {
            return aliases;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + file, e);
        }
        for (int i = 0; i < lines.size(); i++) {
            aliases.parse(lines.get(i), i + 1);
        }
        return aliases;
    }

    private void parse(String text, int line) {
        int comment = text.indexOf('#');
        String content = (comment < 0 ? text : text.substring(0, comment)).trim();
        if (content.isEmpty()) {
            return;
        }
        String[] fields = content.split("\\s+");
        if (fields.length != 3) {
            throw wrong(line, "expected '<generated signal> <reference signal> <encoding>', got '" + content + "'");
        }
        Encoding encoding;
        try {
            encoding = Encoding.valueOf(fields[2]);
        } catch (IllegalArgumentException e) {
            throw wrong(line, "unknown encoding " + fields[2] + ", expected one of " + Arrays.toString(Encoding.values()));
        }
        if (aliases.containsKey(fields[0])) {
            throw wrong(line, "a second alias for " + fields[0]);
        }
        aliases.put(fields[0], new Alias(fields[1], encoding, line));
    }

    /** True if there is an alias for {@code generated}. */
    boolean has(Binding generated) {
        return aliases.containsKey(generated.signal());
    }

    /** The aliased counterpart of {@code generated}, which must have an alias ({@link #has}). */
    Binding locate(Binding generated, Set<String> names) {
        Alias alias = aliases.get(generated.signal());
        used.add(generated.signal());
        if (!names.contains(alias.target)) {
            throw wrong(alias.line, "the reference does not record " + alias.target);
        }
        if (alias.encoding.form() != generated.encoding().form()) {
            throw wrong(alias.line, alias.encoding + " is not comparable with " + generated.encoding()
                + " (" + generated.signal() + ")");
        }
        return Binding.reference(alias.target, alias.encoding, "alias");
    }

    /** Throws if an alias was never used: the compiled model does not generate its signal. */
    void checkAllUsed() {
        for (Map.Entry<String, Alias> alias : aliases.entrySet()) {
            if (!used.contains(alias.getKey())) {
                throw wrong(alias.getValue().line, "the compiled model does not generate " + alias.getKey());
            }
        }
    }

    private IllegalArgumentException wrong(int line, String problem) {
        return new IllegalArgumentException(file.getFileName() + ", line " + line + ": " + problem);
    }
}
