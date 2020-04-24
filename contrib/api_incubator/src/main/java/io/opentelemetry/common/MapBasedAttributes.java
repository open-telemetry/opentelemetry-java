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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import jdk.nashorn.internal.ir.annotations.Immutable;

/**
 * Commentary on allocations:
 *
 * <p>Per entry, we have:
 *
 * <p>1 allocation for the AttributeKey
 *
 * <p>1 Map.Entry (this is like 2 extra allocations)
 *
 * <p>1 primitive box for the 3 primitive types
 *
 * <p>Note: the attributeKey, for a lot of instrumentation, could be stored as a static instance in
 * the instrumenting class, which means this allocation can be "free" for the most common usages.
 */
@AutoValue
@Immutable
public abstract class MapBasedAttributes implements Attributes {

  abstract Map<AttributeKey, Object> getData();

  @Override
  public Set<AttributeKey> getKeys() {
    return getData().keySet();
  }

  @Override
  public boolean getValue(AttributeKey.BooleanValuedKey key) {
    return (boolean) getData().get(key);
  }

  @Override
  public String getValue(AttributeKey.StringValuedKey key) {
    return (String) getData().get(key);
  }

  @Override
  public long getValue(AttributeKey.LongValuedKey key) {
    return (long) getData().get(key);
  }

  @Override
  public double getValue(AttributeKey.DoubleValuedKey key) {
    return (double) getData().get(key);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private final Map<AttributeKey, Object> data = new ConcurrentHashMap<>();

    public Attributes build() {
      return new AutoValue_MapBasedAttributes(Collections.unmodifiableMap(data));
    }

    public Builder put(AttributeKey.BooleanValuedKey key, boolean value) {
      data.put(key, value);
      return this;
    }

    public Builder put(AttributeKey.StringValuedKey key, String value) {
      data.put(key, value);
      return this;
    }

    public Builder put(AttributeKey.LongValuedKey key, long value) {
      data.put(key, value);
      return this;
    }

    public Builder put(AttributeKey.DoubleValuedKey key, double value) {
      data.put(key, value);
      return this;
    }
  }
}
