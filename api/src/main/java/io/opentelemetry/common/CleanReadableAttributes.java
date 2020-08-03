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

  Boolean getBooleanValue(Object value);

  String getStringValue(Object value);

  Double getDoubleValue(Object value);

  Long getLongValue(Object value);

  List<Boolean> getBooleanArrayValue(Object value);

  List<String> getStringArrayValue(Object value);

  List<Double> getDoubleArrayValue(Object value);

  List<Long> getLongArrayValue(Object value);

  @Nullable
  Object get(String key);

  interface AttributeConsumer {
    void consume(String key, AttributeValue.Type type, Object value);
  }
}
