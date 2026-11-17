/**
 * Hand-written base types of the CRML metamodel.
 *
 * <p>The generated classes extend {@link org.openmodelica.crml.model.base.CrmlObjectImpl}
 * and their interfaces {@link org.openmodelica.crml.model.base.CrmlObject} (set
 * by the {@code rootExtendsClass} and {@code rootExtendsInterface} options of
 * {@code crml.xcore}), so every CRML object knows its
 * {@link org.openmodelica.crml.model.base.Namespace namespace}.
 *
 * <p>Design pattern: behaviour declared in {@code crml.xcore} is a one-line
 * call to a static method of {@link org.openmodelica.crml.model.base.NamespaceSupport}
 * (or another helper here). The logic is plain Java 8, so it can be edited,
 * tested and debugged without Xcore tooling.
 */
package org.openmodelica.crml.model.base;
