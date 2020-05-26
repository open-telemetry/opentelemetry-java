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

import io.opentelemetry.sdk.trace.IdsGenerator;
import io.opentelemetry.sdk.trace.RandomIdsGenerator;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Generates tracing ids compatible with the AWS X-Ray tracing service. In the X-Ray system the
 * first 32 bits of the trace id are the Unix epoch time in secords. Spans (AWS calls them segments)
 * submit with trace id timestamps outside of the last 30 days are rejected.
 *
 * @see <a
 *     href="https://docs.aws.amazon.com/xray/latest/devguide/xray-api-sendingdata.html#xray-api-traceids">Generating
 *     Trace IDs</a>
 */
public class AwsXRayIdsGenerator implements IdsGenerator {

  private static final RandomIdsGenerator RANDOM_IDS_GENERATOR = new RandomIdsGenerator();

  @Override
  public SpanId generateSpanId() {
    return RANDOM_IDS_GENERATOR.generateSpanId();
  }

  @Override
  public TraceId generateTraceId() {
    // hi - 4 bytes timestamp, 4 bytes random
    // low - 8 bytes random.
    // Since we include timestamp, impossible to be invalid.

    Random random = ThreadLocalRandom.current();
    long timestampSecs = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    long hiRandom = random.nextInt() & 0xFFFFFFFFL;

    long lowRandom = random.nextLong();

    return new TraceId(timestampSecs << 32 | hiRandom, lowRandom);
  }
}
