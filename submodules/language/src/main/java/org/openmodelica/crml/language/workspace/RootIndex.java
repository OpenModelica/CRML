package org.openmodelica.crml.language.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * The roots of a run, by declared name. Attached to the resource set as an
 * adapter, so code that only has an {@link EObject} (scope resolution,
 * validation rules) can find it through {@link #of(EObject)}.
 *
 * <p>Built from the {@link RootHeader headers}, so a file with syntax errors
 * is indexed too: looking up its name gives {@link Lookup.Status#BROKEN},
 * not {@link Lookup.Status#MISSING}.
 */
public class RootIndex extends AdapterImpl {

    /** The result of looking up a declared name. */
    public static final class Lookup {
        public enum Status {
            /** Exactly one file declares the name and its DOM was built completely. */
            FOUND,
            /**
             * One file declares the name, but it has syntax or build errors.
             * Its DOM, if any, is partial: {@link Lookup#root()} still gives it.
             */
            BROKEN,
            /** No file declares the name. */
            MISSING,
            /** Several files declare the name. Conflict rules are a later feature. */
            DUPLICATE
        }

        private final Status status;
        private final List<CrmlResource> resources;

        Lookup(Status status, List<CrmlResource> resources) {
            this.status = status;
            this.resources = resources;
        }

        public Status status() { return status; }

        /** The resources declaring the name; empty if missing. */
        public List<CrmlResource> resources() { return resources; }

        /**
         * The root if exactly one file declares the name: complete if
         * {@link Status#FOUND}, partial or null if {@link Status#BROKEN}.
         * Null otherwise.
         */
        public EObject root() {
            return status == Status.FOUND || status == Status.BROKEN ? resources.get(0).getRoot() : null;
        }
    }

    private final Map<String, List<CrmlResource>> byName = new LinkedHashMap<>();

    /** The index attached to {@code resourceSet}, or null if there is none. */
    public static RootIndex of(ResourceSet resourceSet) {
        return resourceSet == null ? null
            : (RootIndex) EcoreUtil.getExistingAdapter(resourceSet, RootIndex.class);
    }

    /** The index of the run {@code object} belongs to, or null. */
    public static RootIndex of(EObject object) {
        Resource resource = object == null ? null : object.eResource();
        return resource == null ? null : of(resource.getResourceSet());
    }

    @Override
    public boolean isAdapterForType(Object type) {
        return type == RootIndex.class;
    }

    /** Adds a loaded resource under its declared name. Does nothing without a header. */
    public void add(CrmlResource resource) {
        RootHeader header = resource.getHeader();
        if (header == null) {
            return;
        }
        List<CrmlResource> declaring = byName.computeIfAbsent(header.name(), name -> new ArrayList<>());
        if (!declaring.contains(resource)) {
            declaring.add(resource);
        }
    }

    /** Removes a resource, e.g. before it is reloaded. */
    public void remove(CrmlResource resource) {
        byName.values().forEach(list -> list.remove(resource));
        byName.values().removeIf(List::isEmpty);
    }

    /** Looks up a declared name. */
    public Lookup lookup(String name) {
        List<CrmlResource> declaring = byName.getOrDefault(name, Collections.emptyList());
        if (declaring.isEmpty()) {
            return new Lookup(Lookup.Status.MISSING, Collections.emptyList());
        }
        List<CrmlResource> copy = Collections.unmodifiableList(new ArrayList<>(declaring));
        if (declaring.size() > 1) {
            return new Lookup(Lookup.Status.DUPLICATE, copy);
        }
        CrmlResource resource = declaring.get(0);
        boolean complete = resource.getRoot() != null && resource.isComplete();
        return new Lookup(complete ? Lookup.Status.FOUND : Lookup.Status.BROKEN, copy);
    }

    /** The declared names, in the order they were first indexed. */
    public Iterable<String> names() {
        return Collections.unmodifiableSet(byName.keySet());
    }

    @Override
    public void setTarget(Notifier newTarget) {
        if (newTarget != null && !(newTarget instanceof ResourceSet)) {
            throw new IllegalArgumentException("A RootIndex is attached to a ResourceSet, not to " + newTarget);
        }
        super.setTarget(newTarget);
    }
}
