package crml.web.emul.javax.xml.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import crml.web.emul.javax.xml.namespace.QName;

/**
 * javax.xml.datatype.XMLGregorianCalendar for EMF in the browser. No instance
 * exists, since DatatypeFactory.newInstance always fails.
 */
public abstract class XMLGregorianCalendar implements Cloneable {
  public abstract void clear();
  public abstract void reset();
  public abstract void setYear(BigInteger year);
  public abstract void setYear(int year);
  public abstract void setMonth(int month);
  public abstract void setDay(int day);
  public abstract void setTimezone(int offset);
  public abstract void setHour(int hour);
  public abstract void setMinute(int minute);
  public abstract void setSecond(int second);
  public abstract void setMillisecond(int millisecond);
  public abstract void setFractionalSecond(BigDecimal fractional);
  public abstract BigInteger getEon();
  public abstract int getYear();
  public abstract BigInteger getEonAndYear();
  public abstract int getMonth();
  public abstract int getDay();
  public abstract int getTimezone();
  public abstract int getHour();
  public abstract int getMinute();
  public abstract int getSecond();
  public abstract BigDecimal getFractionalSecond();
  public abstract int compare(XMLGregorianCalendar xmlGregorianCalendar);
  public abstract XMLGregorianCalendar normalize();
  public abstract String toXMLFormat();
  public abstract QName getXMLSchemaType();
  public abstract boolean isValid();
  public abstract void add(Duration duration);
  public abstract GregorianCalendar toGregorianCalendar();
  public abstract GregorianCalendar toGregorianCalendar(TimeZone timezone, Locale aLocale,
      XMLGregorianCalendar defaults);
  public abstract TimeZone getTimeZone(int defaultZoneoffset);
}
