package org.teavm.classlib.java.util.concurrent.locks;

/**
 * java.util.concurrent.locks.Lock, which TeaVM does not provide, for EMF's
 * URI pool. The browser runs one thread, so there is nothing to lock.
 */
public interface TLock {
  void lock();
  void unlock();
}
