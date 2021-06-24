/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.google.common.io.ByteStreams;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class AwsXrayRemoteSamplerTest {

  private static final byte[] RESPONSE_1;
  private static final byte[] RESPONSE_2;

  static {
    try {
      RESPONSE_1 =
          ByteStreams.toByteArray(
              requireNonNull(
                  AwsXrayRemoteSamplerTest.class.getResourceAsStream(
                      "/test-sampling-rules-response-1.json")));
      RESPONSE_2 =
          ByteStreams.toByteArray(
              requireNonNull(
                  AwsXrayRemoteSamplerTest.class.getResourceAsStream(
                      "/test-sampling-rules-response-2.json")));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static final AtomicReference<byte[]> response = new AtomicReference<>();

  private static final String TRACE_ID = TraceId.fromLongs(1, 2);

  @RegisterExtension
  public static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
              "/GetSamplingRules",
              (ctx, req) -> {
                byte[] response = AwsXrayRemoteSamplerTest.response.get();
                if (response == null) {
                  // Error out until the test configures a response, the sampler will use the
                  // initial
                  // sampler in the meantime.
                  return HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, response);
              });
        }
      };

  private AwsXrayRemoteSampler sampler;

  @BeforeEach
  void setUp() {
    sampler =
        AwsXrayRemoteSampler.newBuilder(Resource.empty())
            .setInitialSampler(Sampler.alwaysOn())
            .setEndpoint(server.httpUri().toString())
            .setPollingInterval(Duration.ofMillis(10))
            .build();
  }

  @AfterEach
  void tearDown() {
    sampler.close();
    response.set(null);
  }

  @Test
  void getAndUpdate() {
    // Initial Sampler allows all.
    assertThat(
            sampler
                .shouldSample(
                    Context.root(),
                    TRACE_ID,
                    "cat-service",
                    SpanKind.SERVER,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    assertThat(
            sampler
                .shouldSample(
                    Context.root(),
                    TRACE_ID,
                    "dog-service",
                    SpanKind.SERVER,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);

    response.set(RESPONSE_1);

    // cat-service allowed, others dropped
    await()
        .untilAsserted(
            () -> {
              assertThat(
                      sampler
                          .shouldSample(
                              Context.root(),
                              TRACE_ID,
                              "cat-service",
                              SpanKind.SERVER,
                              Attributes.empty(),
                              Collections.emptyList())
                          .getDecision())
                  .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
              assertThat(
                      sampler
                          .shouldSample(
                              Context.root(),
                              TRACE_ID,
                              "dog-service",
                              SpanKind.SERVER,
                              Attributes.empty(),
                              Collections.emptyList())
                          .getDecision())
                  .isEqualTo(SamplingDecision.DROP);
            });

    response.set(RESPONSE_2);

    // cat-service dropped, others allowed
    await()
        .untilAsserted(
            () -> {
              assertThat(
                      sampler
                          .shouldSample(
                              Context.root(),
                              TRACE_ID,
                              "cat-service",
                              SpanKind.SERVER,
                              Attributes.empty(),
                              Collections.emptyList())
                          .getDecision())
                  .isEqualTo(SamplingDecision.DROP);
              assertThat(
                      sampler
                          .shouldSample(
                              Context.root(),
                              TRACE_ID,
                              "dog-service",
                              SpanKind.SERVER,
                              Attributes.empty(),
                              Collections.emptyList())
                          .getDecision())
                  .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
            });
  }

  @Test
  void initialSampler() {
    assertThat(sampler.getDescription()).isEqualTo("AwsXrayRemoteSampler{AlwaysOnSampler}");
  }
}
