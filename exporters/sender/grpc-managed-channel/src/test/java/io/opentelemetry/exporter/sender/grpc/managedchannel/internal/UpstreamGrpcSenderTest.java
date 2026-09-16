/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.grpc.managedchannel.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class UpstreamGrpcSenderTest {

  @Test
  void shutdown_ManagedChannel_WaitsForChannelTermination() throws Exception {
    // The result code must only complete after the channel terminates, not as soon as
    // shutdownNow() returns.
    FakeManagedChannel channel = new FakeManagedChannel(/* terminates= */ true);
    UpstreamGrpcSender sender = newSender(channel, /* shutdownChannel= */ true);

    CompletableResultCode result = sender.shutdown();

    assertThat(channel.awaitTerminationEntered.await(10, TimeUnit.SECONDS)).isTrue();
    assertThat(channel.shutdownNowCalled()).isTrue();
    assertThat(result.isDone())
        .describedAs("result should not complete before the channel terminates")
        .isFalse();

    channel.releaseAwaitTermination.countDown();

    assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void shutdown_UnmanagedChannel_ReturnsImmediatelyAndLeavesChannelOpen() {
    // A caller supplied channel is not ours to close.
    FakeManagedChannel channel = new FakeManagedChannel(/* terminates= */ true);
    UpstreamGrpcSender sender = newSender(channel, /* shutdownChannel= */ false);

    CompletableResultCode result = sender.shutdown();

    assertThat(result.isDone()).isTrue();
    assertThat(result.isSuccess()).isTrue();
    assertThat(channel.shutdownNowCalled()).isFalse();
    assertThat(channel.awaitTerminationCalled()).isFalse();
  }

  @Test
  void shutdown_ChannelDoesNotTerminateInTime_Succeeds() {
    // Termination timing out must not hang or fail the shutdown.
    FakeManagedChannel channel = new FakeManagedChannel(/* terminates= */ false);
    channel.releaseAwaitTermination.countDown();
    UpstreamGrpcSender sender = newSender(channel, /* shutdownChannel= */ true);

    CompletableResultCode result = sender.shutdown();

    assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  private static UpstreamGrpcSender newSender(ManagedChannel channel, boolean shutdownChannel) {
    return new UpstreamGrpcSender(
        channel,
        "opentelemetry.proto.collector.trace.v1.TraceService/Export",
        /* compressor= */ null,
        shutdownChannel,
        Duration.ofSeconds(10),
        Collections::emptyMap,
        /* executorService= */ null);
  }

  /**
   * A {@link ManagedChannel} which records shutdown interactions and lets the test control when
   * {@link #awaitTermination(long, TimeUnit)} returns.
   */
  private static class FakeManagedChannel extends ManagedChannel {

    final CountDownLatch awaitTerminationEntered = new CountDownLatch(1);
    final CountDownLatch releaseAwaitTermination = new CountDownLatch(1);

    private final boolean terminates;
    private final AtomicBoolean shutdownNow = new AtomicBoolean();
    private final AtomicBoolean awaitTermination = new AtomicBoolean();

    FakeManagedChannel(boolean terminates) {
      this.terminates = terminates;
    }

    boolean shutdownNowCalled() {
      return shutdownNow.get();
    }

    boolean awaitTerminationCalled() {
      return awaitTermination.get();
    }

    @Override
    public ManagedChannel shutdown() {
      return this;
    }

    @Override
    public ManagedChannel shutdownNow() {
      shutdownNow.set(true);
      return this;
    }

    @Override
    public boolean isShutdown() {
      return shutdownNow.get();
    }

    @Override
    public boolean isTerminated() {
      return terminates && shutdownNow.get();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      awaitTermination.set(true);
      awaitTerminationEntered.countDown();
      releaseAwaitTermination.await(10, TimeUnit.SECONDS);
      return terminates;
    }

    @Override
    public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(
        MethodDescriptor<RequestT, ResponseT> methodDescriptor, CallOptions callOptions) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String authority() {
      return "fake";
    }
  }
}
