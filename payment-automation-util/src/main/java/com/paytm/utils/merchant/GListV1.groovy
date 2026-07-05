package com.paytm.utils.merchant

trait GListV1<E> implements Iterable<E> {

    E add(E element) {
        this.addAll([element]).find()
    }


    E remove(Object o) {
        this.removeAll([o]).find()
    }

    E removeLast() {
        this.removeAll([this.get(this.size() - 1)]).find()
    }

    abstract List<E> addAll(Collection<? extends E> c)


    abstract List<E> removeAll(Collection<?> c)


    List<E> retainAll(Collection<?> c) {
        this.removeAll(this.findAll() - c)
    }


    List<E> removeAll(Closure condition) {
        this.removeAll(this.findAll(condition))
    }

    int size() {
        this.iterator().size()
    }


    boolean isEmpty() {
        this.iterator().size() == 0
    }


    boolean contains(Object o) {
        this.iterator().toList().contains(o)
    }


    abstract Iterator<E> iterator()


    Object[] toArray() {
        this.iterator().toList().toArray()
    }

    boolean containsAll(Collection<?> c) {
        c.every { this.contains(it) }
    }


    boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException()
    }


    void clear() {
        this.removeAll { true }
        assert this.empty, "not able to clear $this"
    }


    E get(int index) {
        this.iterator()[index]
    }


    E set(int index, E element) {
        throw new UnsupportedOperationException()
    }


    void add(int index, E element) {
        throw new UnsupportedOperationException()
    }


    E remove(int index) {
        def o = this[index]
        this.remove(o) ? o : null
    }


    int indexOf(Object o) {
        this.iterator().findIndexOf { it == o.toString() }
    }


    int lastIndexOf(Object o) {
        this.iterator().findLastIndexOf { it == o.toString() }
    }


    ListIterator<E> listIterator() {
        this.iterator().toList().listIterator()
    }


    ListIterator<E> listIterator(int index) {
        this.iterator().toList().listIterator(index)
    }


    List<E> subList(int fromIndex, int toIndex) {
        this.iterator().toList().subList(fromIndex, toIndex)
    }


    String toString() {
        this.collect { it as String }
    }
}
