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

package io.opentelemetry.sdk.extensions.trace.jaeger.sampler;

import static io.opentelemetry.sdk.extensions.trace.jaeger.sampler.JaegerRemoteSamplerTest.samplerIsType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@EnabledIfSystemProperty(named = "enable.docker.tests", matches = "true")
class JaegerRemoteSamplerIntegrationTest {

  private static final int QUERY_PORT = 16686;
  private static final int COLLECTOR_PORT = 14250;
  private static final String JAEGER_VERSION = "1.17";
  private static final String SERVICE_NAME = "E2E-test";
  private static final String SERVICE_NAME_RATE_LIMITING = "bar";
  private static final int RATE = 150;

  @Container
  public static GenericContainer<?> jaegerContainer =
      new GenericContainer<>("jaegertracing/all-in-one:" + JAEGER_VERSION)
          .withCommand("--sampling.strategies-file=/sampling.json")
          .withExposedPorts(COLLECTOR_PORT, QUERY_PORT)
          .waitingFor(new HttpWaitStrategy().forPath("/"))
          .withClasspathResourceMapping("sampling.json", "/sampling.json", BindMode.READ_ONLY);

  @Test
  void remoteSampling_perOperation() {
    String jaegerHost =
        String.format("127.0.0.1:%d", jaegerContainer.getMappedPort(COLLECTOR_PORT));
    final JaegerRemoteSampler remoteSampler =
        JaegerRemoteSampler.newBuilder()
            .setChannel(ManagedChannelBuilder.forTarget(jaegerHost).usePlaintext().build())
            .setServiceName(SERVICE_NAME)
            .build();

    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(samplerIsType(remoteSampler, PerOperationSampler.class));
    assertThat(remoteSampler.getSampler()).isInstanceOf(PerOperationSampler.class);
    assertThat(remoteSampler.getDescription()).contains("0.33").doesNotContain("150");
  }

  @Test
  void remoteSampling_rateLimiting() {
    String jaegerHost =
        String.format("127.0.0.1:%d", jaegerContainer.getMappedPort(COLLECTOR_PORT));
    final JaegerRemoteSampler remoteSampler =
        JaegerRemoteSampler.newBuilder()
            .setChannel(ManagedChannelBuilder.forTarget(jaegerHost).usePlaintext().build())
            .setServiceName(SERVICE_NAME_RATE_LIMITING)
            .build();

    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(samplerIsType(remoteSampler, RateLimitingSampler.class));
    assertThat(remoteSampler.getSampler()).isInstanceOf(RateLimitingSampler.class);
    assertThat(((RateLimitingSampler) remoteSampler.getSampler()).getMaxTracesPerSecond())
        .isEqualTo(RATE);
  }
}
