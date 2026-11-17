package org.openmodelica.crml.language.workspace;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.openmodelica.crml.language.dom.DomModification;
import org.openmodelica.crml.language.dom.UnresolvedLink;
import org.openmodelica.crml.language.dom.builders.LiftBuilder;
import org.openmodelica.crml.language.opcall.MixfixParser;
import org.openmodelica.crml.language.validation.CRMLValidator;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic.Kind;
import org.openmodelica.crml.language.workspace.StageResult.CallResult;
import org.openmodelica.crml.language.workspace.StageResult.LiftResult;
import org.openmodelica.crml.language.workspace.StageResult.LinkResult;
import org.openmodelica.crml.model.metamodel.Liftable;
import org.openmodelica.crml.model.metamodel.Sequence;

/**
 * All CRML files of one run (one CLI invocation, one test case, one editor
 * workspace) in one {@link ResourceSet}, one {@link CrmlResource} per file.
 * A library is loaded once per workspace, however many models use it.
 *
 * <pre>{@code
 * CrmlWorkspace ws = new CrmlWorkspace(Collections.singletonList(libraryDir));
 * ws.loadLibraryPath();
 * CrmlResource model = ws.load(modelFile);
 * ws.resolve();
 * Diagnostic d = ws.validate(model);
 * }</pre>
 *
 * <p>Wraps a plain {@link ResourceSetImpl} instead of extending it. The
 * per-run state other code needs (the {@link RootIndex}) is attached to the
 * resource set as an adapter.
 */
public class CrmlWorkspace {

    private final ResourceSet resourceSet = new ResourceSetImpl();
    private final RootIndex index = new RootIndex();
    private final List<Path> libraryPath;
    private int inlineCount;

    /**
     * @param libraryPath folders searched for libraries; every {@code .crml}
     *        file in them is loaded by {@link #loadLibraryPath()}
     */
    public CrmlWorkspace(List<Path> libraryPath) {
        this.libraryPath = Collections.unmodifiableList(new ArrayList<>(libraryPath));
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
            .put(CrmlResourceFactory.EXTENSION, new CrmlResourceFactory());
        resourceSet.eAdapters().add(index);
    }

    public CrmlWorkspace() {
        this(Collections.<Path>emptyList());
    }

    /** The resource set holding every file of the run. */
    public ResourceSet getResourceSet() { return resourceSet; }

    /** The roots of the run by declared name. */
    public RootIndex getIndex() { return index; }

    public List<Path> getLibraryPath() { return libraryPath; }

    /**
     * Loads every {@code .crml} file of the library path (not recursive),
     * in file-name order, whether or not a model uses it: libraries are found
     * by their declared name, which is only known once a file is parsed. A
     * file that does not parse is loaded too, with its errors, so a dependent
     * can be told the dependency is broken.
     */
    public List<CrmlResource> loadLibraryPath() throws IOException {
        List<CrmlResource> loaded = new ArrayList<>();
        for (Path folder : libraryPath) {
            List<Path> files;
            try (Stream<Path> list = Files.list(folder)) {
                files = list.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith("." + CrmlResourceFactory.EXTENSION))
                    .sorted()
                    .collect(Collectors.toList());
            }
            for (Path file : files) {
                loaded.add(load(file));
            }
        }
        return loaded;
    }

    /**
     * Loads one file: parses it and builds its DOM, without linking. Loading
     * the same file twice returns the same resource. The file is read as
     * CRML whatever its extension. Its diagnostics name it by {@code file}
     * as given, its URI is absolute.
     *
     * @throws IOException if the file cannot be read
     */
    public CrmlResource load(Path file) throws IOException {
        CrmlResource resource = resourceFor(uri(file));
        if (!resource.isLoaded()) {
            // Diagnostics name the file as the caller did, e.g. relative to
            // the working directory on the command line.
            resource.setLocation(file.toString());
            // Read through the Path, not the URI: the file may be in a jar
            // (test resources), which EMF's URI converter cannot open.
            try (InputStream in = Files.newInputStream(file)) {
                resource.load(in, null);
            } catch (IOException e) {
                resourceSet.getResources().remove(resource);
                throw e;
            }
        }
        index.add(resource);
        return resource;
    }

    /**
     * Loads CRML text that is not read from a file, e.g. an editor buffer or
     * a test model. If a resource with this URI is already loaded, it is
     * unloaded and loaded again from {@code content}; objects of other
     * resources that refer into it then point to proxies, so its dependents
     * have to be loaded again as well.
     *
     * @param uri identifies the text in diagnostics; it is not read
     */
    public CrmlResource load(URI uri, String content) {
        CrmlResource resource = resourceFor(uri);
        if (resource.isLoaded()) {
            index.remove(resource);
            resource.unload();
        }
        try {
            resource.load(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), null);
        } catch (IOException e) {
            // Not expected when reading from memory.
            throw new UncheckedIOException(e);
        }
        index.add(resource);
        return resource;
    }

    /**
     * Loads CRML text under a fresh {@code inline:} URI, see
     * {@link #load(URI, String)}.
     */
    public CrmlResource load(String content) {
        return load(URI.createURI("inline:/text" + (++inlineCount) + "." + CrmlResourceFactory.EXTENSION), content);
    }

    /** A file URI for a file of the default file system, the Path's own URI otherwise (e.g. in a jar). */
    private static URI uri(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        if (normalized.getFileSystem() == FileSystems.getDefault()) {
            return URI.createFileURI(normalized.toString());
        }
        return URI.createURI(normalized.toUri().toString());
    }

    /**
     * The resource for {@code uri}: the one in the resource set, or a new,
     * unloaded one. Looked up by hand rather than with
     * {@link ResourceSet#getResource}, which normalizes the URI with EMF's URI
     * converter; that needs the Eclipse platform, which the web build (TeaVM)
     * does not have. The workspace makes its URIs itself, so they need no
     * normalizing.
     */
    private CrmlResource resourceFor(URI uri) {
        Resource existing = null;
        for (Resource resource : resourceSet.getResources()) {
            if (uri.equals(resource.getURI())) {
                existing = resource;
                break;
            }
        }
        if (existing instanceof CrmlResource) {
            return (CrmlResource) existing;
        }
        if (existing != null) {
            throw new IllegalStateException(uri + " is already loaded as a " + existing.getClass().getName());
        }
        CrmlResource resource = new CrmlResource(uri);
        resourceSet.getResources().add(resource);
        return resource;
    }

    /** All CRML resources of the run, in load order. */
    public List<CrmlResource> resources() {
        List<CrmlResource> result = new ArrayList<>();
        for (Resource resource : resourceSet.getResources()) {
            if (resource instanceof CrmlResource) {
                result.add((CrmlResource) resource);
            }
        }
        return result;
    }

    /**
     * Links every resource that is {@link CrmlResource.State#BUILT built} but
     * not resolved yet, in load order. Resolved resources are skipped, so a
     * library shared by several models is resolved once.
     *
     * <p>Per resource, the stages are {@link #link}, {@link #operatorParser},
     * {@link #resolveOperatorCalls} and {@link #lift}. A stage that fails
     * reports its problems as diagnostics of the resource (no exception), and
     * the next stages still run ("load as much as possible").
     *
     * <p>For now each resource is linked on its own, as before: references
     * into other resources are not resolved. Steps still to do (TODO):
     * <ol>
     * <li>bind each {@code Dependency} of each root to the root found by
     *     {@link RootIndex#lookup}; leave it unbound when missing, broken or
     *     duplicate (a validation rule reports it);</li>
     * <li>order the roots dependencies-first (post-order over the dependency
     *     graph, cutting cycles);</li>
     * <li>link with a scope of the root's own members followed by those of
     *     its transitive dependencies, never the whole resource set;</li>
     * <li>resolve operator calls with the operators of the transitive
     *     dependencies (dependencies first) followed by the root's own.</li>
     * </ol>
     */
    public void resolve() {
        for (CrmlResource resource : resources()) {
            if (resource.getState() == CrmlResource.State.BUILT) {
                resolve(resource);
            }
        }
    }

    /**
     * Runs the stages on one resource. A stage method that returns null stops
     * here and leaves the resource {@link CrmlResource.State#BUILT built}, so
     * a subclass in a test can stop after a given stage. A later
     * {@link #resolve()} then runs all stages again.
     */
    private void resolve(CrmlResource resource) {
        if (link(resource) == null) {
            return;
        }
        MixfixParser parser = null;
        try {
            parser = operatorParser(resource);
        } catch (RuntimeException | StackOverflowError e) {
            report(resource, CrmlDiagnostic.of(Kind.OPERATOR_CALL, e, resource.location()), new ArrayList<>());
        }
        if (resolveOperatorCalls(resource, parser) == null) {
            return;
        }
        if (lift(resource) == null) {
            return;
        }
        resource.markResolved();
    }

    /**
     * Stage 1: resolves the references queued while building the DOM. Each
     * reference left unresolved is an {@link Kind#UNRESOLVED_REFERENCE} error
     * at the position of the object holding it.
     *
     * @return what the stage did; null stops the resolution of {@code resource}
     */
    LinkResult link(CrmlResource resource) {
        List<CrmlDiagnostic> diagnostics = new ArrayList<>();
        List<UnresolvedLink> unresolved = new ArrayList<>();
        try {
            unresolved = resource.getVisitor().link();
            for (UnresolvedLink link : unresolved) {
                report(resource, resource.getVisitor().diagnosticAt(link.host(), Kind.UNRESOLVED_REFERENCE,
                    "Unresolved reference " + link, null), diagnostics);
            }
        } catch (RuntimeException | StackOverflowError e) {
            report(resource, CrmlDiagnostic.of(Kind.LINKING, e, resource.location()), diagnostics);
        }
        return new LinkResult(unresolved, diagnostics);
    }

    /**
     * The parser for the operator calls of {@code resource}: today the
     * operators declared in its root. An exception thrown here is reported as
     * an {@link Kind#OPERATOR_CALL} error, and calls are then not resolved.
     */
    MixfixParser operatorParser(CrmlResource resource) {
        return resource.getVisitor().operatorParser();
    }

    /**
     * Stage 2: replaces each Sequence that spells out a call to an operator
     * in scope with that call. Sequences that name no operator stay in the
     * DOM; they are listed in the result but not reported, as before. A call
     * that can be read in several ways is an {@link Kind#OPERATOR_CALL} error
     * at its position, and stays a Sequence.
     *
     * @param parser the parser from {@link #operatorParser}; null resolves nothing
     * @return what the stage did; null stops the resolution of {@code resource}
     */
    CallResult resolveOperatorCalls(CrmlResource resource, MixfixParser parser) {
        List<CrmlDiagnostic> diagnostics = new ArrayList<>();
        List<DomModification> replaced = new ArrayList<>();
        try {
            replaced = resource.getVisitor().resolveOperatorCalls(parser);
        } catch (RuntimeException | StackOverflowError e) {
            report(resource, CrmlDiagnostic.of(Kind.OPERATOR_CALL, e, resource.location()), diagnostics);
        }
        for (CrmlDiagnostic error : resource.getVisitor().takeErrors()) {
            report(resource, error, diagnostics);
        }
        return new CallResult(replaced, sequences(resource.getRoot()), diagnostics);
    }

    /**
     * Stage 3: links each {@code x.element} to the application it lifts.
     * Needs the calls of stage 2 in the DOM.
     *
     * @return what the stage did; null stops the resolution of {@code resource}
     */
    LiftResult lift(CrmlResource resource) {
        List<CrmlDiagnostic> diagnostics = new ArrayList<>();
        List<Liftable> linked = new ArrayList<>();
        try {
            linked = LiftBuilder.linkAll(resource.getRoot());
        } catch (RuntimeException | StackOverflowError e) {
            report(resource, CrmlDiagnostic.of(Kind.LIFTING, e, resource.location()), diagnostics);
        }
        return new LiftResult(linked, diagnostics);
    }

    /** Adds {@code diagnostic} to the errors of {@code resource} and to {@code stage}. */
    private static void report(CrmlResource resource, CrmlDiagnostic diagnostic, List<CrmlDiagnostic> stage) {
        resource.getErrors().add(diagnostic);
        stage.add(diagnostic);
    }

    /** The Sequences below {@code root}: the first element of each, not its continuation. */
    private static List<Sequence> sequences(EObject root) {
        List<Sequence> found = new ArrayList<>();
        if (root == null) {
            return found;
        }
        TreeIterator<EObject> contents = EcoreUtil.getAllContents(root, true);
        while (contents.hasNext()) {
            EObject object = contents.next();
            if (object instanceof Sequence && !(object.eContainer() instanceof Sequence)) {
                found.add((Sequence) object);
            }
        }
        return found;
    }

    /**
     * Validates {@code root}: the errors and warnings collected on its
     * resource while loading and linking, a {@link Kind#BROKEN_DEPENDENCY}
     * error for each dependency whose file has errors, then the CRML
     * validation rules. The errors of the dependencies themselves are not
     * included; they are reported when the dependency is validated.
     */
    public Diagnostic validate(EObject root) {
        BasicDiagnostic result = new BasicDiagnostic(CRMLValidator.DIAGNOSTIC_SOURCE, 0,
            "Validation of " + root, new Object[] { root });
        Resource resource = root.eResource();
        if (resource != null) {
            addAll(result, resource.getErrors(), Diagnostic.ERROR);
            addAll(result, resource.getWarnings(), Diagnostic.WARNING);
        }
        if (resource instanceof CrmlResource) {
            addAll(result, brokenDependencies((CrmlResource) resource), Diagnostic.ERROR);
        }
        result.add(CRMLValidator.validate(root));
        return result;
    }

    /**
     * Validates one resource: the errors and warnings collected while loading
     * and linking it, then, if it has a root, the CRML validation rules. Unlike
     * {@link #validate(EObject)}, this also works for a file that has no DOM,
     * e.g. because of syntax errors.
     */
    public Diagnostic validate(CrmlResource resource) {
        BasicDiagnostic result = new BasicDiagnostic(CRMLValidator.DIAGNOSTIC_SOURCE, 0,
            "Validation of " + resource.getURI(), new Object[] { resource });
        addAll(result, resource.getErrors(), Diagnostic.ERROR);
        addAll(result, resource.getWarnings(), Diagnostic.WARNING);
        addAll(result, brokenDependencies(resource), Diagnostic.ERROR);
        EObject root = resource.getRoot();
        if (root != null) {
            result.add(CRMLValidator.validate(root));
        }
        return result;
    }

    /**
     * One {@link Kind#BROKEN_DEPENDENCY} error per dependency of
     * {@code resource} that is declared by a file with syntax or build
     * errors, at the dependency's name in the header. Missing and duplicate
     * dependencies are left to the dependency validation rules.
     */
    private List<CrmlDiagnostic> brokenDependencies(CrmlResource resource) {
        List<CrmlDiagnostic> broken = new ArrayList<>();
        RootHeader header = resource.getHeader();
        if (header == null) {
            return broken;
        }
        for (RootHeader.DependencyName dependency : header.dependencies()) {
            RootIndex.Lookup lookup = index.lookup(dependency.name());
            if (lookup.status() == RootIndex.Lookup.Status.BROKEN) {
                broken.add(new CrmlDiagnostic(Kind.BROKEN_DEPENDENCY,
                    "Dependency " + dependency.name() + " has errors: " + lookup.resources().get(0).location(),
                    resource.location(), dependency.line(), dependency.column()));
            }
        }
        return broken;
    }

    private static void addAll(BasicDiagnostic target, List<? extends Resource.Diagnostic> source, int severity) {
        for (Resource.Diagnostic diagnostic : source) {
            Throwable cause = diagnostic instanceof CrmlDiagnostic ? ((CrmlDiagnostic) diagnostic).getCause() : null;
            Object[] data = cause == null ? new Object[] { diagnostic } : new Object[] { diagnostic, cause };
            target.add(new BasicDiagnostic(severity, CRMLValidator.DIAGNOSTIC_SOURCE, 0, diagnostic.toString(), data));
        }
    }
}
