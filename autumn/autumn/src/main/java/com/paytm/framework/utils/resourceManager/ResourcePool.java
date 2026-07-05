package com.paytm.framework.utils.resourceManager;

import com.paytm.framework.conditions.Wait;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class ResourcePool<R extends Comparable> {

    private final List<R> resources;
    private final List<R> readResources;
    private final Wait wait;
    private final long timeout;
    private final ExecutorService service;

    public ResourcePool(List<R> resources, Wait wait, long timeout) {
        this.resources = resources;
        this.wait = wait;
        this.timeout = timeout;
        this.readResources = new ArrayList<>();
        this.service = Executors.newSingleThreadExecutor();
    }

    public ResourcePool(List<R> resources, Wait wait) {
        this(resources, wait, 5 * 60 * 1000);
    }

    public R find(boolean editable, Predicate<R> predicate) {
        try {
            Future<R> future = service.submit(() -> {
                List<R> filteredList = new ArrayList<>();
                this.wait.apply(() -> {
                    List<R> resources = new ArrayList<>(this.resources);
                    if (editable) resources.removeAll(this.readResources);
                    synchronized (this) {
                        for (R resource : resources) {
                            if (predicate.test(resource)) {
                                filteredList.add(resource);
                                break;
                            }
                        }
                        return !filteredList.isEmpty();
                    }
                });
                if (filteredList.isEmpty()) throw new RuntimeException("Resource not acquired in the specified time");
                R result = filteredList.stream().reduce((a, b) -> a.compareTo(b) <= 0 ? a : b).get();//TODO can subtract locked resources from filteredList again to handle edge case where any of the resources in filtered list gets locked by other thread; And these 2-3 steps can be put in synchronised block for total safety
                if (editable) this.resources.remove(result);
                else this.readResources.add(result);
                return result;
            });
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            throw new RuntimeException("resource not acquired", e);
        }
    }

    public boolean add(R resource) {
        if (resource == null) return false;
        return this.readResources.contains(resource) ? this.readResources.remove(resource) : this.resources.add(resource);
    }

    public boolean addAll(Collection<R> resources) {
        if (resources == null) return false;
        return resources.stream().allMatch(this::add);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        service.shutdownNow();
    }
}
