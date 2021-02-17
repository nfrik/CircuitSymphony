package org.circuitsymphony.util;

import java.util.Arrays;

/**
 * Simple implementation of pre-allocated circular buffer
 *
 * @param <E> type of elements that will be stored
 */
public class CircularList<E> {
    private E[] entries;
    private int head;

    @SuppressWarnings("unchecked")
    public CircularList(int size) {
        entries = (E[]) new Object[size];
        clear();
    }

    /**
     * Clears this list by settings all elements to null and resetting head position
     */
    public void clear() {
        Arrays.fill(entries, null);
        head = 0;
    }

    /**
     * Puts new element into the list
     */
    public void put(E e) {
        entries[head] = e;
        head++;
        if (head == entries.length) {
            head = 0;
        }
    }

    /**
     * @param index absolute index of element to retrieve
     * @return element or null if no element is present at provided index
     */
    public E get(int index) {
        if (index < 0) throw new IllegalArgumentException("Index can't be negative");
        return entries[index];
    }

    /**
     * Returns element that is n elements behind head position.
     * For example offset 0 will return element that was added last, offset 1 will return element that was added second to last.
     * Offsets greater than list size will be automatically wrapped.
     *
     * @return element or null if no element is present at specified offset
     */
    public E lookBack(int offset) {
        if (offset < 0) throw new IllegalArgumentException("Offset can't be negative");
        int index = head - 1 - offset;
        while (index < 0) {
            index += entries.length;
        }
        return entries[index];
    }

    /**
     * @return current head position, note that head points to next slot that will be written to.
     * (not to the element that was added last)
     */
    public int getHead() {
        return head;
    }
}
