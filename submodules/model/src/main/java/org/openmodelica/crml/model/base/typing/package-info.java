/**
 * Type inference for CRML values: {@code Value.inferType()},
 * {@code Value.inferSize()}, {@code TypeReference.toType()} and
 * {@code Variable.declaredType()} of {@code crml.xcore} call
 * {@link org.openmodelica.crml.model.base.typing.TypeInference} and
 * {@link org.openmodelica.crml.model.base.typing.SizeInference}.
 *
 * <p>A type is a {@link org.openmodelica.crml.model.base.typing.ValueType}: a
 * {@link org.openmodelica.crml.model.base.typing.BaseType} and a
 * {@link org.openmodelica.crml.model.base.typing.Multiplicity}, or
 * {@code UNKNOWN}. It is plain Java, not an EMF object, so it never takes part
 * in containment. The size of a set is not part of its type.
 *
 * <p>Inference is pure: it computes from the current model on every call, and
 * reports nothing. Errors and incompatibilities are the validator's, which can
 * reuse the operator rules of
 * {@link org.openmodelica.crml.model.base.typing.operators}.
 */
package org.openmodelica.crml.model.base.typing;
