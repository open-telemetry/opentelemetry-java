/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.concurrent.ThreadSafe;

/** Exports spans to Jaeger via gRPC, using Jaeger's protobuf model. */
@ThreadSafe
public final class JaegerGrpcSpanExporter implements SpanExporter {

  private static final String DEFAULT_HOST_NAME = "unknown";
  private static final AttributeKey<String> CLIENT_VERSION_KEY =
      AttributeKey.stringKey("jaeger.version");
  private static final String CLIENT_VERSION_VALUE = "opentelemetry-java";
  private static final AttributeKey<String> HOSTNAME_KEY = AttributeKey.stringKey("hostname");
  private static final AttributeKey<String> IP_KEY = AttributeKey.stringKey("ip");
  private static final String IP_DEFAULT = "0.0.0.0";
  private final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(JaegerGrpcSpanExporter.class.getName()));

  private final MarshalerCollectorServiceGrpc.CollectorServiceFutureStub stub;

  // Jaeger-specific resource information
  private final Resource jaegerResource;
  private final ManagedChannel managedChannel;
  private final long timeoutNanos;

  /**
   * Creates a new Jaeger gRPC Span Reporter with the given name, using the given channel.
   *
   * @param channel the channel to use when communicating with the Jaeger Collector.
   * @param timeoutNanos max waiting time for the collector to process each span batch. When set to
   *     0 or to a negative value, the exporter will wait indefinitely.
   */
  JaegerGrpcSpanExporter(ManagedChannel channel, long timeoutNanos) {
    String hostname;
    String ipv4;

    try {
      hostname = InetAddress.getLocalHost().getHostName();
      ipv4 = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      hostname = DEFAULT_HOST_NAME;
      ipv4 = IP_DEFAULT;
    }

    jaegerResource =
        Resource.builder()
            .put(CLIENT_VERSION_KEY, CLIENT_VERSION_VALUE)
            .put(IP_KEY, ipv4)
            .put(HOSTNAME_KEY, hostname)
            .build();

    this.managedChannel = channel;
    this.stub = MarshalerCollectorServiceGrpc.newFutureStub(channel);
    this.timeoutNanos = timeoutNanos;
  }

  /**
   * Submits all the given spans in a single batch to the Jaeger collector.
   *
   * @param spans the list of sampled Spans to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    MarshalerCollectorServiceGrpc.CollectorServiceFutureStub stub = this.stub;
    if (timeoutNanos > 0) {
      stub = stub.withDeadlineAfter(timeoutNanos, TimeUnit.NANOSECONDS);
    }

    List<PostSpansRequestMarshaler> requests = new ArrayList<>();
    spans.stream()
        .collect(Collectors.groupingBy(SpanData::getResource))
        .forEach((resource, spanData) -> requests.add(buildRequest(resource, spanData)));

    List<ListenableFuture<PostSpansResponse>> listenableFutures = new ArrayList<>(requests.size());
    for (PostSpansRequestMarshaler request : requests) {
      listenableFutures.add(stub.postSpans(request));
    }

    final CompletableResultCode result = new CompletableResultCode();
    AtomicInteger pending = new AtomicInteger(listenableFutures.size());
    AtomicReference<Throwable> error = new AtomicReference<>();
    for (ListenableFuture<PostSpansResponse> future : listenableFutures) {
      Futures.addCallback(
          future,
          new FutureCallback<PostSpansResponse>() {
            @Override
            public void onSuccess(PostSpansResponse result) {
              fulfill();
            }

            @Override
            public void onFailure(Throwable t) {
              error.set(t);
              fulfill();
            }

            private void fulfill() {
              if (pending.decrementAndGet() == 0) {
                Throwable t = error.get();
                if (t != null) {
                  logger.log(Level.WARNING, "Failed to export spans", t);
                  result.fail();
                } else {
                  result.succeed();
                }
              }
            }
          },
          MoreExecutors.directExecutor());
    }
    return result;
  }

  private PostSpansRequestMarshaler buildRequest(Resource resource, List<SpanData> spans) {
    Resource mergedResource = jaegerResource.merge(resource);
    return PostSpansRequestMarshaler.create(spans, mergedResource);
  }

  /**
   * The Jaeger exporter does not batch spans, so this method will immediately return with success.
   *
   * @return always Success
   */
  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static JaegerGrpcSpanExporterBuilder builder() {
    return new JaegerGrpcSpanExporterBuilder();
  }

  /**
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled.
   */
  @Override
  public CompletableResultCode shutdown() {
    final CompletableResultCode result = new CompletableResultCode();
    managedChannel.notifyWhenStateChanged(ConnectivityState.SHUTDOWN, result::succeed);
    if (managedChannel.isShutdown()) {
      return result.succeed();
    }
    managedChannel.shutdown();
    return result;
  }

  // Visible for testing
  Resource getJaegerResource() {
    return jaegerResource;
  }

  // Visible for testing
  ManagedChannel getManagedChannel() {
    return managedChannel;
  }
}
