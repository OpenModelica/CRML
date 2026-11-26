package org.openmodelica.crml.model.base.typing.operators.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmodelica.crml.model.base.typing.BaseType;
import org.openmodelica.crml.model.metamodel.BuiltinType;

/**
 * The typing rules of an operator as a table: each row maps the base types of
 * the operands to the base type of the result.
 *
 * <p>A row is looked up by exact match, so a table must list every accepted
 * combination; {@link Builder#row(BuiltinType, BuiltinType[]...)} takes a set
 * of alternatives per operand to keep that short. Rows never overlap: two
 * rows for the same operands are a mistake in the table and fail when it is
 * built, unless they agree on the result.
 *
 * <p>The validator can reuse a table through {@link #accepts(BaseType...)} and
 * {@link #getSignatures()}.
 */
public final class SignatureTable {

    private final int arity;
    private final Map<List<BaseType>, Signature> rows;

    private SignatureTable(int arity, Map<List<BaseType>, Signature> rows) {
        this.arity = arity;
        this.rows = rows;
    }

    /** A builder of a table for operators of the given number of operands. */
    public static Builder builder(int arity) {
        return new Builder(arity);
    }

    /** Alternatives for one operand of a {@link Builder#row row}: {@code oneOf(INTEGER, REAL)}. */
    public static BuiltinType[] oneOf(BuiltinType... types) {
        return types;
    }

    public int getArity() {
        return arity;
    }

    /** The result base type for the given operand base types, or null if no row matches. */
    public BaseType result(BaseType... operands) {
        if (operands.length != arity) {
            return null;
        }
        Signature signature = rows.get(Arrays.asList(operands));
        return signature == null ? null : signature.getResult();
    }

    /** True if a row matches the given operand base types. */
    public boolean accepts(BaseType... operands) {
        return result(operands) != null;
    }

    /** All rows, in the order they were added. */
    public List<Signature> getSignatures() {
        return Collections.unmodifiableList(new ArrayList<>(rows.values()));
    }

    /** Builds a {@link SignatureTable}. */
    public static final class Builder {
        private final int arity;
        private final Map<List<BaseType>, Signature> rows = new LinkedHashMap<>();

        private Builder(int arity) {
            this.arity = arity;
        }

        /**
         * Adds one row for every combination of the alternatives, one array
         * of alternatives per operand.
         */
        public Builder row(BuiltinType result, BuiltinType[]... operands) {
            if (operands.length != arity) {
                throw new IllegalArgumentException("expected " + arity + " operands, got " + operands.length);
            }
            expand(BaseType.of(result), operands, 0, new ArrayList<BaseType>());
            return this;
        }

        private void expand(BaseType result, BuiltinType[][] operands, int index, List<BaseType> prefix) {
            if (index == operands.length) {
                add(new Signature(prefix, result));
                return;
            }
            for (BuiltinType alternative : operands[index]) {
                List<BaseType> next = new ArrayList<>(prefix);
                next.add(BaseType.of(alternative));
                expand(result, operands, index + 1, next);
            }
        }

        private void add(Signature signature) {
            Signature existing = rows.get(signature.getParameters());
            if (existing != null && !existing.getResult().equals(signature.getResult())) {
                throw new IllegalStateException("conflicting rows " + existing + " and " + signature);
            }
            if (existing == null) {
                rows.put(signature.getParameters(), signature);
            }
        }

        public SignatureTable build() {
            return new SignatureTable(arity, new LinkedHashMap<>(rows));
        }
    }
}
