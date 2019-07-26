package gov.nih.nichd.ctdb.common.cache;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jun 15, 2007
 * Time: 9:28:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class CachedList extends CachedObject implements List {

    protected List cachedList = new ArrayList();

    public CachedList() {super();}


    public int size () {
        return cachedList.size();
    }
    public boolean isEmpty () {
        return cachedList.isEmpty();
    }
    public boolean contains (Object x){
        return cachedList.contains(x);
    }

    public Iterator iterator() {
        return cachedList.iterator();
    }

    public Object[] toArray() {
        return cachedList.toArray();
    }

    public boolean add(Object o) {
        return cachedList.add(o);
    }

    public boolean remove(Object o) {
        return cachedList.remove(o);
    }

    public boolean addAll(Collection c) {
        return cachedList.addAll(c);
    }

    public boolean addAll(int index, Collection c) {
        return cachedList.addAll(index, c);
    }

    public void clear() {
        cachedList.clear();
    }

    public Object get(int index) {
        return cachedList.get(index);
    }

    public Object set(int index, Object element) {
        return cachedList.set (index, element);
    }

    public void add(int index, Object element) {
        cachedList.add(index,  element);
    }

    public Object remove(int index) {
        return cachedList.remove(index);
    }

    public int indexOf(Object o) {
        return cachedList.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return cachedList.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return cachedList.listIterator();
    }

    public ListIterator listIterator(int index) {
        return cachedList.listIterator(index);
    }

    public List subList(int fromIndex, int toIndex) {
        return cachedList.subList(fromIndex, toIndex);
    }

    public boolean retainAll(Collection c) {
        return cachedList.retainAll(c);
    }

    public boolean removeAll(Collection c) {
        return cachedList.removeAll(c);
    }

    public boolean containsAll(Collection c) {
        return cachedList.containsAll(c);
    }

    public Object[] toArray(Object[] a) {
        return cachedList.toArray(a);
    }


}
