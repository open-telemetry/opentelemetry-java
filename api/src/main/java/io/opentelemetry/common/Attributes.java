/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.common;

import com.google.auto.value.AutoValue;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import javax.annotation.concurrent.Immutable;

/** javadoc me. */
@Immutable
public abstract class Attributes implements Iterable<Entry<String, AttributeValue>> {
  private static final Attributes EMPTY = new EmptyAttributes();

  private static Attributes sortAndFilter(List<Object> data) {
    // note: this is possibly not the most memory-efficient possible implementation, but it works.
    TreeMap<String, AttributeValue> sorter = new TreeMap<>();
    for (int i = 0; i < data.size(); i++) {
      String key = (String) data.get(i++);
      // todo: skip here, favoring the first, or use the TreeMap's built in replacement to favor the
      // last?
      if (!sorter.containsKey(key)) {
        sorter.put(key, (AttributeValue) data.get(i));
      }
    }
    List<Object> sortedData = new ArrayList<>(sorter.size() * 2);
    for (Entry<String, AttributeValue> entry : sorter.entrySet()) {
      sortedData.add(entry.getKey());
      sortedData.add(entry.getValue());
    }
    return new AutoValue_Attributes_ArrayBackedAttributes(sortedData);
  }

  /** javadoc me. */
  public static Attributes empty() {
    return EMPTY;
  }

  /** javadoc me. */
  public static Attributes of(String key, AttributeValue value) {
    return sortAndFilter(Arrays.asList(key, value));
  }

  /** javadoc me. */
  public static Attributes of(
      String key1, AttributeValue value1, String key2, AttributeValue value2) {
    return sortAndFilter(
        Arrays.asList(
            key1, value1,
            key2, value2));
  }

  /** javadoc me. */
  public static Attributes of(
      String key1,
      AttributeValue value1,
      String key2,
      AttributeValue value2,
      String key3,
      AttributeValue value3) {
    return sortAndFilter(
        Arrays.asList(
            key1, value1,
            key2, value2,
            key3, value3));
  }

  /** javadoc me. */
  public static Attributes of(
      String key1,
      AttributeValue value1,
      String key2,
      AttributeValue value2,
      String key3,
      AttributeValue value3,
      String key4,
      AttributeValue value4) {
    return sortAndFilter(
        Arrays.asList(
            key1, value1,
            key2, value2,
            key3, value3,
            key4, value4));
  }

  /** javadoc me. */
  public static Attributes of(
      String key1,
      AttributeValue value1,
      String key2,
      AttributeValue value2,
      String key3,
      AttributeValue value3,
      String key4,
      AttributeValue value4,
      String key5,
      AttributeValue value5) {
    return sortAndFilter(
        Arrays.asList(
            key1, value1,
            key2, value2,
            key3, value3,
            key4, value4,
            key5, value5));
  }

  /** javadoc me. */
  public static Builder newBuilder() {
    return new Builder();
  }

  @AutoValue
  @Immutable
  abstract static class ArrayBackedAttributes extends Attributes {
    abstract List<Object> data();

    ArrayBackedAttributes() {}

    @Override
    public Iterator<Entry<String, AttributeValue>> iterator() {
      return new ArrayIterator();
    }

    private class ArrayIterator implements Iterator<Entry<String, AttributeValue>> {
      private int currentIndex = 0;

      @Override
      public boolean hasNext() {
        return data().size() > currentIndex;
      }

      @Override
      public Entry<String, AttributeValue> next() {
        if (!hasNext()) {
          throw new NoSuchElementException("no more");
        }
        List<Object> data = data();
        return new SimpleImmutableEntry<>(
            (String) data.get(currentIndex++), (AttributeValue) data.get(currentIndex++));
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    }
  }

  /** javadoc me. */
  public static class Builder {
    private final List<Object> data = new ArrayList<>();

    /** javadoc me. */
    public Attributes build() {
      return sortAndFilter(Arrays.asList(data.toArray()));
    }

    /** javadoc me. */
    public Builder addAttribute(String key, String value) {
      data.add(key);
      data.add(AttributeValue.stringAttributeValue(value));
      return this;
    }

    /** javadoc me. */
    public Builder addAttribute(String key, long value) {
      data.add(key);
      data.add(AttributeValue.longAttributeValue(value));
      return this;
    }

    /** javadoc me. */
    public Builder addAttribute(String key, double value) {
      data.add(key);
      data.add(AttributeValue.doubleAttributeValue(value));
      return this;
    }

    /** javadoc me. */
    public Builder addAttribute(String key, boolean value) {
      data.add(key);
      data.add(AttributeValue.booleanAttributeValue(value));
      return this;
    }

    /** javadoc me. */
    public Builder addAttribute(String key, String... value) {
      data.add(key);
      data.add(AttributeValue.arrayAttributeValue(value));
      return this;
    }

    /** javadoc me. */
    public Builder addAttribute(String key, Long... value) {
      data.add(key);
      data.add(AttributeValue.arrayAttributeValue(value));
      return this;
    }

    /** javadoc me. */
    public Builder addAttribute(String key, Double... value) {
      data.add(key);
      data.add(AttributeValue.arrayAttributeValue(value));
      return this;
    }

    /** javadoc me. */
    public Builder addAttribute(String key, Boolean... value) {
      data.add(key);
      data.add(AttributeValue.arrayAttributeValue(value));
      return this;
    }
  }

  private static class EmptyAttributes extends Attributes {

    @Override
    public Iterator<Entry<String, AttributeValue>> iterator() {
      return new Iterator<Entry<String, AttributeValue>>() {
        @Override
        public boolean hasNext() {
          return false;
        }

        @Override
        public Entry<String, AttributeValue> next() {
          throw new NoSuchElementException();
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException("empty");
        }
      };
    }
  }
}
