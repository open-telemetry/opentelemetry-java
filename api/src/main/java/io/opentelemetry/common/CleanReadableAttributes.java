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
import javax.annotation.Nullable;

/**
 * A read-only container for String-keyed attributes.
 *
 * <p>See {@link Attributes} for the public API implementation.
 */
public interface CleanReadableAttributes {

  int size();

  boolean isEmpty();

  /** Iterates over all the key-value pairs of attributes contained by this instance. */
  void forEach(AttributeConsumer consumer);

  void forEach(TypedAttributeConsumer typedConsumer);

  @Nullable
  Object get(String key);

  interface AttributeConsumer {
    void consume(String key, AttributeType type, Object value);
  }

  interface TypedAttributeConsumer {
    void consumeString(String key, String value);

    void consumeLong(String key, long value);

    void consumeDouble(String key, double value);

    void consumeBoolean(String key, boolean value);

    void consumeStringArray(String key, List<String> value);

    void consumeLongArray(String key, List<Long> value);

    void consumeDoubleArray(String key, List<Double> value);

    void consumeBooleanArray(String key, List<Boolean> value);
  }
}
