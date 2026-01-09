/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static io.opentelemetry.sdk.extension.trace.jaeger.sampler.JaegerRemoteSamplerTest.samplerIsType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class JaegerRemoteSamplerIntegrationTest {

  private static final int QUERY_PORT = 16686;
  private static final int COLLECTOR_PORT = 14250;
  private static final int HEALTH_PORT = 14269;
  private static final String SERVICE_NAME_PER_OPERATION = "foo";
  private static final String SERVICE_NAME_RATE_LIMITING = "bar";
  private static final String SERVICE_NAME_DEFAULT_STRATEGY = "foobar";

  @Container
  public static final GenericContainer<?> jaegerContainer =
      new GenericContainer<>("jaegertracing/all-in-one:1.32")
          .withCommand("--sampling.strategies-file=/sampling.json")
          .withExposedPorts(COLLECTOR_PORT, QUERY_PORT, HEALTH_PORT)
          .waitingFor(Wait.forHttp("/").forPort(HEALTH_PORT))
          .withClasspathResourceMapping("sampling.json", "/sampling.json", BindMode.READ_ONLY);

  @Test
  void remoteSampling_perOperation() {
    try (JaegerRemoteSampler remoteSampler =
        JaegerRemoteSampler.builder()
            .setEndpoint("http://127.0.0.1:" + jaegerContainer.getMappedPort(COLLECTOR_PORT))
            .setServiceName(SERVICE_NAME_PER_OPERATION)
            .build()) {
      await()
          .atMost(Duration.ofSeconds(10))
          .untilAsserted(samplerIsType(remoteSampler, PerOperationSampler.class));
      assertThat(remoteSampler.getDescription())
          .contains("op0")
          .contains("op1")
          .contains("op2")
          .doesNotContain("150");
      assertThat(
              remoteSampler.shouldSample(
                  Context.current(),
                  TraceId.getInvalid(),
                  "op0",
                  SpanKind.CLIENT,
                  Attributes.empty(),
                  ImmutableList.of()))
          .isEqualTo(SamplingResult.drop());
    }
  }

  @Test
  void remoteSampling_rateLimiting() {
    try (JaegerRemoteSampler remoteSampler =
        JaegerRemoteSampler.builder()
            .setEndpoint("http://127.0.0.1:" + jaegerContainer.getMappedPort(COLLECTOR_PORT))
            .setServiceName(SERVICE_NAME_RATE_LIMITING)
            .build()) {
      await()
          .atMost(Duration.ofSeconds(10))
          .untilAsserted(samplerIsType(remoteSampler, RateLimitingSampler.class));
      assertThat(remoteSampler.getDescription()).contains("RateLimitingSampler{150.00}");
    }
  }

  @Test
  void remoteSampling_defaultStrategy() {
    try (JaegerRemoteSampler remoteSampler =
        JaegerRemoteSampler.builder()
            .setEndpoint("http://127.0.0.1:" + jaegerContainer.getMappedPort(COLLECTOR_PORT))
            .setServiceName(SERVICE_NAME_DEFAULT_STRATEGY)
            .build()) {
      await()
          .atMost(Duration.ofSeconds(10))
          .untilAsserted(
              () ->
                  assertThat(remoteSampler.getDescription())
                      .contains("TraceIdRatioBased")
                      .contains("0.8"));
    }
  }
}
