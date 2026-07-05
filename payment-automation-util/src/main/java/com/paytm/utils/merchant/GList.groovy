package com.paytm.utils.merchant

trait GList<E> implements List<E> {

    @Override
    int size() {
        this.iterator().size()
    }

    @Override
    boolean isEmpty() {
        this.iterator().size() == 0
    }

    @Override
    boolean contains(Object o) {
        this.iterator().toList().contains(o)
    }

    @Override
    abstract Iterator<E> iterator()

    @Override
    Object[] toArray() {
        this.iterator().toList().toArray()
    }

    @Override
    <T> T[] toArray(T[] a) {
        this.iterator().toList().toArray(a)
    }

    @Override
    boolean add(E element) {
        this.addAll([element])
    }

    @Override
    boolean remove(Object o) {
        return this.removeAll([o])
    }

    @Override
    boolean containsAll(Collection<?> c) {
        c.every { this.contains(it) }
    }

    @Override
    abstract boolean addAll(Collection<? extends E> c)

    @Override
    boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    abstract boolean removeAll(Collection<?> c)

    @Override
    boolean retainAll(Collection<?> c) {
        return this.removeAll(this.findAll() - c)
    }

    @Override
    void clear() {
        if (!this.removeAll(this.collect())) throw new RuntimeException("not able to clear $this")
    }

    @Override
    E get(int index) {
        this.iterator()[index]
    }

    @Override
    E set(int index, E element) {
        throw new UnsupportedOperationException()
    }

    @Override
    void add(int index, E element) {
        throw new UnsupportedOperationException()
    }

    @Override
    E remove(int index) {
        def o = this[index]
        this.remove(o) ? o : null
    }

    @Override
    int indexOf(Object o) {
        this.iterator().findIndexOf { it == o.toString() }
    }

    @Override
    int lastIndexOf(Object o) {
        this.iterator().findLastIndexOf { it == o.toString() }
    }

    @Override
    ListIterator<E> listIterator() {
        this.iterator().toList().listIterator()
    }

    @Override
    ListIterator<E> listIterator(int index) {
        this.iterator().toList().listIterator(index)
    }

    @Override
    List<E> subList(int fromIndex, int toIndex) {
        this.iterator().toList().subList(fromIndex, toIndex)
    }

    boolean removeAll(Closure condition) {
        this.removeAll(this.findAll(condition))
    }

    @Override
    String toString() {
        this.collect { it as String }
    }
}
