package crml.language.pretty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public class PrettyPrint {

    private static final String CONNECTOR_MID = "├── ";
    private static final String CONNECTOR_LAST = "└── ";
    private static final String PIPE = "│   ";
    private static final String BLANK = "    ";

    public static String prettyPrint(EObject root) {
        StringBuilder builder = new StringBuilder();
        if (root == null) {
            builder.append("null");
            return builder.toString();
        }
        builder.append(identify(root)).append(System.lineSeparator());
        printChildren(root, "", builder);
        return builder.toString();
    }

    private static void printChildren(EObject host, String prefix, StringBuilder builder) {
        List<Entry> entries = collectEntries(host);
        for (int i = 0; i < entries.size(); i++) {
            printEntry(entries.get(i), prefix, i == entries.size() - 1, builder);
        }
    }

    private static void printEntry(Entry entry, String prefix, boolean isLast, StringBuilder builder) {
        String connector = isLast ? CONNECTOR_LAST : CONNECTOR_MID;
        String childPrefix = prefix + (isLast ? BLANK : PIPE);

        if (entry instanceof Leaf) {
            builder.append(prefix).append(connector).append(((Leaf) entry).text).append(System.lineSeparator());
            return;
        }

        Group group = (Group) entry;
        builder.append(prefix).append(connector).append(group.label).append(System.lineSeparator());
        for (int i = 0; i < group.members.size(); i++) {
            printMember(group.members.get(i), childPrefix, i == group.members.size() - 1, builder);
        }
    }

    private static void printMember(EObject member, String prefix, boolean isLast, StringBuilder builder) {
        String connector = isLast ? CONNECTOR_LAST : CONNECTOR_MID;
        if (member == null) {
            builder.append(prefix).append(connector).append("null").append(System.lineSeparator());
            return;
        }
        builder.append(prefix).append(connector).append(identify(member)).append(System.lineSeparator());
        printChildren(member, prefix + (isLast ? BLANK : PIPE), builder);
    }

    private static List<Entry> collectEntries(EObject host) {
        List<Entry> entries = new ArrayList<>();

        EAttribute idAttr = host.eClass().getEIDAttribute();
        for (EAttribute attr : host.eClass().getEAllAttributes()) {
            if (attr == idAttr) {
                continue;
            }
            entries.add(new Leaf("" + attr.getName() + ": " + Objects.toString(host.eGet(attr))));
        }

        for (EReference ref : host.eClass().getEAllReferences()) {
            if (ref.isContainment()) {
                List<EObject> members = new ArrayList<>();
                if (ref.isMany()) {
                    EList<?> ls = (EList<?>) host.eGet(ref);
                    for (Object o : ls) {
                        members.add((EObject) o);
                    }
                } else {
                    members.add((EObject) host.eGet(ref));
                }
                entries.add(new Group("" + ref.getName() + ":", members));
            } else if (ref.isMany()) {
                EList<?> ls = (EList<?>) host.eGet(ref);
                for (Object obj : ls) {
                    entries.add(new Leaf("" + ref.getName() + ": " + refLabel(obj) + " <crossref>"));
                }
            } else {
                EObject target = (EObject) host.eGet(ref);
                entries.add(new Leaf("" + ref.getName() + ": " + refLabel(target) + " <crossref>"));
            }
        }

        return entries;
    }

    private static String refLabel(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj instanceof EObject ? identify((EObject) obj) : String.valueOf(obj.hashCode());
    }

    private static String identify(EObject obj) {
        EAttribute idAttr = obj.eClass().getEIDAttribute();
        Object idValue = idAttr != null ? obj.eGet(idAttr) : null;
        String identity = idValue != null ? Objects.toString(idValue) : String.valueOf(obj.hashCode());
        String type = idAttr != null ? obj.eClass().getName() + "::" + idAttr.getName() : obj.eClass().getName();
        return identity + " (" + type + ")";
    }

    private static abstract class Entry {
    }

    private static final class Leaf extends Entry {
        final String text;

        Leaf(String text) {
            this.text = text;
        }
    }

    private static final class Group extends Entry {
        final String label;
        final List<EObject> members;

        Group(String label, List<EObject> members) {
            this.label = label;
            this.members = members;
        }
    }
}
