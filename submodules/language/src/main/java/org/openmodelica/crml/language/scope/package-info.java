/**
 * Scoping: which declarations are available at a point of the DOM
 * ({@link org.openmodelica.crml.language.scope.Scopes#list list}, for content
 * assist) and which one a name denotes there
 * ({@link org.openmodelica.crml.language.scope.Scopes#resolve resolve}, for the
 * linker). Resolution is built on the list.
 *
 * <p>The structure comes from the metamodel: every element knows its
 * {@link org.openmodelica.crml.model.base.Namespace namespace}, and a
 * namespace knows its owned and inherited members. The rules (nearest level
 * first, hiding, ambiguity, self-reference, built-ins) are here. See
 * {@code SCOPING_SPEC.md} in this module.
 */
package org.openmodelica.crml.language.scope;
