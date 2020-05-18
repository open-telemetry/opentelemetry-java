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
import io.opentelemetry.common.AttributeKey.BooleanArrayValuedKey;
import io.opentelemetry.common.AttributeKey.BooleanValuedKey;
import io.opentelemetry.common.AttributeKey.DoubleArrayValuedKey;
import io.opentelemetry.common.AttributeKey.DoubleValuedKey;
import io.opentelemetry.common.AttributeKey.LongArrayValuedKey;
import io.opentelemetry.common.AttributeKey.LongValuedKey;
import io.opentelemetry.common.AttributeKey.StringArrayValuedKey;
import io.opentelemetry.common.AttributeKey.StringValuedKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jdk.nashorn.internal.ir.annotations.Immutable;

/**
 * Commentary on allocations:
 *
 * <p>Per entry, we have:
 *
 * <p>1 allocation for the AttributeKey
 *
 * <p>1 primitive box for the 3 primitive types
 *
 * <p>Note: the attributeKey, for a lot of instrumentation, could be stored as a static instance in
 * the instrumenting class, which means this allocation can be "free" for the most common usages.
 *
 * <p>See the documentation on the {@link MapBasedAttributes} class to compare with the existing
 * implementation in the API.
 */
@AutoValue
@Immutable
public abstract class ArrayBasedAttributes implements Attributes {
  abstract List<Object> keysAndValues();

  @Override
  public Set<AttributeKey> getKeys() {
    Set<AttributeKey> results = new HashSet<>(keysAndValues().size() / 2);
    for (int i = 0; i < keysAndValues().size(); i++) {
      results.add((AttributeKey) keysAndValues().get(i++));
    }
    return results;
  }

  @Override
  public boolean getValue(BooleanValuedKey key) {
    return find(key, keysAndValues());
  }

  @Override
  public String getValue(StringValuedKey key) {
    return find(key, keysAndValues());
  }

  @Override
  public long getValue(LongValuedKey key) {
    return find(key, keysAndValues());
  }

  @Override
  public double getValue(DoubleValuedKey key) {
    return find(key, keysAndValues());
  }

  @Override
  public List<String> getValue(StringArrayValuedKey key) {
    return find(key, keysAndValues());
  }

  @Override
  public List<Double> getValue(DoubleArrayValuedKey key) {
    return find(key, keysAndValues());
  }

  @Override
  public List<Long> getValue(LongArrayValuedKey key) {
    return find(key, keysAndValues());
  }

  @Override
  public List<Boolean> getValue(BooleanArrayValuedKey key) {
    return find(key, keysAndValues());
  }

  @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
  private static <T> T find(AttributeKey key, List<Object> keysAndValues) {
    for (int i = 0; i < keysAndValues.size(); i++) {
      AttributeKey attributeKey = (AttributeKey) keysAndValues.get(i++);
      if (key.equals(attributeKey)) {
        return (T) keysAndValues.get(i);
      }
    }
    throw new IllegalStateException("key not found" + key);
  }

  public static ArrayBasedAttributes.Builder newBuilder() {
    return new ArrayBasedAttributes.Builder();
  }

  public static class Builder {

    private final List<Object> data = new ArrayList<>();

    public Attributes build() {
      return new AutoValue_ArrayBasedAttributes(Collections.unmodifiableList(data));
    }

    /** Doc me. */
    public ArrayBasedAttributes.Builder put(AttributeKey.BooleanValuedKey key, boolean value) {
      data.add(key);
      data.add(value);
      return this;
    }

    /** Doc me. */
    public ArrayBasedAttributes.Builder put(AttributeKey.StringValuedKey key, String value) {
      data.add(key);
      data.add(value);
      return this;
    }

    /** Doc me. */
    public ArrayBasedAttributes.Builder put(AttributeKey.LongValuedKey key, long value) {
      data.add(key);
      data.add(value);
      return this;
    }

    /** Doc me. */
    public ArrayBasedAttributes.Builder put(AttributeKey.DoubleValuedKey key, double value) {
      data.add(key);
      data.add(value);
      return this;
    }

    /** Doc me. */
    public ArrayBasedAttributes.Builder put(
        AttributeKey.StringArrayValuedKey key, List<String> value) {
      data.add(key);
      data.add(value);
      return this;
    }

    /** Doc me. */
    public ArrayBasedAttributes.Builder put(AttributeKey.LongArrayValuedKey key, List<Long> value) {
      data.add(key);
      data.add(value);
      return this;
    }

    /** Doc me. */
    public ArrayBasedAttributes.Builder put(
        AttributeKey.DoubleArrayValuedKey key, List<Double> value) {
      data.add(key);
      data.add(value);
      return this;
    }

    /** Doc me. */
    public ArrayBasedAttributes.Builder put(
        AttributeKey.BooleanArrayValuedKey key, List<Boolean> value) {
      data.add(key);
      data.add(value);
      return this;
    }
  }
}
