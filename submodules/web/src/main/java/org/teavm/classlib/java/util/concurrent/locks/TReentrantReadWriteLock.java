package org.teavm.classlib.java.util.concurrent.locks;

/**
 * java.util.concurrent.locks.ReentrantReadWriteLock, which TeaVM does not
 * provide, for EMF's URI pool. The browser runs one thread, so there is
 * nothing to lock.
 */
public class TReentrantReadWriteLock {
  private final ReadLock readLock = new ReadLock();
  private final WriteLock writeLock = new WriteLock();

  public ReadLock readLock() { return readLock; }
  public WriteLock writeLock() { return writeLock; }

  public static class ReadLock implements TLock {
    public void lock() { }
    public void unlock() { }
  }

  public static class WriteLock implements TLock {
    public void lock() { }
    public void unlock() { }
  }
}
