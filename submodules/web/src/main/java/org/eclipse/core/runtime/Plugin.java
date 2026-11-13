package org.eclipse.core.runtime;

import org.osgi.framework.Bundle;

/**
 * Stand-in for the Eclipse plug-in class, which EMF extends for its Eclipse
 * plug-ins; never instantiated, since the platform never runs in the browser.
 */
public abstract class Plugin {
  public Plugin() {
    throw new UnsupportedOperationException("The Eclipse platform does not run in the browser");
  }
  public final Bundle getBundle() { return null; }
}
