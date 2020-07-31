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

import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Sampling;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Sampling.RateLimitingSamplingStrategy;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Sampling.SamplingStrategyParameters;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Sampling.SamplingStrategyType;
import io.opentelemetry.exporters.jaeger.proto.api_v2.SamplingManagerGrpc;
import io.opentelemetry.sdk.trace.Sampler;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

public class JaegerRemoteSamplerTest {

  private static final String SERVICE_NAME = "my-service";
  private static final int RATE = 999;

  private final String serverName = InProcessServerBuilder.generateName();
  private final ManagedChannel inProcessChannel =
      InProcessChannelBuilder.forName(serverName).directExecutor().build();

  @Rule public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private final SamplingManagerGrpc.SamplingManagerImplBase service =
      mock(
          SamplingManagerGrpc.SamplingManagerImplBase.class,
          delegatesTo(new MockSamplingManagerService()));

  static class MockSamplingManagerService extends SamplingManagerGrpc.SamplingManagerImplBase {

    @Override
    public void getSamplingStrategy(
        Sampling.SamplingStrategyParameters request,
        StreamObserver<Sampling.SamplingStrategyResponse> responseObserver) {

      Sampling.SamplingStrategyResponse response =
          Sampling.SamplingStrategyResponse.newBuilder()
              .setStrategyType(SamplingStrategyType.RATE_LIMITING)
              .setRateLimitingSampling(
                  RateLimitingSamplingStrategy.newBuilder().setMaxTracesPerSecond(RATE).build())
              .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
  }

  @Before
  public void before() throws IOException {
    grpcCleanup.register(
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(service)
            .build()
            .start());
    grpcCleanup.register(inProcessChannel);
  }

  @Test
  public void connectionWorks() {
    ArgumentCaptor<SamplingStrategyParameters> requestCaptor =
        ArgumentCaptor.forClass(Sampling.SamplingStrategyParameters.class);

    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.newBuilder()
            .setChannel(inProcessChannel)
            .setServiceName(SERVICE_NAME)
            .build();

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(samplerIsType(sampler, RateLimitingSampler.class));

    // verify
    verify(service).getSamplingStrategy(requestCaptor.capture(), ArgumentMatchers.any());
    Assert.assertEquals(SERVICE_NAME, requestCaptor.getValue().getServiceName());
    Assert.assertTrue(sampler.getSampler() instanceof RateLimitingSampler);
    Assert.assertEquals(
        RATE, ((RateLimitingSampler) sampler.getSampler()).getMaxTracesPerSecond(), 0);
  }

  @Test
  public void description() {
    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.newBuilder()
            .setChannel(inProcessChannel)
            .setServiceName(SERVICE_NAME)
            .build();
    assertTrue(
        sampler
            .getDescription()
            .matches(
                "JaegerRemoteSampler\\{Probability\\{probability=0.001, idUpperBound=.*\\}\\}"));

    // wait until the sampling strategy is retrieved before exiting test method
    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(samplerIsType(sampler, RateLimitingSampler.class));
  }

  static Callable<Boolean> samplerIsType(
      final JaegerRemoteSampler sampler, final Class<? extends Sampler> expected) {
    return () -> sampler.getSampler().getClass().equals(expected);
  }
}
