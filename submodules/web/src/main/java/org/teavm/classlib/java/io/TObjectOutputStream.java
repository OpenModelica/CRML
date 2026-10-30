package org.teavm.classlib.java.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * java.io.ObjectOutputStream, which TeaVM does not provide, for EMF's
 * serialized EJavaObject values. CRML has none; Java serialization is not
 * supported in the browser.
 */
public class TObjectOutputStream extends OutputStream {
  public TObjectOutputStream(OutputStream out) throws IOException {
    throw new IOException("Java serialization is not supported in the browser");
  }
  @Override public void write(int b) throws IOException { throw new IOException(); }
  public void writeObject(Object obj) throws IOException { throw new IOException(); }
}
