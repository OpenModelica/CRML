package org.openmodelica.crml.model.base;

import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Keyword;
import org.openmodelica.crml.model.metamodel.OperatorHeaderElement;

/**
 * The implementation of the operator operations of {@code crml.xcore} that
 * are more than a line, which call these methods.
 */
public final class OperatorSupport {

    private OperatorSupport() {
    }

    /**
     * See {@link CustomOperator#getSignature()}: the header with each
     * keyword as written and each parameter as {@code _}, separated by
     * spaces, e.g. {@code _ 'and' _}.
     */
    // TODO: operator overloading. The CRML specification identifies an
    // operator by its typed signature (return type, words and parameter
    // types: T$w1$T1$w2$T2), so operators with the same words and different
    // parameter types are overloads. This signature leaves the types out, so
    // such operators are taken for the same one. Overloading needs type
    // inference to pick the overload at a call; until then it is not
    // supported.
    public static String signature(CustomOperator operator) {
        StringBuilder signature = new StringBuilder();
        for (OperatorHeaderElement element : operator.getHeader()) {
            if (signature.length() > 0) {
                signature.append(' ');
            }
            signature.append(element instanceof Keyword ? ((Keyword) element).getKeyword() : "_");
        }
        return signature.toString();
    }
}
