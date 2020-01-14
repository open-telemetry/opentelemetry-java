/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.context.propagation;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Formatter to serializing and deserializing a value with into a binary format.
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface BinaryFormat<V> {

  /**
   * Serializes the {@code value} into the on-the-wire representation.
   *
   * @param value the value to serialize.
   * @return the on-the-wire representation of a {@code value}.
   * @since 0.1.0
   */
  byte[] toByteArray(V value);

  /**
   * Creates a value from the given on-the-wire encoded representation.
   *
   * <p>If the value could not be parsed, the underlying implementation will decide to return ether
   * an empty value, an invalid value, or a valid value.
   *
   * @param bytes on-the-wire representation of the value.
   * @return a value deserialized from {@code bytes}.
   * @since 0.1.0
   */
  V fromByteArray(byte[] bytes);
}
