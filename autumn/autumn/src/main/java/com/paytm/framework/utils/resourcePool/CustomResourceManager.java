package com.paytm.framework.utils.resourcePool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by deepakkumar on 21/2/18.
 */
public class CustomResourceManager<E, L> implements ResourceManager<E, L> {

    private final ResourcePool<E> resourcePool;
    private final PrioritySetter<E, L> prioritySetter;
    private final Map<Long, List<E>> threadEntitiesMap = new ConcurrentHashMap<>();

    public CustomResourceManager(ResourcePool resourcePool, PrioritySetter prioritySetter) {
        this.resourcePool = resourcePool;
        this.prioritySetter = prioritySetter;
    }

    @Override
    public void add(E entity, List<L> labels) {
        this.resourcePool.add(entity);
        this.prioritySetter.add(entity, labels);
    }

    @Override
    public E getForRead(L... labels) throws Exception {
        return getForRead(Arrays.asList(labels));
    }

    @Override
    public E getForRead(List<L> labels) throws Exception {
        long currentThreadId = Thread.currentThread().getId();
        threadEntitiesMap.putIfAbsent(currentThreadId, new ArrayList<E>());
        List<E> entities = prioritySetter.getEntities(labels);
        if (entities.size() == 0)
            throw new RuntimeException("No entities available for passed labels");
        E readEntity = null;
        for (E entity : entities) {
            if (!threadEntitiesMap.get(currentThreadId).contains(entity)) {
                if (!resourcePool.isWriteLocked(entity)) {
                    readEntity = resourcePool.readLock(entity);
                    break;
                }
            }
        }
        if (readEntity == null) {
            for (E entity : entities) {
                if (!threadEntitiesMap.get(currentThreadId).contains(entity)) {
                    readEntity = resourcePool.readLock(entity);
                    break;
                }
            }
        }
        if (readEntity == null)
            throw new RuntimeException("No new entities available to be given to the thread");
        if (!threadEntitiesMap.get(currentThreadId).contains(readEntity))
            threadEntitiesMap.get(currentThreadId).add(readEntity);
        return readEntity;
    }

    @Override
    public E getForWrite(L... labels) throws Exception {
        return getForWrite(Arrays.asList(labels));
    }

    @Override
    public E getForWrite(List<L> labels) throws Exception {
        long currentThreadId = Thread.currentThread().getId();
        threadEntitiesMap.putIfAbsent(currentThreadId, new ArrayList<>());
        List<E> entities = prioritySetter.getEntities(labels);
        if (entities.size() == 0)
            throw new RuntimeException("No entities available for passed labels");
        E writeEntity = null;
        for (E entity : entities) {
            if (!threadEntitiesMap.get(currentThreadId).contains(entity)) {
                if (!(resourcePool.isWriteLocked(entity) || resourcePool.isReadLocked(entity))) {
                    writeEntity = resourcePool.writeLock(entity);
                    break;
                }
            }
        }
        if (writeEntity == null) {
            for (E entity : entities) {
                if (!threadEntitiesMap.get(currentThreadId).contains(entity)) {
                    writeEntity = resourcePool.writeLock(entity);
                    break;
                }
            }
        }
        if (writeEntity == null)
            throw new RuntimeException("No new entities available to be given to the thread");
        if (!threadEntitiesMap.get(currentThreadId).contains(writeEntity))
            threadEntitiesMap.get(currentThreadId).add(writeEntity);
        return writeEntity;
    }

    @Override
    public void release(E entity) {
        resourcePool.readUnlock(entity);
        resourcePool.writeUnlock(entity);
    }

    @Override
    public void release() {
        long currentThreadId = Thread.currentThread().getId();
        List<E> entitiesToRelease = new ArrayList<E>();
        if (threadEntitiesMap.get(currentThreadId) != null) {
            entitiesToRelease.addAll(threadEntitiesMap.get(currentThreadId));
        }
        for (E entity : entitiesToRelease) {
            release(entity);
            threadEntitiesMap.get(currentThreadId).remove(entity);
        }
    }

}
