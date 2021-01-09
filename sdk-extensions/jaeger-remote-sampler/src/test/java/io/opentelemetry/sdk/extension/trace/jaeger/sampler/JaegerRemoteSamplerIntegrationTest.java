/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static io.opentelemetry.sdk.extension.trace.jaeger.sampler.JaegerRemoteSamplerTest.samplerIsType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
  private static final String SERVICE_NAME = "E2E-test";
  private static final String SERVICE_NAME_RATE_LIMITING = "bar";

  @Container
  public static GenericContainer<?> jaegerContainer =
      new GenericContainer<>("ghcr.io/open-telemetry/java-test-containers:jaeger")
          .withCommand("--sampling.strategies-file=/sampling.json")
          .withExposedPorts(COLLECTOR_PORT, QUERY_PORT, HEALTH_PORT)
          .waitingFor(Wait.forHttp("/").forPort(HEALTH_PORT))
          .withClasspathResourceMapping("sampling.json", "/sampling.json", BindMode.READ_ONLY);

  @Test
  void remoteSampling_perOperation() {
    final JaegerRemoteSampler remoteSampler =
        JaegerRemoteSampler.builder()
            .setEndpoint("127.0.0.1:" + jaegerContainer.getMappedPort(COLLECTOR_PORT))
            .setServiceName(SERVICE_NAME)
            .build();

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(samplerIsType(remoteSampler, PerOperationSampler.class));
    assertThat(remoteSampler.getDescription()).contains("0.33").doesNotContain("150");
  }

  @Test
  void remoteSampling_rateLimiting() {
    final JaegerRemoteSampler remoteSampler =
        JaegerRemoteSampler.builder()
            .setEndpoint("127.0.0.1:" + jaegerContainer.getMappedPort(COLLECTOR_PORT))
            .setServiceName(SERVICE_NAME_RATE_LIMITING)
            .build();

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(samplerIsType(remoteSampler, RateLimitingSampler.class));
    assertThat(remoteSampler.getDescription()).contains("RateLimitingSampler{150.00}");
  }
}
