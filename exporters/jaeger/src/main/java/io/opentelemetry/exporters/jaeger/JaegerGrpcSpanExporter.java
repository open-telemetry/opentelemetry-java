/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.exporters.jaeger;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporters.jaeger.proto.api_v2.CollectorServiceGrpc;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Exports spans to Jaeger via gRPC, using Jaeger's protobuf model. */
public class JaegerGrpcSpanExporter implements SpanExporter {
  private static final Logger logger = Logger.getLogger(JaegerGrpcSpanExporter.class.getName());

  private final CollectorServiceGrpc.CollectorServiceBlockingStub blockingStub;
  private final Model.Process process;
  private final ManagedChannel managedChannel;

  /**
   * Creates a new Jaeger gRPC Span Reporter with the given name, using the given channel.
   *
   * @param serviceName this service's name
   * @param channel the channel to use when communicating with the Jaeger Collector
   */
  private JaegerGrpcSpanExporter(String serviceName, ManagedChannel channel) {
    String hostname;
    String ipv4;

    if (serviceName == null || serviceName.trim().length() == 0) {
      throw new IllegalArgumentException("Service name must not be null or empty");
    }

    try {
      hostname = InetAddress.getLocalHost().getHostName();
      ipv4 = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      hostname = "(unknown)";
      ipv4 = "0.0.0.0";
    }

    Model.KeyValue clientTag =
        Model.KeyValue.newBuilder().setKey("jaeger.version").setVStr("opentelemetry-java").build();

    Model.KeyValue ipv4Tag = Model.KeyValue.newBuilder().setKey("ip").setVStr(ipv4).build();

    Model.KeyValue hostnameTag =
        Model.KeyValue.newBuilder().setKey("hostname").setVStr(hostname).build();

    this.process =
        Model.Process.newBuilder()
            .setServiceName(serviceName)
            .addTags(clientTag)
            .addTags(ipv4Tag)
            .addTags(hostnameTag)
            .build();
    this.managedChannel = channel;
    this.blockingStub = CollectorServiceGrpc.newBlockingStub(channel);
  }

  @Override
  public ResultCode export(List<Span> spans) {
    Model.Batch.Builder builder = Model.Batch.newBuilder();
    builder.addAllSpans(Adapter.toJaeger(spans));
    builder.setProcess(this.process);

    Collector.PostSpansRequest.Builder requestBuilder = Collector.PostSpansRequest.newBuilder();
    requestBuilder.setBatch(builder.build());
    Collector.PostSpansRequest request = requestBuilder.build();

    try {
      // for now, there's nothing to check in the response object
      //noinspection ResultOfMethodCallIgnored
      this.blockingStub.postSpans(request);
      return ResultCode.SUCCESS;
    } catch (Throwable t) {
      // TODO(jpkroehling) what are the possibilities here?
      //  gRPC should will retry when the problem is in the connection
      return ResultCode.FAILED_NONE_RETRYABLE;
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public void shutdown() {
    try {
      managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.log(Level.WARNING, "Failed to shutdown the gRPC channel", e);
    }
  }

  public static class Builder {
    private String serviceName;
    private ManagedChannel channel;

    public Builder setServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    public Builder setChannel(ManagedChannel channel) {
      this.channel = channel;
      return this;
    }

    public JaegerGrpcSpanExporter build() {
      return new JaegerGrpcSpanExporter(serviceName, channel);
    }
  }
}
