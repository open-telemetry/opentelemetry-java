/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.trace;

import java.util.Random;

/** Common utilities for tests. */
public final class TestUtils {

  TestUtils() {}

  /**
   * Generates a random {@link TraceId}.
   *
   * @param random seed {@code Random}.
   * @return a {@link TraceId}.
   */
  public static TraceId generateRandomTraceId(Random random) {
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    return TraceId.fromBytes(bytes, 0);
  }

  /**
   * Generates a random {@link SpanId}.
   *
   * @param random seed {@code Random}.
   * @return a {@link SpanId}.
   */
  public static SpanId generateRandomSpanId(Random random) {
    byte[] bytes = new byte[8];
    random.nextBytes(bytes);
    return SpanId.fromBytes(bytes, 0);
  }
}
