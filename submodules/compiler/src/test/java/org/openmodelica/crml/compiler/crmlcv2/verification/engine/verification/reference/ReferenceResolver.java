package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.reference;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Binding;

/**
 * Finds, in a reference, the counterpart of a signal the compiler generates.
 *
 * <p>The compiled model is not searched: what it generates is fixed (see
 * {@code Contract}). The reference is a hand-written model, built on another
 * library, so its names vary. In order, the first that answers wins:
 * <ol>
 * <li>the reference's alias file ({@link Aliases}): for a signal that no name
 *     ties to the generated one, such as the activation count of a synchronous
 *     clock, which the reference records only as {@code _Clocks.BaseClock_<n>};</li>
 * <li>the same name: the reference records the signal as the compiler does;</li>
 * <li>the conventions of the CRML library ({@link Conventions}): a member of the
 *     output's own component, such as a time locator's {@code P.timePeriod}.</li>
 * </ol>
 */
public final class ReferenceResolver {

    private final Set<String> names;
    private final Aliases aliases;
    private final Path aliasFile;

    private ReferenceResolver(Set<String> names, Path aliasFile) {
        this.names = names;
        this.aliases = Aliases.load(aliasFile);
        this.aliasFile = aliasFile;
    }

    /**
     * A resolver for the reference {@code reference}, which records {@code names}.
     * Its alias file is next to it, with the extension {@code .aliases} instead of {@code .mat}.
     */
    public static ReferenceResolver of(Path reference, Set<String> names) {
        return new ReferenceResolver(names, aliasFile(reference));
    }

    /** Where the alias file of {@code reference} is: {@code X_ref.mat} has {@code X_ref.aliases}. */
    public static Path aliasFile(Path reference) {
        String name = reference.getFileName().toString();
        String base = name.endsWith(".mat") ? name.substring(0, name.length() - ".mat".length()) : name;
        return reference.resolveSibling(base + ".aliases");
    }

    /**
     * The reference's counterpart of {@code generated}, a signal of output {@code output},
     * or null if none. Throws {@link IllegalArgumentException} if its alias is wrong.
     */
    public Binding locate(String output, Binding generated) {
        if (aliases.has(generated)) {
            // An alias is not second-guessed: a wrong one throws, it is not a fallback.
            return aliases.locate(generated, names);
        }
        if (names.contains(generated.signal())) {
            return Binding.reference(generated.signal(), generated.encoding(), "same name");
        }
        return Conventions.locate(output, generated, names);
    }

    /** Why {@link #locate} found nothing for {@code generated}. */
    public String notFound(String output, Binding generated) {
        List<String> candidates = new ArrayList<>();
        candidates.add(generated.signal());
        candidates.addAll(Conventions.lookingFor(output, generated));
        return (candidates.size() == 1
            ? "the reference does not record " + candidates.get(0)
            : "the reference records neither " + String.join(" nor ", candidates))
            + "; if it records it under another name, give an alias in " + aliasFile.getFileName();
    }

    /**
     * Throws if the alias file has an alias that was never used: the compiled
     * model does not generate its signal. Call it after every output was located.
     */
    public void checkAllAliasesUsed() {
        aliases.checkAllUsed();
    }
}
