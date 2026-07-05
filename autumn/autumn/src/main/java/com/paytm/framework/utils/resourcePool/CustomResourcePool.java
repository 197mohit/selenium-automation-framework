package com.paytm.framework.utils.resourcePool;

import com.paytm.framework.reporting.Reporter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by deepakkumar on 21/2/18.
 */
public class CustomResourcePool<E> implements ResourcePool<E> {

    private final int lockTimeout;
    private final TimeUnit timeUnit;
    private final Map<E, ReentrantReadWriteLock> lockMap = new HashMap<>();


    public CustomResourcePool(int lockTimeout, TimeUnit timeUnit) {
        this.lockTimeout = lockTimeout;
        this.timeUnit = timeUnit;
    }

    public CustomResourcePool() {
        this.lockTimeout = 15;
        this.timeUnit = TimeUnit.MINUTES;
    }

    @Override
    public void add(E resource) {
        this.lockMap.put(resource, new ReentrantReadWriteLock(true));
    }

    @Override
    public void delete(E resource) {
        this.lockMap.remove(resource);
    }

    @Override
    public E readLock(E resource) throws Exception {
        try {
            if (!this.lockMap.get(resource).readLock().tryLock(lockTimeout, timeUnit))
                throw new RuntimeException("Resource not acquired in the specified time. \n"+
                        "Queued Threads: "+lockMap.get(resource).hasQueuedThreads() +"\n"+
                        lockMap.get(resource).toString() +"\n"+
                        " Queue Length: "+lockMap.get(resource).getQueueLength() +"\n"+
                        " Read Hold Count: "+lockMap.get(resource).getReadHoldCount() +"\n");
        } catch (InterruptedException e) {
            Reporter.report.error("Couldn't readLock "+e.getMessage());
            throw new RuntimeException("Thread interrupted");
        }
        return resource;
    }

    @Override
    public E writeLock(E resource) throws Exception {
        try {
            if (!this.lockMap.get(resource).writeLock().tryLock(lockTimeout, timeUnit))
                throw new RuntimeException("Resource not acquired in the specified time");
        } catch (InterruptedException e) {
            Reporter.report.error("Couldn't writeLock "+e.getMessage());
            throw new RuntimeException("Thread interrupted");
        }
        return resource;
    }

    @Override
    public boolean isWriteLocked(E resource) {
        ReentrantReadWriteLock rwLock = this.lockMap.get(resource);
        return rwLock.isWriteLocked();
    }

    @Override
    public boolean isReadLocked(E resource) {
        ReentrantReadWriteLock rwLock = this.lockMap.get(resource);
        return (rwLock.getReadLockCount() != 0) ? true : false;
    }

    @Override
    public void readUnlock(E resource) {
        try {
            this.lockMap.get(resource).readLock().unlock();
        } catch (IllegalMonitorStateException e) {

        }
    }

    @Override
    public void writeUnlock(E resource) {
        try {
            this.lockMap.get(resource).writeLock().unlock();
        } catch (IllegalMonitorStateException e) {

        }

    }


}
