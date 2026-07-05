package com.paytm.framework.utils.resourcePool;

/**
 * Created by deepakkumar on 21/2/18.
 */
public interface ResourcePool<E> {

    void add(E resource);

    void delete(E resource);

    E readLock(E resource) throws Exception;

    E writeLock(E resource) throws Exception;

    boolean isWriteLocked(E resource);

    boolean isReadLocked(E resource);

    void readUnlock(E resource);

    void writeUnlock(E resource);

}
