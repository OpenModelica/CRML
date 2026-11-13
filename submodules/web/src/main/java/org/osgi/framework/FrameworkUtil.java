package org.osgi.framework;

/** Stand-in for OSGi, which never runs in the browser. */
public final class FrameworkUtil {
  private FrameworkUtil() { }
  public static Bundle getBundle(Class<?> c) { return null; }
}
