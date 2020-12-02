/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.context;

import java.util.Arrays;
import javax.annotation.Nullable;

/**
 * A persistent (copy-on-write) hash tree/trie. Collisions are handled linearly. Delete is not
 * supported, but replacement is. The implementation favors simplicity and low memory allocation
 * during insertion. Although the asymptotics are good, it is optimized for small sizes like less
 * than 20; "unbelievably large" would be 100.
 *
 * <p>Inspired by popcnt-based compression seen in Ideal Hash Trees, Phil Bagwell (2000). The rest
 * of the implementation is ignorant of/ignores the paper.
 */
final class PersistentHashArrayMappedTrie {

  private PersistentHashArrayMappedTrie() {}

  /** Returns the value with the specified key, or {@code null} if it does not exist. */
  static Object get(@Nullable Node root, ContextKey<?> key) {
    if (root == null) {
      return null;
    }
    return root.get(key, System.identityHashCode(key), 0);
  }

  /** Returns a new trie where the key is set to the specified value. */
  static Node put(@Nullable Node root, ContextKey<?> key, Object value) {
    if (root == null) {
      return new Leaf(key, value);
    }
    return root.put(key, value, System.identityHashCode(key), 0);
  }

  // Not actually annotated to avoid depending on guava
  // @VisibleForTesting
  static final class Leaf implements Node {
    private final ContextKey<?> key;
    private final Object value;

    public Leaf(ContextKey<?> key, Object value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public int size() {
      return 1;
    }

    @Override
    public Object get(ContextKey<?> key, int hash, int bitsConsumed) {
      if (this.key == key) {
        return value;
      } else {
        return null;
      }
    }

    @Override
    public Node put(ContextKey<?> key, Object value, int hash, int bitsConsumed) {
      int thisHash = System.identityHashCode(this.key);
      if (thisHash != hash) {
        // Insert
        return CompressedIndex.combine(new Leaf(key, value), hash, this, thisHash, bitsConsumed);
      } else if (this.key == key) {
        // Replace
        return new Leaf(key, value);
      } else {
        // Hash collision
        return new CollisionLeaf(this.key, this.value, key, value);
      }
    }

    @Override
    public String toString() {
      return String.format("Leaf(key=%s value=%s)", key, value);
    }
  }

  // Not actually annotated to avoid depending on guava
  // @VisibleForTesting
  static final class CollisionLeaf implements Node {
    // All keys must have same hash, but not have the same reference
    private final ContextKey<?>[] keys;
    private final Object[] values;

    // Not actually annotated to avoid depending on guava
    // @VisibleForTesting
    @SuppressWarnings("unchecked")
    CollisionLeaf(ContextKey<?> key1, Object value1, ContextKey<?> key2, Object value2) {
      this((ContextKey<?>[]) new Object[] {key1, key2}, new Object[] {value1, value2});
      assert key1 != key2;
      assert System.identityHashCode(key1) == System.identityHashCode(key2);
    }

    private CollisionLeaf(ContextKey<?>[] keys, Object[] values) {
      this.keys = keys;
      this.values = values;
    }

    @Override
    public int size() {
      return values.length;
    }

    @Override
    public Object get(ContextKey<?> key, int hash, int bitsConsumed) {
      for (int i = 0; i < keys.length; i++) {
        if (keys[i] == key) {
          return values[i];
        }
      }
      return null;
    }

    @Override
    public Node put(ContextKey<?> key, Object value, int hash, int bitsConsumed) {
      int thisHash = System.identityHashCode(keys[0]);
      int keyIndex;
      if (thisHash != hash) {
        // Insert
        return CompressedIndex.combine(new Leaf(key, value), hash, this, thisHash, bitsConsumed);
      } else if ((keyIndex = indexOfKey(key)) != -1) {
        // Replace
        ContextKey<?>[] newKeys = Arrays.copyOf(keys, keys.length);
        Object[] newValues = Arrays.copyOf(values, keys.length);
        newKeys[keyIndex] = key;
        newValues[keyIndex] = value;
        return new CollisionLeaf(newKeys, newValues);
      } else {
        // Yet another hash collision
        ContextKey<?>[] newKeys = Arrays.copyOf(keys, keys.length + 1);
        Object[] newValues = Arrays.copyOf(values, keys.length + 1);
        newKeys[keys.length] = key;
        newValues[keys.length] = value;
        return new CollisionLeaf(newKeys, newValues);
      }
    }

    // -1 if not found
    private int indexOfKey(ContextKey<?> key) {
      for (int i = 0; i < keys.length; i++) {
        if (keys[i] == key) {
          return i;
        }
      }
      return -1;
    }

    @Override
    public String toString() {
      StringBuilder valuesSb = new StringBuilder();
      valuesSb.append("CollisionLeaf(");
      for (int i = 0; i < values.length; i++) {
        valuesSb.append("(key=").append(keys[i]).append(" value=").append(values[i]).append(") ");
      }
      return valuesSb.append(")").toString();
    }
  }

  // Not actually annotated to avoid depending on guava
  // @VisibleForTesting
  static final class CompressedIndex implements Node {
    private static final int BITS = 5;
    private static final int BITS_MASK = 0x1F;

    final int bitmap;
    final Node[] values;
    private final int size;

    private CompressedIndex(int bitmap, Node[] values, int size) {
      this.bitmap = bitmap;
      this.values = values;
      this.size = size;
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public Object get(ContextKey<?> key, int hash, int bitsConsumed) {
      int indexBit = indexBit(hash, bitsConsumed);
      if ((bitmap & indexBit) == 0) {
        return null;
      }
      int compressedIndex = compressedIndex(indexBit);
      return values[compressedIndex].get(key, hash, bitsConsumed + BITS);
    }

    @Override
    public Node put(ContextKey<?> key, Object value, int hash, int bitsConsumed) {
      int indexBit = indexBit(hash, bitsConsumed);
      int compressedIndex = compressedIndex(indexBit);
      if ((bitmap & indexBit) == 0) {
        // Insert
        int newBitmap = bitmap | indexBit;
        @SuppressWarnings("unchecked")
        Node[] newValues = new Node[values.length + 1];
        System.arraycopy(values, 0, newValues, 0, compressedIndex);
        newValues[compressedIndex] = new Leaf(key, value);
        System.arraycopy(
            values,
            compressedIndex,
            newValues,
            compressedIndex + 1,
            values.length - compressedIndex);
        return new CompressedIndex(newBitmap, newValues, size() + 1);
      } else {
        // Replace
        Node[] newValues = Arrays.copyOf(values, values.length);
        newValues[compressedIndex] =
            values[compressedIndex].put(key, value, hash, bitsConsumed + BITS);
        int newSize = size();
        newSize += newValues[compressedIndex].size();
        newSize -= values[compressedIndex].size();
        return new CompressedIndex(bitmap, newValues, newSize);
      }
    }

    static Node combine(Node node1, int hash1, Node node2, int hash2, int bitsConsumed) {
      assert hash1 != hash2;
      int indexBit1 = indexBit(hash1, bitsConsumed);
      int indexBit2 = indexBit(hash2, bitsConsumed);
      if (indexBit1 == indexBit2) {
        Node node = combine(node1, hash1, node2, hash2, bitsConsumed + BITS);
        @SuppressWarnings("unchecked")
        Node[] values = new Node[] {node};
        return new CompressedIndex(indexBit1, values, node.size());
      } else {
        // Make node1 the smallest
        if (uncompressedIndex(hash1, bitsConsumed) > uncompressedIndex(hash2, bitsConsumed)) {
          Node nodeCopy = node1;
          node1 = node2;
          node2 = nodeCopy;
        }
        @SuppressWarnings("unchecked")
        Node[] values = new Node[] {node1, node2};
        return new CompressedIndex(indexBit1 | indexBit2, values, node1.size() + node2.size());
      }
    }

    @Override
    public String toString() {
      StringBuilder valuesSb = new StringBuilder();
      valuesSb
          .append("CompressedIndex(")
          .append(String.format("bitmap=%s ", Integer.toBinaryString(bitmap)));
      for (Node value : values) {
        valuesSb.append(value).append(" ");
      }
      return valuesSb.append(")").toString();
    }

    private int compressedIndex(int indexBit) {
      return Integer.bitCount(bitmap & (indexBit - 1));
    }

    private static int uncompressedIndex(int hash, int bitsConsumed) {
      return (hash >>> bitsConsumed) & BITS_MASK;
    }

    private static int indexBit(int hash, int bitsConsumed) {
      int uncompressedIndex = uncompressedIndex(hash, bitsConsumed);
      return 1 << uncompressedIndex;
    }
  }

  static final class RootNode implements Node {

    @Override
    public Object get(ContextKey<?> key, int hash, int bitsConsumed) {
      return null;
    }

    @Override
    public Node put(ContextKey<?> key, Object value, int hash, int bitsConsumed) {
      return PersistentHashArrayMappedTrie.put(null, key, value);
    }

    @Override
    public int size() {
      return 0;
    }
  }

  interface Node extends Context {
    int size();

    Object get(ContextKey<?> key, int hash, int bitsConsumed);

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    default <V> V get(ContextKey<V> key) {
      return (V) get(key, System.identityHashCode(key), 0);
    }

    Node put(ContextKey<?> key, Object value, int hash, int bitsConsumed);

    @Override
    default <V> Context with(ContextKey<V> k1, V v1) {
      return put(k1, v1, System.identityHashCode(k1), 0);
    }
  }
}
