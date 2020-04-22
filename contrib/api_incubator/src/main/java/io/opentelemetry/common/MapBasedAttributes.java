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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MapBasedAttributes implements Attributes {

  private final Map<AttributeKey, Object> data;

  private MapBasedAttributes(Map<AttributeKey, Object> data) {
    this.data = data;
  }

  @Override
  public Set<AttributeKey> getKeys() {
    return data.keySet();
  }

  @Override
  public boolean getBooleanValue(BooleanValuedKey key) {
    return (boolean) data.get(key);
  }

  @Override
  public String getStringValue(StringValuedKey key) {
    return (String) data.get(key);
  }

  @Override
  public long getLongValue(LongValuedKey key) {
    return (long) data.get(key);
  }

  @Override
  public double getDoubleValue(DoubleValuedKey key) {
    return (double) data.get(key);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private final Map<AttributeKey, Object> data = new ConcurrentHashMap<>();

    public Attributes build() {
      return new MapBasedAttributes(Collections.unmodifiableMap(data));
    }

    public Builder put(BooleanValuedKey key, boolean value) {
      data.put(key, value);
      return this;
    }

    public Builder put(StringValuedKey key, String value) {
      data.put(key, value);
      return this;
    }

    public Builder put(LongValuedKey key, long value) {
      data.put(key, value);
      return this;
    }

    public Builder put(DoubleValuedKey key, double value) {
      data.put(key, value);
      return this;
    }
  }
}
