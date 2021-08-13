/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.trace.IdGenerator;
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
 * @deprecated Use the AwsXrayIdGenerator provided in the <a
 *     href="https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/aws-xray">
 *     io.opentelemetry.contrib:opentelemetry-aws-xray</a> artifact instead of this one.
 */
@Deprecated
public final class AwsXrayIdGenerator implements IdGenerator {

  private static final AwsXrayIdGenerator INSTANCE = new AwsXrayIdGenerator();

  private static final IdGenerator RANDOM_ID_GENERATOR = IdGenerator.random();

  /** Returns a singleton instance of {@link AwsXrayIdGenerator}. */
  public static AwsXrayIdGenerator getInstance() {
    return INSTANCE;
  }

  @Override
  public String generateSpanId() {
    return RANDOM_ID_GENERATOR.generateSpanId();
  }

  @Override
  public String generateTraceId() {
    // hi - 4 bytes timestamp, 4 bytes random
    // low - 8 bytes random.
    // Since we include timestamp, impossible to be invalid.

    Random random = ThreadLocalRandom.current();
    long timestampSecs = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    long hiRandom = random.nextInt() & 0xFFFFFFFFL;

    long lowRandom = random.nextLong();

    return TraceId.fromLongs(timestampSecs << 32 | hiRandom, lowRandom);
  }

  private AwsXrayIdGenerator() {}
}
