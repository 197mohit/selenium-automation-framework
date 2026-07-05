//package com.paytm.framework.utils.resourcePool;
//
//import sun.misc.ASCIICaseInsensitiveComparator;
//
//import java.util.*;
//
///**
// * Created by deepakkumar on 21/2/18.
// */
//public class CountBasedPrioritySetter2<E, L> implements PrioritySetter<E, L> {
//
//    private final List<List<L>> labelsList = new ArrayList<>();
//
//    @Override
//    public void add(E entity, List<L> labels) {
//        labels.sort(new StringComparator<L>());
//        this.labelsList.add(labels);
//    }
//
//    @Override
//    public void delete(E entity) {
//        this.labelsList.remove(entity);
//    }
//
//    @Override
//    public List<L> getEntities(List<L> labels) {
//        /*getting all users having required configs*/
//        List<List<L>> filteredLabels = new ArrayList<>();
//        for (List<L> label : labelsList) {
//            if (label.containsAll(labels))
//                filteredLabels.add(label);
//        }
//        /*getting the counts of labels*/
//        Map<List<L>, Integer> frequencyMap = new HashMap<>();
//        for (List<L> label : filteredLabels) {
//            if (frequencyMap.containsKey(label)) {
//                frequencyMap.put(label, frequencyMap.get(label) + 1);
//            } else {
//                frequencyMap.put(label, 1);
//            }
//        }
//        /*sorted based on values*/
//        frequencyMap = sortByValue(frequencyMap);
//        filteredLabels.clear();
//        for (List<L> label : frequencyMap.keySet()) {
//            for (int i = 0; i < frequencyMap.get(label); i++) {
//                filteredLabels.add(label);
//            }
//        }
//        return  filteredLabels;
//    }
//
//
//    private Map sortByValue(Map unsortedMap) {
//        Map sortedMap = new TreeMap(new ValueComparator(unsortedMap));
//        sortedMap.putAll(unsortedMap);
//        return sortedMap;
//    }
//
//
//    private class ValueComparator<L> implements Comparator<List<L>> {
//        Map map;
//
//        public ValueComparator(Map map) {
//            this.map = map;
//        }
//
//        public int compare(List<L> key1, List<L> key2) {
//            Comparable value1 = (Comparable) map.get(key1);
//            Comparable value2 = (Comparable) map.get(key2);
//            int compare = value1.compareTo(value2);
//            if (compare == 0) {
//                compare = key1.size() > key2.size() ? 1 : -1;
//            }
//            return compare;
//        }
//    }
//
//    private class StringComparator<L> implements Comparator<L> {
//
//        private Comparator comparator = new ASCIICaseInsensitiveComparator();
//
//        @Override
//        public int compare(L o1, L o2) {
//            Comparable value1 = o1.toString();
//            Comparable value2 = o2.toString();
//            return comparator.compare(value1, value2);
//        }
//    }
//}
//
