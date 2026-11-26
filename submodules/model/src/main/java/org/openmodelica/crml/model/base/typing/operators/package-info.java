/**
 * The typing rules of the built-in operators, on base types.
 *
 * <ul>
 * <li>This package: the rule interfaces, {@link org.openmodelica.crml.model.base.typing.operators.UnaryRule}
 *     and {@link org.openmodelica.crml.model.base.typing.operators.BinaryRule}, which
 *     {@code TypeInference} and the validator use.</li>
 * <li>{@code unary} and {@code binary}: the rule definitions, one class per
 *     group of operators, and {@code UnaryOperatorTyping.rule(kind)} and
 *     {@code BinaryOperatorTyping.rule(kind)}, which give the rule of an
 *     operator, or null where its typing is not defined yet.</li>
 * <li>{@code util}: the structures the definitions are written with: signature
 *     tables and the rules backed by one.</li>
 * </ul>
 *
 * <p>Dependencies go one way: {@code unary} and {@code binary} use this
 * package and {@code util}; {@code util} uses only the interfaces here.
 */
package org.openmodelica.crml.model.base.typing.operators;
