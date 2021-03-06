package org.basex.query.util.list;

import java.util.*;

import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for items.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class ItemList extends ElementList implements Iterable<Item> {
  /** Element container. */
  private Item[] list;

  /**
   * Default constructor.
   */
  public ItemList() {
    this(1);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param capacity array capacity
   */
  public ItemList(final int capacity) {
    list = new Item[capacity];
  }

  /**
   * Returns the specified element.
   * @param p position
   * @return value
   */
  public Item get(final int p) {
    return list[p];
  }

  /**
   * Adds an element to the array.
   * @param element element to be added
   * @return self reference
   */
  public ItemList add(final Item element) {
    if(size == list.length) resize(newSize());
    list[size++] = element;
    return this;
  }

  /**
   * Adds all elements in the given value to this list.
   * @param value value to add
   * @return self reference
   */
  public ItemList add(final Value value) {
    if(value instanceof Item) return add((Item) value);
    final long n = value.size();
    if(n > Integer.MAX_VALUE - size) throw Util.notExpected(n);
    final int newSize = size + (int) n;
    int sz = size;
    if(newSize > sz) {
      do {
        sz = Array.newSize(sz, factor);
      } while(sz < newSize);
      resize(sz);
    }
    size += value.writeTo(list, size);
    return this;
  }

  /**
   * Resizes the array.
   * @param sz new size
   */
  private void resize(final int sz) {
    final Item[] tmp = new Item[sz];
    System.arraycopy(list, 0, tmp, 0, size);
    list = tmp;
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public Item[] finish() {
    Item[] lst = list;
    final int s = size;
    if(s != lst.length) {
      lst = new Item[s];
      System.arraycopy(list, 0, lst, 0, s);
    }
    list = null;
    return lst;
  }

  /**
   * Returns a value containing the items in this list.
   * @return the value
   */
  public Value value() {
    return value(null);
  }

  /**
   * Returns a value with the given element type containing the items in this list.
   * @param type item type (not checked), may be {@code null}
   * @return the value
   */
  public Value value(final Type type) {
    return ValueBuilder.value(list, size, type);
  }

  /**
   * Sets the item at the given position to the given value.
   * @param pos position
   * @param item new value
   * @return self reference
   */
  public ItemList set(final int pos, final Item item) {
    if(pos >= size) resize(newSize(pos + 1));
    list[pos] = item;
    return this;
  }

  /**
   * Returns an iterator over the items in this list.
   * The list must not be modified after the iterator has been requested.
   * @return the iterator
   */
  public BasicIter<Item> iter() {
    return new BasicIter<Item>(size) {
      @Override
      public Item get(final long i) {
        return list[(int) i];
      }
      @Override
      public Value value() {
        return ItemList.this.value();
      }
    };
  }

  /**
   * Returns the current backing array of this list.
   * @return the backing array
   */
  public Item[] internal() {
    return list;
  }

  @Override
  public Iterator<Item> iterator() {
    return new ArrayIterator<>(list, size);
  }
}
