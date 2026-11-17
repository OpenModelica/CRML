package org.openmodelica.crml.testutil.constraints;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * Short texts for values in the report: {@code Class Machine (Equipment.crml)},
 * {@code "'or'"}, {@code 3}. An object is shown by its class, its {@code name}
 * attribute if it has one, and the file of its resource.
 */
final class Describer {

    /** How many values a list shows before "… and n more". */
    static final int LIMIT = 5;

    private Describer() {
    }

    static String describe(Object value) {
        if (value instanceof EObject) {
            EObject object = (EObject) value;
            StringBuilder text = new StringBuilder(object.eClass().getName());
            EStructuralFeature name = object.eClass().getEStructuralFeature("name");
            if (name != null && !name.isMany()) {
                Object n = object.eGet(name);
                if (n instanceof String) {
                    text.append(' ').append(n);
                }
            }
            String file = file(object);
            if (file != null) {
                text.append(" (").append(file).append(')');
            }
            return text.toString();
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        if (value instanceof Enumerator) {
            return ((Enumerator) value).getLiteral();
        }
        return String.valueOf(value);
    }

    /** {@code {a, b}}, at most {@link #LIMIT} values, then "… and n more". */
    static String describeAll(Collection<?> values) {
        StringBuilder text = new StringBuilder("{");
        Iterator<?> it = values.iterator();
        for (int i = 0; it.hasNext() && i < LIMIT; i++) {
            text.append(i == 0 ? "" : ", ").append(describe(it.next()));
        }
        if (values.size() > LIMIT) {
            text.append(", … and ").append(values.size() - LIMIT).append(" more");
        }
        return text.append('}').toString();
    }

    /** The last segment of the object's resource URI, decoded; null outside a resource. */
    static String file(EObject object) {
        Resource resource = object.eResource();
        if (resource == null || resource.getURI() == null) {
            return null;
        }
        String last = resource.getURI().lastSegment();
        return last == null ? null : URI.decode(last);
    }
}
