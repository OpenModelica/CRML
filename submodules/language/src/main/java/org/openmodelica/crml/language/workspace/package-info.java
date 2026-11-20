/**
 * Loading CRML models together with the libraries they depend on.
 *
 * <p>One parsed file is one {@link org.openmodelica.crml.language.workspace.CrmlResource}.
 * All files of a run live in one {@link org.eclipse.emf.ecore.resource.ResourceSet},
 * owned by a {@link org.openmodelica.crml.language.workspace.CrmlWorkspace}.
 * References between files are plain cross-resource references (no proxies):
 * a model refers into the library resources, nothing is copied.
 *
 * <p>Pipeline:
 * <ol>
 * <li><b>Per file</b> ({@link org.openmodelica.crml.language.workspace.CrmlResource#load}):
 *     parse, read the {@link org.openmodelica.crml.language.workspace.RootHeader header},
 *     build the DOM and its {@link org.openmodelica.crml.language.workspace.SourceMap source map}.
 *     No linking. A file with syntax or build errors gets a partial DOM: what
 *     could be built from the text the parser recovered.</li>
 * <li><b>Per run</b> ({@link org.openmodelica.crml.language.workspace.CrmlWorkspace#resolve()}):
 *     index the roots by declared name, bind the dependencies, order the roots
 *     dependencies-first, then link, resolve operator calls and lift, root by root.</li>
 * <li><b>Validation</b> ({@link org.openmodelica.crml.language.workspace.CrmlWorkspace#validate}):
 *     EMF validation plus the errors collected on the resources. Dependency
 *     problems (missing, cycle, library on a model, broken dependency) are
 *     validation errors, not exceptions.</li>
 * </ol>
 *
 * <p>Status: skeleton. Loading and indexing are wired up; dependency binding,
 * cross-resource scoping and operator-call resolution are TODO.
 */
package org.openmodelica.crml.language.workspace;
