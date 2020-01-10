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

package io.opentelemetry.sdk.contrib.trace.aws;

import static org.junit.Assert.assertTrue;

import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import org.junit.Test;

/** Unit tests for {@link AwsXRayIdsGenerator}. */
public class AwsXRayIdsGeneratorTest {

  @Test
  public void shouldGenerateValidIds() {
    AwsXRayIdsGenerator generator = new AwsXRayIdsGenerator();
    TraceId traceId = generator.generateTraceId();
    assertTrue(traceId.isValid());
    SpanId spanId = generator.generateSpanId();
    assertTrue(spanId.isValid());
  }

  @Test
  public void shouldGenerateTraceIdsWithTimestampsWithAllowedXrayTimeRange() {
    AwsXRayIdsGenerator generator = new AwsXRayIdsGenerator();
    TraceId traceId = generator.generateTraceId();
    Long unixSeconds = Long.valueOf(traceId.toLowerBase16().substring(0, 8), 16);
    long ts = unixSeconds.longValue() * 1000L;
    long currentTs = System.currentTimeMillis();
    assertTrue(ts <= currentTs);
    long month = 86400000L * 30L;
    assertTrue(ts > currentTs - month);
  }
}
