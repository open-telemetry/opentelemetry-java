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

import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

public class JaegerRemoteSamplerIntegrationTest {

  private static final int QUERY_PORT = 16686;
  private static final int COLLECTOR_PORT = 14250;
  private static final String JAEGER_VERSION = "1.17";
  private static final String SERVICE_NAME = "E2E-test";
  private static final String SERVICE_NAME_RATE_LIMITING = "bar";
  private static final int RATE = 150;

  @SuppressWarnings("rawtypes")
  @ClassRule
  @Nullable
  public static GenericContainer jaegerContainer;

  static {
    // make sure that the user has enabled the docker-based tests
    if (Boolean.getBoolean("enable.docker.tests")) {
      jaegerContainer =
          new GenericContainer<>("jaegertracing/all-in-one:" + JAEGER_VERSION)
              .withCommand("--sampling.strategies-file=/sampling.json")
              .withExposedPorts(COLLECTOR_PORT, QUERY_PORT)
              .waitingFor(new HttpWaitStrategy().forPath("/"))
              .withClasspathResourceMapping("sampling.json", "/sampling.json", BindMode.READ_ONLY);
    }
  }

  @Test
  public void remoteSampling_perOperation() {
    Assume.assumeNotNull(jaegerContainer);
    String jaegerHost =
        String.format("127.0.0.1:%d", jaegerContainer.getMappedPort(COLLECTOR_PORT));
    final JaegerRemoteSampler remoteSampler =
        JaegerRemoteSampler.newBuilder()
            .setChannel(ManagedChannelBuilder.forTarget(jaegerHost).usePlaintext().build())
            .setServiceName(SERVICE_NAME)
            .build();

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(samplerIsType(remoteSampler, PerOperationSampler.class));
    Assert.assertTrue(remoteSampler.getSampler() instanceof PerOperationSampler);
    Assert.assertTrue(remoteSampler.getDescription().contains("0.33"));
    Assert.assertFalse(remoteSampler.getDescription().contains("150"));
  }

  @Test
  public void remoteSampling_rateLimiting() {
    Assume.assumeNotNull(jaegerContainer);
    String jaegerHost =
        String.format("127.0.0.1:%d", jaegerContainer.getMappedPort(COLLECTOR_PORT));
    final JaegerRemoteSampler remoteSampler =
        JaegerRemoteSampler.newBuilder()
            .setChannel(ManagedChannelBuilder.forTarget(jaegerHost).usePlaintext().build())
            .setServiceName(SERVICE_NAME_RATE_LIMITING)
            .build();

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(samplerIsType(remoteSampler, RateLimitingSampler.class));
    Assert.assertTrue(remoteSampler.getSampler() instanceof RateLimitingSampler);
    Assert.assertEquals(
        RATE, ((RateLimitingSampler) remoteSampler.getSampler()).getMaxTracesPerSecond(), 0);
  }
}
