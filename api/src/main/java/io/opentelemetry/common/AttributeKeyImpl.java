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

import java.util.List;

public abstract class AttributeKeyImpl<T> implements AttributeKey<T> {
  private final String key;

  AttributeKeyImpl(String key) {
    this.key = key;
  }

  @Override
  public String get() {
    return key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttributeKeyImpl)) {
      return false;
    }

    AttributeKeyImpl<?> that = (AttributeKeyImpl<?>) o;

    return key != null ? key.equals(that.key) : that.key == null;
  }

  @Override
  public int hashCode() {
    return key != null ? key.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "AttributeKeyImpl{" + "key='" + key + '\'' + '}';
  }

  public static StringKey stringKey(String key) {
    return new StringKey(key);
  }

  public static class StringKey extends AttributeKeyImpl<String> {
    private StringKey(String key) {
      super(key);
    }

    @Override
    public AttributeValue.Type getType() {
      return AttributeValue.Type.STRING;
    }
  }

  public static BooleanKey booleanKey(String key) {
    return new BooleanKey(key);
  }

  public static class BooleanKey extends AttributeKeyImpl<Boolean> {
    private BooleanKey(String key) {
      super(key);
    }

    @Override
    public AttributeValue.Type getType() {
      return AttributeValue.Type.BOOLEAN;
    }
  }

  public static LongKey longKey(String key) {
    return new LongKey(key);
  }

  public static class LongKey extends AttributeKeyImpl<Long> {
    private LongKey(String key) {
      super(key);
    }

    @Override
    public AttributeValue.Type getType() {
      return AttributeValue.Type.LONG;
    }
  }

  public static DoubleKey doubleKey(String key) {
    return new DoubleKey(key);
  }

  public static class DoubleKey extends AttributeKeyImpl<Double> {
    private DoubleKey(String key) {
      super(key);
    }

    @Override
    public AttributeValue.Type getType() {
      return AttributeValue.Type.DOUBLE;
    }
  }

  public static StringArrayKey stringArrayKey(String key) {
    return new StringArrayKey(key);
  }

  public static class StringArrayKey extends AttributeKeyImpl<List<String>> {
    private StringArrayKey(String key) {
      super(key);
    }

    @Override
    public AttributeValue.Type getType() {
      return AttributeValue.Type.STRING_ARRAY;
    }
  }

  public static BooleanArrayKey booleanArrayKey(String key) {
    return new BooleanArrayKey(key);
  }

  public static class BooleanArrayKey extends AttributeKeyImpl<List<Boolean>> {
    private BooleanArrayKey(String key) {
      super(key);
    }

    @Override
    public AttributeValue.Type getType() {
      return AttributeValue.Type.BOOLEAN_ARRAY;
    }
  }

  public static LongArrayKey longArrayKey(String key) {
    return new LongArrayKey(key);
  }

  public static class LongArrayKey extends AttributeKeyImpl<List<Long>> {
    private LongArrayKey(String key) {
      super(key);
    }

    @Override
    public AttributeValue.Type getType() {
      return AttributeValue.Type.LONG_ARRAY;
    }
  }

  public static DoubleArrayKey doubleArrayKey(String key) {
    return new DoubleArrayKey(key);
  }

  public static class DoubleArrayKey extends AttributeKeyImpl<List<Double>> {
    private DoubleArrayKey(String key) {
      super(key);
    }

    @Override
    public AttributeValue.Type getType() {
      return AttributeValue.Type.DOUBLE_ARRAY;
    }
  }
}
