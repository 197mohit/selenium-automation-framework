package com.paytm.framework.utils.resourcePool;

import java.util.List;

/**
 * Created by deepakkumar on 21/2/18.
 */
public interface PrioritySetter<E, L> {

    void add(E entity, List<L> labels);

    void delete(E entity);

    List<E> getEntities(List<L> labels);

}
