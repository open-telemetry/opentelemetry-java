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

import io.opentelemetry.common.AttributeKeyImpl.BooleanArrayKey;
import io.opentelemetry.common.AttributeKeyImpl.BooleanKey;
import io.opentelemetry.common.AttributeKeyImpl.DoubleArrayKey;
import io.opentelemetry.common.AttributeKeyImpl.DoubleKey;
import io.opentelemetry.common.AttributeKeyImpl.LongArrayKey;
import io.opentelemetry.common.AttributeKeyImpl.LongKey;
import io.opentelemetry.common.AttributeKeyImpl.StringArrayKey;
import io.opentelemetry.common.AttributeKeyImpl.StringKey;
import java.util.List;

/**
 * Creation methods for {@link AttributeKey} implementations.
 *
 * @see Attributes
 */
public class AttributesKeys {
  private AttributesKeys() {}

  /** Create a new AttributeKey for String valued attributes. */
  public static AttributeKey<String> stringKey(String key) {
    return new StringKey(key);
  }

  /** Create a new AttributeKey for Boolean valued attributes. */
  public static AttributeKey<Boolean> booleanKey(String key) {
    return new BooleanKey(key);
  }

  /** Create a new AttributeKey for Long valued attributes. */
  public static AttributeKey<Long> longKey(String key) {
    return new LongKey(key);
  }

  /** Create a new AttributeKey for Double valued attributes. */
  public static AttributeKey<Double> doubleKey(String key) {
    return new DoubleKey(key);
  }

  /** Create a new AttributeKey for List&lt;String> valued attributes. */
  public static AttributeKey<List<String>> stringArrayKey(String key) {
    return new StringArrayKey(key);
  }

  /** Create a new AttributeKey for List&lt;Boolean> valued attributes. */
  public static AttributeKey<List<Boolean>> booleanArrayKey(String key) {
    return new BooleanArrayKey(key);
  }

  /** Create a new AttributeKey for List&lt;Long> valued attributes. */
  public static AttributeKey<List<Long>> longArrayKey(String key) {
    return new LongArrayKey(key);
  }

  /** Create a new AttributeKey for List&lt;Double> valued attributes. */
  public static AttributeKey<List<Double>> doubleArrayKey(String key) {
    return new DoubleArrayKey(key);
  }
}
