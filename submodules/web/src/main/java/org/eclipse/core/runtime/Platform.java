package org.eclipse.core.runtime;

import org.osgi.framework.Bundle;

/** Stand-in for the Eclipse platform, which never runs in the browser. */
public final class Platform {
  private Platform() { }
  public static boolean isRunning() { return false; }
  public static Bundle getBundle(String symbolicName) { return null; }
}
