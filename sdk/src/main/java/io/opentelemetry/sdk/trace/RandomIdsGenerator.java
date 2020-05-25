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

import static java.util.Objects.requireNonNull;

import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** The default {@link IdsGenerator} which generates IDs as random numbers. */
public final class RandomIdsGenerator implements IdsGenerator {

  private static final long INVALID_ID = 0;

  /**
   * A supplier of the {@link Random} that will be used to generate random IDs. This is a functional
   * interface and can be safely initialized as a lambda.
   */
  public interface RandomSupplier {
    /** Returns the {@link Random} to use for generating IDs. */
    Random get();
  }

  /** Creates a {@link RandomIdsGenerator} which uses {@link ThreadLocalRandom} to generate IDs. */
  public RandomIdsGenerator() {
    this(
        new RandomSupplier() {
          @Override
          public Random get() {
            return ThreadLocalRandom.current();
          }

          @Override
          public String toString() {
            return "ThreadLocalRandom";
          }
        });
  }

  /**
   * Creates a {@link RandomIdsGenerator} which uses the provided {@link RandomSupplier} to generate
   * IDs.
   */
  public RandomIdsGenerator(RandomSupplier randomSupplier) {
    this.randomSupplier = requireNonNull(randomSupplier, "randomSupplier");
  }

  private final RandomSupplier randomSupplier;

  @Override
  public SpanId generateSpanId() {
    long id;
    Random random = randomSupplier.get();
    do {
      id = random.nextLong();
    } while (id == INVALID_ID);
    return new SpanId(id);
  }

  @Override
  public TraceId generateTraceId() {
    long idHi;
    long idLo;
    Random random = randomSupplier.get();
    do {
      idHi = random.nextLong();
      idLo = random.nextLong();
    } while (idHi == INVALID_ID && idLo == INVALID_ID);
    return new TraceId(idHi, idLo);
  }
}
