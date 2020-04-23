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

package io.opentelemetry.sdk.contrib.trace.jaeger.sampler;

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
import io.opentelemetry.exporters.jaeger.proto.api_v2.Sampling.SamplingStrategyResponse;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Sampling.SamplingStrategyType;
import io.opentelemetry.exporters.jaeger.proto.api_v2.SamplingManagerGrpc;
import io.opentelemetry.sdk.trace.Sampler;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

@RunWith(JUnit4.class)
public class JaegerRemoteSamplerTest {

  private static final String SERVICE_NAME = "my-service";
  private static final int RATE = 999;

  @Rule public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private final SamplingManagerGrpc.SamplingManagerImplBase service =
      mock(
          SamplingManagerGrpc.SamplingManagerImplBase.class,
          delegatesTo(new MockSamplingManagerService()));

  static class MockSamplingManagerService extends SamplingManagerGrpc.SamplingManagerImplBase {

    @Override
    public void getSamplingStrategy(
        io.opentelemetry.exporters.jaeger.proto.api_v2.Sampling.SamplingStrategyParameters request,
        io.grpc.stub.StreamObserver<
                io.opentelemetry.exporters.jaeger.proto.api_v2.Sampling.SamplingStrategyResponse>
            responseObserver) {

      SamplingStrategyResponse response =
          SamplingStrategyResponse.newBuilder()
              .setStrategyType(SamplingStrategyType.RATE_LIMITING)
              .setRateLimitingSampling(
                  RateLimitingSamplingStrategy.newBuilder().setMaxTracesPerSecond(RATE).build())
              .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
  }

  @Test
  public void connectionWorks() throws IOException {
    final String serverName = InProcessServerBuilder.generateName();
    final ArgumentCaptor<SamplingStrategyParameters> requestCaptor =
        ArgumentCaptor.forClass(Sampling.SamplingStrategyParameters.class);

    grpcCleanup.register(
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(service)
            .build()
            .start());

    ManagedChannel channel =
        grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.newBuilder().setChannel(channel).setServiceName(SERVICE_NAME).build();

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(samplerIsType(sampler, RateLimitingSampler.class));

    // verify
    verify(service)
        .getSamplingStrategy(
            requestCaptor.capture(),
            ArgumentMatchers.<StreamObserver<SamplingStrategyResponse>>any());
    Assert.assertEquals(SERVICE_NAME, requestCaptor.getValue().getServiceName());
    Assert.assertTrue(sampler.getSampler() instanceof RateLimitingSampler);
    Assert.assertEquals(
        RATE, ((RateLimitingSampler) sampler.getSampler()).getMaxTracesPerSecond(), 0);
  }

  @Test
  public void description() {
    ManagedChannel channel =
        grpcCleanup.register(
            InProcessChannelBuilder.forName(SERVICE_NAME).directExecutor().build());
    JaegerRemoteSampler sampler =
        JaegerRemoteSampler.newBuilder().setChannel(channel).setServiceName(SERVICE_NAME).build();
    assertTrue(
        sampler
            .getDescription()
            .matches(
                "JaegerRemoteSampler\\{Probability\\{probability=0.001, idUpperBound=.*\\}\\}"));
  }

  static Callable<Boolean> samplerIsType(
      final JaegerRemoteSampler sampler, final Class<? extends Sampler> expected) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() {
        return sampler.getSampler().getClass().equals(expected);
      }
    };
  }
}
