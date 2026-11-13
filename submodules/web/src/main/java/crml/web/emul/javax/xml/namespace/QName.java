package crml.web.emul.javax.xml.namespace;

/** javax.xml.namespace.QName for EMF in the browser. */
public class QName {
  private final String namespaceURI;
  private final String localPart;
  private final String prefix;

  public QName(String namespaceURI, String localPart, String prefix) {
    this.namespaceURI = namespaceURI == null ? "" : namespaceURI;
    this.localPart = localPart;
    this.prefix = prefix == null ? "" : prefix;
  }

  public QName(String namespaceURI, String localPart) { this(namespaceURI, localPart, ""); }
  public QName(String localPart) { this("", localPart, ""); }

  public String getNamespaceURI() { return namespaceURI; }
  public String getLocalPart() { return localPart; }
  public String getPrefix() { return prefix; }

  @Override public boolean equals(Object o) {
    return o instanceof QName && namespaceURI.equals(((QName) o).namespaceURI)
        && localPart.equals(((QName) o).localPart);
  }
  @Override public int hashCode() { return namespaceURI.hashCode() ^ localPart.hashCode(); }
  @Override public String toString() {
    return namespaceURI.isEmpty() ? localPart : "{" + namespaceURI + "}" + localPart;
  }
}
