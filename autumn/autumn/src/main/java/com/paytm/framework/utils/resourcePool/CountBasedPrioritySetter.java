package com.paytm.framework.utils.resourcePool;

import java.util.*;

/**
 * Created by deepakkumar on 21/2/18.
 */
public class CountBasedPrioritySetter<E, L> implements PrioritySetter<E, L> {

    private final Map<E, List<L>> labelsMap = new HashMap<>();

    @Override
    public void add(E entity, List<L> labels) {
        labels.sort(new StringComparator<L>());
        this.labelsMap.put(entity, labels);
    }

    @Override
    public void delete(E entity) {
        this.labelsMap.remove(entity);
    }

    @Override
    public List<E> getEntities(List<L> labels) {
        /*getting all users having required configs*/
        List<E> entities = new ArrayList<>();
        Set<E> labelEntities = labelsMap.keySet();
        for (E entity : labelEntities) {
            if (labelsMap.get(entity).containsAll(labels))
                entities.add(entity);
        }
        /*getting the counts of labels*/
        Map<List<L>, Integer> labelsCountMap = new HashMap<>();
        for (E entity : entities) {
            Boolean flag = false;
            for (List<L> label : labelsCountMap.keySet()) {
                if (label.equals(labelsMap.get(entity))) {
                    labelsCountMap.put(label, labelsCountMap.get(label) + 1);
                    flag = true;
                }
            }
            if (!flag) {
                labelsCountMap.put(labelsMap.get(entity), 1);
            }
        }
        /*sorted based on values*/
        labelsCountMap = sortByValue(labelsCountMap);

        entities.clear();
        for (List<L> label : labelsCountMap.keySet()) {
            for (E key : labelsMap.keySet()) {
                if (label.equals(labelsMap.get(key))) {
                    entities.add(key);
                }
            }
        }
        return entities;
    }

    private Map<List<L>, Integer> sortByValue(Map<List<L>, Integer> unsortedMap) {
        Map<List<L>, Integer> sortedMap = new LinkedHashMap<>();
        unsortedMap.entrySet().stream()
                .sorted(Map.Entry.<List<L>, Integer>comparingByValue().reversed())
                .forEachOrdered(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        return sortedMap;
    }

    private class ValueComparator implements Comparator {
        Map map;

        public ValueComparator(Map map) {
            this.map = map;
        }

        public int compare(Object key1, Object key2) {
            Comparable value1 = (Comparable) map.get(key1);
            Comparable value2 = (Comparable) map.get(key2);
            int compare = value1.compareTo(value2);
            return compare != 0 ? compare : 1;
        }
    }

    private class StringComparator<L> implements Comparator<L> {
        @Override
        public int compare(L o1, L o2) {
            String value1 = o1.toString();
            String value2 = o2.toString();
            return String.CASE_INSENSITIVE_ORDER.compare(value1, value2);
        }
    }

}

