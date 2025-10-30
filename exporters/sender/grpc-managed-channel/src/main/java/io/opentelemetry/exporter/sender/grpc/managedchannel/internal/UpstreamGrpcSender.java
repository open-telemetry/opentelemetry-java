/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.grpc.managedchannel.internal;

import static io.grpc.MethodDescriptor.generateFullMethodName;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.Compressor;
import io.grpc.CompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.exporter.grpc.GrpcMessageWriter;
import io.opentelemetry.exporter.grpc.GrpcResponse;
import io.opentelemetry.exporter.grpc.GrpcSender;
import io.opentelemetry.exporter.grpc.GrpcStatusCode;
import io.opentelemetry.exporter.internal.grpc.ImmutableGrpcResponse;
import io.opentelemetry.exporter.internal.grpc.MarshalerInputStream;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * A {@link GrpcSender} which uses the upstream grpc-java library.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class UpstreamGrpcSender implements GrpcSender {

  private static final MethodDescriptor.Marshaller<GrpcMessageWriter> REQUEST_MARSHALER =
      new MethodDescriptor.Marshaller<GrpcMessageWriter>() {
        @Override
        public InputStream stream(GrpcMessageWriter value) {
          return new MarshalerInputStream(value);
        }

        @Override
        public GrpcMessageWriter parse(InputStream stream) {
          throw new UnsupportedOperationException("Only for serializing");
        }
      };

  private static final MethodDescriptor.Marshaller<byte[]> RESPONSE_MARSHALER =
      new MethodDescriptor.Marshaller<byte[]>() {
        @Override
        public InputStream stream(byte[] value) {
          throw new UnsupportedOperationException("Only for parsing");
        }

        @Override
        public byte[] parse(InputStream stream) {
          return new byte[] {}; // TODO: drain input to byte array
        }
      };

  private final ManagedChannel channel;
  private final MethodDescriptor<GrpcMessageWriter, byte[]> methodDescriptor;
  @Nullable private final String compressorName;
  private final boolean shutdownChannel;
  private final long timeoutNanos;
  private final Supplier<Map<String, List<String>>> headersSupplier;
  private final Executor executor;

  /** Creates a new {@link UpstreamGrpcSender}. */
  public UpstreamGrpcSender(
      ManagedChannel channel,
      String fullServiceName,
      String methodName,
      @Nullable io.opentelemetry.exporter.compressor.Compressor compressor,
      boolean shutdownChannel,
      long timeoutNanos,
      Supplier<Map<String, List<String>>> headersSupplier,
      @Nullable ExecutorService executorService) {
    this.channel = channel;
    this.methodDescriptor =
        MethodDescriptor.<GrpcMessageWriter, byte[]>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(generateFullMethodName(fullServiceName, methodName))
            .setRequestMarshaller(REQUEST_MARSHALER)
            .setResponseMarshaller(RESPONSE_MARSHALER)
            .build();
    if (compressor != null) {
      CompressorRegistry.getDefaultInstance()
          .register(
              new Compressor() {
                @Override
                public String getMessageEncoding() {
                  return compressor.getEncoding();
                }

                @Override
                public OutputStream compress(OutputStream os) throws IOException {
                  return compressor.compress(os);
                }
              });
      this.compressorName = compressor.getEncoding();
    } else {
      this.compressorName = null;
    }
    this.shutdownChannel = shutdownChannel;
    this.timeoutNanos = timeoutNanos;
    this.headersSupplier = headersSupplier;
    this.executor = executorService == null ? MoreExecutors.directExecutor() : executorService;
  }

  @Override
  public void send(
      GrpcMessageWriter messageWriter,
      Consumer<GrpcResponse> onResponse,
      Consumer<Throwable> onError) {
    CallOptions requestCallOptions = CallOptions.DEFAULT;
    Channel requestChannel = channel;
    if (timeoutNanos > 0) {
      requestCallOptions = requestCallOptions.withDeadlineAfter(Duration.ofNanos(timeoutNanos));
    }
    Metadata metadata = new Metadata();
    Map<String, List<String>> headers = headersSupplier.get();
    if (headers != null) {
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        metadata.put(
            Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER),
            String.join(",", entry.getValue()));
      }
      requestChannel =
          ClientInterceptors.intercept(
              requestChannel, MetadataUtils.newAttachHeadersInterceptor(metadata));

      List<String> hostHeaders = headers.get("host");
      if (hostHeaders != null && !hostHeaders.isEmpty()) {
        requestCallOptions = requestCallOptions.withAuthority(hostHeaders.get(0));
      }
    }
    if (this.compressorName != null) {
      requestCallOptions = requestCallOptions.withCompression(compressorName);
    }

    Futures.addCallback(
        ClientCalls.futureUnaryCall(
            requestChannel.newCall(methodDescriptor, requestCallOptions), messageWriter),
        new FutureCallback<byte[]>() {
          @Override
          public void onSuccess(@Nullable byte[] result) {
            // TODO: evaluate null case
            onResponse.accept(
                ImmutableGrpcResponse.create(
                    GrpcStatusCode.OK,
                    Status.OK.getDescription(),
                    result == null ? new byte[0] : result));
          }

          @Override
          public void onFailure(Throwable t) {
            Status status = fromThrowable(t);
            if (status == null) {
              onError.accept(t);
            } else {
              onResponse.accept(
                  ImmutableGrpcResponse.create(
                      GrpcStatusCode.fromValue(status.getCode().value()),
                      status.getDescription(),
                      new byte[0]));
            }
          }
        },
        executor);
  }

  /**
   * Copy of {@link Status#fromThrowable(Throwable)} which returns null instead of {@link
   * Status#UNKNOWN} when no status can be found.
   */
  @Nullable
  private static Status fromThrowable(Throwable cause) {
    while (cause != null) {
      if (cause instanceof StatusException) {
        return ((StatusException) cause).getStatus();
      } else if (cause instanceof StatusRuntimeException) {
        return ((StatusRuntimeException) cause).getStatus();
      }
      cause = cause.getCause();
    }
    return null;
  }

  @Override
  public CompletableResultCode shutdown() {
    if (shutdownChannel) {
      channel.shutdownNow();
    }
    return CompletableResultCode.ofSuccess();
  }
}
