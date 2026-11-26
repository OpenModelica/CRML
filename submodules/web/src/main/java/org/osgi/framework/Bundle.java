package org.osgi.framework;

/** Stand-in for OSGi, which never runs in the browser. */
public interface Bundle {
  String getSymbolicName();
  int getState();
  Class<?> loadClass(String name) throws ClassNotFoundException;
}
