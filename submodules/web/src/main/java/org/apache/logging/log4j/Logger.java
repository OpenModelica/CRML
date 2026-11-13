package org.apache.logging.log4j;

/** Stand-in for the log4j API in the browser build: messages go to System.err. */
public interface Logger {
  default void error(String message) { System.err.println("ERROR " + message); }
  default void error(String message, Throwable t) { System.err.println("ERROR " + message); }
  default void warn(String message) { System.err.println("WARN " + message); }
  default void info(String message) { }
  default void debug(String message) { }
  default void trace(String message) { }
}
