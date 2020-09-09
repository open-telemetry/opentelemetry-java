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
 * Convenience interface for consuming {@link ReadableAttributes}.
 *
 * @since 0.9.0
 */
public interface AttributeConsumer {
  void consume(StringKey key, String value);

  void consume(BooleanKey key, boolean value);

  void consume(DoubleKey key, double value);

  void consume(LongKey key, long value);

  void consume(StringArrayKey key, List<String> value);

  void consume(BooleanArrayKey key, List<Boolean> value);

  void consume(DoubleArrayKey key, List<Double> value);

  void consume(LongArrayKey key, List<Long> value);
}
