package org.tinymediamanager.thirdparty;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class implements a fixed-size ring buffer (aka a circular buffer) of
 * objects. Objects are always added to the head of the buffer and removed from
 * the tail. Objects in the middle cannot be retrieved or removed. Decisions on
 * how to handle adding an object to a full buffer are delegated to an
 * OverflowPolicy, which may have the option to fail the add, remove the last
 * element and add, etc. The policy may do whatever it wishes with the ring
 * buffer.
 * <p>
 * This class is fully thread-safe and reentrant.
 * 
 * @author David
 * 
 * @param <T>
 */
public class RingBuffer<T> {
  private final int              maxSize;
  private T[]                    data;
  private int                    head;
  private int                    tail;
  private int                    tailWrapCount;
  private boolean                inOverflow = false;
  private AtomicInteger          count      = new AtomicInteger();
  private AtomicInteger          modCount   = new AtomicInteger();
  private ReentrantReadWriteLock headLock;
  private ReentrantReadWriteLock tailLock;

  @SuppressWarnings("unchecked")
  public RingBuffer(int maxSize) {
    headLock = new ReentrantReadWriteLock();
    tailLock = new ReentrantReadWriteLock();
    this.maxSize = maxSize;
    data = (T[]) new Object[maxSize];
    head = tail = 0;
  }

  @SuppressWarnings("unchecked")
  public void clear() {
    headLock.writeLock().lock();
    tailLock.writeLock().lock();
    head = tail = 0;
    count.set(0);
    modCount.incrementAndGet();
    data = (T[]) new Object[maxSize];
    tailLock.writeLock().unlock();
    headLock.writeLock().unlock();
  }

  protected void lockTail() {
    tailLock.writeLock().lock();
  }

  protected void unlockTail() {
    tailLock.writeLock().lock();
  }

  public void add(T object) {
    headLock.writeLock().lock();
    try {
      if (!isEmpty()) {
        if (head == maxSize) {
          head = 0;
        }
        if (head == tail) {
          if (!inOverflow) {
            inOverflow = true;
            remove();
            add(object);
            inOverflow = false;
            return;
          }
          else {
            throw new IllegalStateException("Double overflow in RingBuffer");
          }
        }
      }
      data[head++] = object;
      count.incrementAndGet();
      modCount.incrementAndGet();
    }
    finally {
      headLock.writeLock().unlock();
    }
  }

  public T getTailItem() {
    return data[tail];
  }

  public T remove() {
    tailLock.writeLock().lock();
    if (isEmpty()) {
      tailLock.writeLock().unlock();
      return null;
    }
    T obj = data[tail++];
    if (tail == maxSize) {
      tail = 0;
      tailWrapCount++;
    }
    count.decrementAndGet();
    modCount.incrementAndGet();
    tailLock.writeLock().unlock();
    return obj;
  }

  public void remove(int numToRemove) {
    tailLock.writeLock().lock();
    if (isEmpty()) {
      tailLock.writeLock().unlock();
      return;
    }
    // don't let head move while we're removing
    headLock.readLock().lock();
    if (tail + numToRemove >= maxSize) {
      int newTail = (tail + numToRemove) - maxSize;
      if (newTail > head) {
        newTail = head;
      }
      tail = newTail;
      tailWrapCount++;
    }
    // check for passing head
    else if ((tail < head) && (tail + numToRemove >= head)) {
      tail = head;
    }
    // recalc size
    if (tail == head) {
      count.set(0);
    }
    else {
      int newCount = count.get();
      newCount -= numToRemove;
      count.set(newCount);
    }
    modCount.incrementAndGet();
    headLock.readLock().unlock();
    tailLock.writeLock().unlock();
  }

  public Iterator<T> iterator() {
    return new RingBufferIterator<T>(this);
  }

  private static class RingBufferIterator<T> implements Iterator<T> {
    private int                 next;
    private int                 nextWrapCount;
    private final RingBuffer<T> buffer;
    private Mode                mode;
    private boolean             hasNext;
    private int                 expectedModCount;

    private enum Mode {
      EMPTY, MODE1, MODE2LEFT, MODE2RIGHT, START, END, INVALID
    }

    private Mode mode() {
      if (buffer.isEmpty()) {
        return Mode.EMPTY;
      }
      else if (next == buffer.tail) {
        return Mode.START;
      }
      else if (next == buffer.head) {
        return Mode.END;
      }
      else if (buffer.tail < buffer.head) {
        if (next < buffer.head) {
          return Mode.MODE1;
        }
        else {
          return Mode.INVALID;
        }
      }
      else if (next < buffer.head) {
        return Mode.MODE2LEFT;
      }
      else if (next > buffer.tail) {
        return Mode.MODE2RIGHT;
      }
      return Mode.INVALID;
    }

    public RingBufferIterator(RingBuffer<T> buffer) {
      this.buffer = buffer;
      next = buffer.tail;
      nextWrapCount = buffer.tailWrapCount;
      mode = Mode.START;
      hasNext = calcHasNext();
      expectedModCount = buffer.modCount.get();
    }

    @Override
    public boolean hasNext() {
      buffer.headLock.readLock().lock();
      if (expectedModCount != buffer.modCount.get()) {
        hasNext = calcHasNext();
      }
      buffer.headLock.readLock().unlock();
      return hasNext;
    }

    private boolean calcHasNext() {
      if (mode == Mode.INVALID) {
        return false;
      }
      Mode newMode = mode();
      if ((newMode == Mode.EMPTY) || (newMode == Mode.END)) {
        // Done
        mode = newMode;
        return false;
      }
      if (newMode == mode) {
        // Not empty, not end, and no mode change, so we are still iterating
        // along prev path.
        // If the iterator wrapped 0 or 1 times then we're ok.
        if ((buffer.tailWrapCount == nextWrapCount) || (buffer.tailWrapCount == nextWrapCount - 1)) {
          // If we are just starting, assume M1
          if (mode == Mode.START) {
            mode = Mode.MODE1;
          }
          return true;
        }
        else {
          // Data wrapped but iterator didn't, so next is now invalid
          mode = Mode.INVALID;
          return false;
        }
      }
      if (mode == Mode.END) {
        // We were at the end, but now we are not because newMode != END. Some
        // items must have been added.
        mode = newMode;
        switch (mode) {
          case MODE1:
          case MODE2LEFT:
          case MODE2RIGHT:
          case START: {
            return true;
          }
          default: {
            return false;
          }
        }
      }
      if (newMode == Mode.START) {
        if (buffer.tailWrapCount == nextWrapCount) {
          // Tail caught up, keep going
          mode = newMode;
          return true;
        }
        mode = Mode.INVALID;
        return false;
      }
      if (mode == Mode.MODE1) {
        if (newMode == Mode.MODE2RIGHT) {
          // M1 -> M2R
          if (buffer.tailWrapCount == nextWrapCount) {
            // only head wrapped, and we're still in the window
            mode = newMode;
            return true;
          }
        }
        else if (newMode == Mode.MODE2LEFT) {
          // M1 -> M2L
          if (buffer.tailWrapCount == nextWrapCount - 1) {
            // head wrapped AND next wrapped
            mode = newMode;
            return true;
          }
        }
        mode = Mode.INVALID;
        return false;
      }
      if (mode == Mode.MODE2LEFT) {
        // M2L -> M1
        if (newMode == Mode.MODE1) {
          // tail wrapped, wrap count must be same
          if (buffer.tailWrapCount == nextWrapCount) {
            mode = newMode;
            return true;
          }
        }
        if (newMode == Mode.MODE2RIGHT) {
          // M2L -> M2R
          if (buffer.tailWrapCount == (nextWrapCount + 1)) {
            // tail wrapped AND head wrapped
            mode = newMode;
            return true;
          }
        }
        mode = Mode.INVALID;
        return false;
      }
      if (mode == Mode.MODE2RIGHT) {
        if (newMode == Mode.MODE2LEFT) {
          if (nextWrapCount == (buffer.tailWrapCount + 1)) {
            // only next wrapped, still in window
            mode = newMode;
            return true;
          }
        }
        if (newMode == Mode.MODE1) {
          if (nextWrapCount == buffer.tailWrapCount) {
            // if both wrapped together, then ok
            mode = newMode;
            return true;
          }
        }
        mode = Mode.INVALID;
        return false;
      }
      // This should never happen
      mode = Mode.INVALID;
      return false;
    }

    @Override
    // This really should be called atomically with hasNext...
    public T next() {
      if (!hasNext) {
        return null;
      }
      buffer.headLock.readLock().lock();
      T item = buffer.data[next++];
      if (next == buffer.maxSize) {
        next = 0;
        nextWrapCount++;
      }
      hasNext = calcHasNext();
      buffer.headLock.readLock().unlock();
      return item;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public int count() {
    return count.get();
  }

  public int maxSize() {
    return maxSize;
  }

  public boolean isEmpty() {
    return (count.get() == 0);
  }

  int head() {
    return head;
  }

  int tail() {
    return tail;
  }

  int tailWrapCount() {
    return tailWrapCount;
  }
}