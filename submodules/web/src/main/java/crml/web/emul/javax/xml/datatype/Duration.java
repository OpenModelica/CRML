package crml.web.emul.javax.xml.datatype;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * javax.xml.datatype.Duration for EMF in the browser. No instance exists,
 * since DatatypeFactory.newInstance always fails.
 */
public abstract class Duration {
  public abstract int getSign();
  public abstract Number getField(DatatypeConstants.Field field);
  public abstract boolean isSet(DatatypeConstants.Field field);
  public abstract Duration add(Duration rhs);
  public abstract void addTo(Calendar calendar);
  public abstract Duration multiply(BigDecimal factor);
  public abstract Duration negate();
  public abstract Duration normalizeWith(Calendar startTimeInstant);
  public abstract int compare(Duration duration);
  public int getYears() { throw new UnsupportedOperationException(); }
  public int getMonths() { throw new UnsupportedOperationException(); }
  public int getDays() { throw new UnsupportedOperationException(); }
  public int getHours() { throw new UnsupportedOperationException(); }
  public int getMinutes() { throw new UnsupportedOperationException(); }
  public int getSeconds() { throw new UnsupportedOperationException(); }
}
