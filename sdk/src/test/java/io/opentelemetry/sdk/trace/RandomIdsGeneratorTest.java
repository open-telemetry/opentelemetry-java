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

package io.opentelemetry.sdk.trace;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.trace.RandomIdsGenerator.RandomSupplier;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class RandomIdsGeneratorTest {

  @Mock private Random random;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void defaults() {
    RandomIdsGenerator generator = new RandomIdsGenerator();

    // Can't assert values but can assert they're valid.
    TraceId traceId = generator.generateTraceId();
    assertThat(traceId).isNotEqualTo(TraceId.getInvalid());

    SpanId spanId = generator.generateSpanId();
    assertThat(spanId).isNotEqualTo(SpanId.getInvalid());
  }

  @Test
  public void customRandom() {
    RandomIdsGenerator generator =
        new RandomIdsGenerator(
            new RandomSupplier() {
              @Override
              public Random get() {
                return random;
              }
            });

    when(random.nextLong()).thenReturn(1L).thenReturn(2L);
    TraceId traceId = generator.generateTraceId();
    assertThat(traceId.toLowerBase16()).isEqualTo("00000000000000010000000000000002");

    when(random.nextLong()).thenReturn(3L);
    SpanId spanId = generator.generateSpanId();
    assertThat(spanId.toLowerBase16()).isEqualTo("0000000000000003");
  }

  @Test
  public void ignoresInvalid() {
    RandomIdsGenerator generator =
        new RandomIdsGenerator(
            new RandomSupplier() {
              @Override
              public Random get() {
                return random;
              }
            });

    // First two zeros skipped
    when(random.nextLong()).thenReturn(0L).thenReturn(0L).thenReturn(0L).thenReturn(4L);
    TraceId traceId = generator.generateTraceId();
    assertThat(traceId.toLowerBase16()).isEqualTo("00000000000000000000000000000004");

    when(random.nextLong()).thenReturn(0L).thenReturn(5L);
    SpanId spanId = generator.generateSpanId();
    assertThat(spanId.toLowerBase16()).isEqualTo("0000000000000005");
  }
}
