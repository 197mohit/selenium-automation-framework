package com.paytm.framework.utils.resourcePool;

import java.util.List;

/**
 * Created by deepakkumar on 21/2/18.
 */
public interface ResourceManager<E, L> {

    void add(E entity, List<L> labels);

    E getForRead(L... labels) throws Exception;

    E getForRead(List<L> labels) throws Exception;

    E getForWrite(L... labels) throws Exception;

    E getForWrite(List<L> labels) throws Exception;

    void release(E entity);

    void release();

}
