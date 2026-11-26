package org.teavm.classlib.java.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * java.io.ObjectInputStream, which TeaVM does not provide, for EMF's
 * serialized EJavaObject values. CRML has none; Java serialization is not
 * supported in the browser.
 */
public class TObjectInputStream extends InputStream {
  public TObjectInputStream(InputStream in) throws IOException {
    throw new IOException("Java serialization is not supported in the browser");
  }
  @Override public int read() throws IOException { throw new IOException(); }
  public Object readObject() throws IOException, ClassNotFoundException { throw new IOException(); }
}
