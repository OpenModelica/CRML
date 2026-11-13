package crml.web.emul.javax.xml.datatype;

/**
 * javax.xml.datatype.DatatypeFactory for EMF in the browser, where XML dates
 * and durations are not supported: CRML has neither.
 */
public abstract class DatatypeFactory {
  public static DatatypeFactory newInstance() throws DatatypeConfigurationException {
    throw new DatatypeConfigurationException("XML dates and durations are not supported in the browser");
  }
  public abstract XMLGregorianCalendar newXMLGregorianCalendar(String lexicalRepresentation);
  public abstract XMLGregorianCalendar newXMLGregorianCalendar(java.util.GregorianCalendar cal);
  public abstract Duration newDuration(String lexicalRepresentation);
}
