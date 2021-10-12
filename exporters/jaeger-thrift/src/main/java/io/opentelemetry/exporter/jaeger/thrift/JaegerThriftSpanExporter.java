/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger.thrift;

import io.jaegertracing.internal.exceptions.SenderException;
import io.jaegertracing.thrift.internal.senders.ThriftSender;
import io.jaegertracing.thriftjava.Process;
import io.jaegertracing.thriftjava.Span;
import io.jaegertracing.thriftjava.Tag;
import io.jaegertracing.thriftjava.TagType;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.concurrent.ThreadSafe;

/** Exports spans to Jaeger via Thrift, using Jaeger's thrift model. */
@ThreadSafe
public final class JaegerThriftSpanExporter implements SpanExporter {

  static final String DEFAULT_ENDPOINT = "http://localhost:14268/api/traces";

  private static final String DEFAULT_HOST_NAME = "unknown";
  private static final String CLIENT_VERSION_KEY = "jaeger.version";
  private static final String CLIENT_VERSION_VALUE = "opentelemetry-java";
  private static final String HOSTNAME_KEY = "hostname";
  private static final String IP_KEY = "ip";
  private static final String IP_DEFAULT = "0.0.0.0";

  private final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(JaegerThriftSpanExporter.class.getName()));
  private final ThriftSender thriftSender;
  private final Process process;

  /**
   * Creates a new Jaeger gRPC Span Reporter with the given name, using the given channel.
   *
   * @param thriftSender The sender used for sending the data.
   */
  JaegerThriftSpanExporter(ThriftSender thriftSender) {
    this.thriftSender = thriftSender;
    String hostname;
    String ipv4;

    try {
      hostname = InetAddress.getLocalHost().getHostName();
      ipv4 = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      hostname = DEFAULT_HOST_NAME;
      ipv4 = IP_DEFAULT;
    }

    Tag clientTag = new Tag(CLIENT_VERSION_KEY, TagType.STRING).setVStr(CLIENT_VERSION_VALUE);
    Tag ipv4Tag = new Tag(IP_KEY, TagType.STRING).setVStr(ipv4);
    Tag hostnameTag = new Tag(HOSTNAME_KEY, TagType.STRING).setVStr(hostname);

    this.process = new Process();
    this.process.addToTags(clientTag);
    this.process.addToTags(ipv4Tag);
    this.process.addToTags(hostnameTag);
  }

  /**
   * Submits all the given spans in a single batch to the Jaeger collector.
   *
   * @param spans the list of sampled Spans to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    Map<Process, List<Span>> batches =
        spans.stream().collect(Collectors.groupingBy(SpanData::getResource)).entrySet().stream()
            .collect(
                Collectors.toMap(
                    entry -> createProcess(entry.getKey()),
                    entry -> Adapter.toJaeger(entry.getValue())));

    List<CompletableResultCode> batchResults = new ArrayList<>(batches.size());
    batches.forEach(
        (process, jaegerSpans) -> {
          CompletableResultCode batchResult = new CompletableResultCode();
          batchResults.add(batchResult);
          try {
            // todo: consider making truly async
            thriftSender.send(process, jaegerSpans);
            batchResult.succeed();
          } catch (SenderException e) {
            logger.log(Level.WARNING, "Failed to export spans", e);
            batchResult.fail();
          }
        });
    return CompletableResultCode.ofAll(batchResults);
  }

  private Process createProcess(Resource resource) {
    Process result = new Process(this.process);

    String serviceName = resource.getAttribute(ResourceAttributes.SERVICE_NAME);
    if (serviceName == null || serviceName.isEmpty()) {
      serviceName = Resource.getDefault().getAttribute(ResourceAttributes.SERVICE_NAME);
    }
    // In practice should never be null unless the default Resource spec is changed.
    if (serviceName != null) {
      result.setServiceName(serviceName);
    }

    List<Tag> tags = Adapter.toTags(resource.getAttributes());
    tags.forEach(result::addToTags);
    return result;
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
  public static JaegerThriftSpanExporterBuilder builder() {
    return new JaegerThriftSpanExporterBuilder();
  }

  /**
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled.
   */
  @Override
  public CompletableResultCode shutdown() {
    final CompletableResultCode result = new CompletableResultCode();
    // todo
    return result.succeed();
  }

  // Visible for testing
  Process getProcess() {
    return process;
  }
}
