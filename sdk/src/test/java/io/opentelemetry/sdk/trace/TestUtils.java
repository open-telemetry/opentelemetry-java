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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.UUID;

/** Common utilities for unit tests. */
public final class TestUtils {

  /**
   * Returns a random {@link TraceId}.
   *
   * @return a random {@link TraceId}.
   */
  public static TraceId generateRandomTraceId() {
    return TraceId.fromLowerBase16(UUID.randomUUID().toString().replace("-", ""), 0);
  }

  /**
   * Returns a random {@link SpanId}.
   *
   * @return a random {@link SpanId}.
   */
  public static SpanId generateRandomSpanId() {
    return SpanId.fromLowerBase16(UUID.randomUUID().toString().replace("-", ""), 0);
  }

  private TestUtils() {}
}
