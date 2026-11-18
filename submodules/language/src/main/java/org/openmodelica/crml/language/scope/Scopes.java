package org.openmodelica.crml.language.scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.openmodelica.crml.language.opcall.OperatorUtil;
import org.openmodelica.crml.model.base.MemberKind;
import org.openmodelica.crml.model.base.Namespace;
import org.openmodelica.crml.model.metamodel.CRMLPackage;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.NamedEntity;

/**
 * The scoping functions: {@link #list list} the declarations available at a
 * point, and {@link #resolve resolve} a name there, which filters that list.
 *
 * <p>Holds no state and caches nothing: the linker changes the DOM, so every
 * call computes its result from the current one. An instance class, so the
 * rules can be changed in a subclass or with another default strategy.
 */
public class Scopes {

    private final UpwardScopeStrategy upward;

    public Scopes() {
        this(new UpwardScopeStrategy());
    }

    /** @param upward the default strategy, used when none is given */
    public Scopes(UpwardScopeStrategy upward) {
        this.upward = upward;
    }

    /** The default strategy. */
    public UpwardScopeStrategy upward() {
        return upward;
    }

    // ---------------------------------------------------------------------
    // (1) list
    // ---------------------------------------------------------------------

    /** The declarations of kind {@code kind} available at {@code context}, nearest first. */
    public List<ScopeEntry> list(EObject context, EClass kind, ScopeMode mode) {
        return list(context, kind, mode, upward);
    }

    /** As {@link #list(EObject, EClass, ScopeMode)}, looked up with {@code strategy}. */
    public List<ScopeEntry> list(EObject context, EClass kind, ScopeMode mode, ScopeStrategy strategy) {
        return finish(strategy.entries(context), kind, mode);
    }

    /**
     * The declarations of kind {@code kind} available in {@code start}, its
     * own members included, and the namespaces around it.
     */
    public List<ScopeEntry> list(Namespace start, EClass kind, ScopeMode mode) {
        return finish(upward.entriesFrom(start, null), kind, mode);
    }

    /** Filters by kind and, for content assist, appends the built-ins. */
    private static List<ScopeEntry> finish(List<ScopeEntry> entries, EClass kind, ScopeMode mode) {
        List<ScopeEntry> result = new ArrayList<>();
        for (ScopeEntry entry : entries) {
            if (kind.isInstance(entry.element())) {
                result.add(entry);
            }
        }
        if (mode == ScopeMode.CONTENT_ASSIST) {
            if (kind.isSuperTypeOf(CRMLPackage.Literals.CLASS)) {
                for (String type : BuiltIns.TYPES.keySet()) {
                    result.add(ScopeEntry.builtIn(type, MemberKind.TYPE));
                }
            }
            if (kind.isSuperTypeOf(CRMLPackage.Literals.OPERATOR)) {
                for (String operator : BuiltIns.operatorNames()) {
                    result.add(ScopeEntry.builtIn(operator, MemberKind.OPERATOR));
                }
            }
        }
        return result;
    }

    // ---------------------------------------------------------------------
    // (2) resolve
    // ---------------------------------------------------------------------

    /** What {@code name} denotes at {@code context} among the declarations of kind {@code kind}. */
    public Resolution resolve(EObject context, EClass kind, String name) {
        return resolve(context, kind, name, upward);
    }

    /** As {@link #resolve(EObject, EClass, String)}, looked up with {@code strategy}. */
    public Resolution resolve(EObject context, EClass kind, String name, ScopeStrategy strategy) {
        List<ScopeEntry> matches = new ArrayList<>();
        for (ScopeEntry entry : list(context, kind, ScopeMode.RESOLUTION, strategy)) {
            if (Objects.equals(name, entry.name())) {
                matches.add(entry);
            }
        }
        if (matches.isEmpty()) {
            NamedEntity self = strategy.declaration(context);
            if (self != null && kind.isInstance(self) && Objects.equals(name, self.getName())) {
                return Resolution.selfReference(name);
            }
        }
        return Resolution.of(name, matches);
    }

    /** What {@code name} denotes for {@code host.reference}: the kind is the reference's type. */
    public Resolution resolve(EObject host, EReference reference, String name) {
        return resolve(host, reference.getEReferenceType(), name, upward);
    }

    /** As {@link #resolve(EObject, EReference, String)}, looked up with {@code strategy}. */
    public Resolution resolve(EObject host, EReference reference, String name, ScopeStrategy strategy) {
        return resolve(host, reference.getEReferenceType(), name, strategy);
    }

    // ---------------------------------------------------------------------
    // Operators for the mixfix parser
    // ---------------------------------------------------------------------

    /**
     * The operators visible in {@code start} that can be called in natural
     * syntax ({@link OperatorUtil#isMixfix}), own operators first, then the
     * inherited ones, then those of the namespaces around it.
     */
    public List<CustomOperator> operators(Namespace start) {
        List<CustomOperator> result = new ArrayList<>();
        for (ScopeEntry entry : list(start, CRMLPackage.Literals.CUSTOM_OPERATOR, ScopeMode.RESOLUTION)) {
            CustomOperator operator = (CustomOperator) entry.element();
            if (OperatorUtil.isMixfix(operator)) {
                result.add(operator);
            }
        }
        return result;
    }
}
