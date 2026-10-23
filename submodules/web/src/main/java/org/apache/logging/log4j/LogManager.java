package org.apache.logging.log4j;

/** Stand-in for the log4j API in the browser build. */
public final class LogManager {
  private static final Logger LOGGER = new Logger() { };
  private LogManager() { }
  public static Logger getLogger(Class<?> c) { return LOGGER; }
  public static Logger getLogger() { return LOGGER; }
}
