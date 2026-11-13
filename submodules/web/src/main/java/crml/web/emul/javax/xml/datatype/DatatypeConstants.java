package crml.web.emul.javax.xml.datatype;

/** javax.xml.datatype.DatatypeConstants for EMF in the browser. */
public final class DatatypeConstants {
  private DatatypeConstants() { }

  public static final class Field {
    private final String name;
    private Field(String name) { this.name = name; }
    @Override public String toString() { return name; }
  }

  public static final Field YEARS = new Field("YEARS");
  public static final Field MONTHS = new Field("MONTHS");
  public static final Field DAYS = new Field("DAYS");
  public static final Field HOURS = new Field("HOURS");
  public static final Field MINUTES = new Field("MINUTES");
  public static final Field SECONDS = new Field("SECONDS");
}
