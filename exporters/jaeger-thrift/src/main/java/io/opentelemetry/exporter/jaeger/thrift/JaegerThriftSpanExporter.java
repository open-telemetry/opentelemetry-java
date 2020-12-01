/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger.thrift;

import io.jaegertracing.internal.exceptions.SenderException;
import io.jaegertracing.thrift.internal.senders.HttpSender;
import io.jaegertracing.thrift.internal.senders.ThriftSender;
import io.jaegertracing.thriftjava.Process;
import io.jaegertracing.thriftjava.Span;
import io.jaegertracing.thriftjava.Tag;
import io.jaegertracing.thriftjava.TagType;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
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

  public static final String DEFAULT_HOST_NAME = "unknown";
  public static final String DEFAULT_ENDPOINT = "http://localhost:14268/api/traces";
  public static final String DEFAULT_SERVICE_NAME = DEFAULT_HOST_NAME;

  private static final Logger logger = Logger.getLogger(JaegerThriftSpanExporter.class.getName());
  private static final String CLIENT_VERSION_KEY = "jaeger.version";
  private static final String CLIENT_VERSION_VALUE = "opentelemetry-java";
  private static final String HOSTNAME_KEY = "hostname";
  private static final String IP_KEY = "ip";
  private static final String IP_DEFAULT = "0.0.0.0";
  private final ThriftSender thriftSender;
  private final Process process;

  /**
   * Creates a new Jaeger gRPC Span Reporter with the given name, using the given channel.
   *
   * @param thriftSender The sender used for sending the data.
   * @param serviceName this service's name.
   */
  private JaegerThriftSpanExporter(ThriftSender thriftSender, String serviceName) {
    this.thriftSender = thriftSender;
    String hostname;
    String ipv4;

    if (serviceName == null || serviceName.trim().length() == 0) {
      throw new IllegalArgumentException("Service name must not be null or empty");
    }

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

    this.process = new Process().setServiceName(serviceName);
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
  public static Builder builder() {
    return new Builder();
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

  /** Builder utility for this exporter. */
  public static class Builder extends ConfigBuilder<Builder> {

    private static final String KEY_SERVICE_NAME = "otel.exporter.jaeger.service.name";
    private static final String KEY_ENDPOINT = "otel.exporter.jaeger.endpoint";

    private String serviceName = DEFAULT_SERVICE_NAME;
    private String endpoint = DEFAULT_ENDPOINT;
    private ThriftSender thriftSender;

    /**
     * Explicitly set the {@link ThriftSender} instance to use for this Exporter. Will override any
     * endpoint that has been set.
     *
     * @param thriftSender The ThriftSender to use.
     * @return this.
     */
    public Builder setThriftSender(ThriftSender thriftSender) {
      this.thriftSender = thriftSender;
      return this;
    }

    /**
     * Sets the service name to be used by this exporter. Required.
     *
     * @param serviceName the service name.
     * @return this.
     */
    public Builder setServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    /**
     * Sets the Jaeger endpoint to connect to. Needs to include the full API path for trace ingest.
     *
     * <p>Optional, defaults to "http://localhost:14268/api/traces".
     *
     * @param endpoint The Jaeger endpoint URL, ex. "https://jaegerhost:14268/api/traces".
     * @return this.
     */
    public Builder setEndpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    /**
     * Sets the configuration values from the given configuration map for only the available keys.
     *
     * @param configMap {@link Map} holding the configuration values.
     * @return this.
     */
    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      String stringValue = getStringProperty(KEY_SERVICE_NAME, configMap);
      if (stringValue != null) {
        this.setServiceName(stringValue);
      }
      stringValue = getStringProperty(KEY_ENDPOINT, configMap);
      if (stringValue != null) {
        this.setEndpoint(stringValue);
      }
      return this;
    }

    /**
     * Constructs a new instance of the exporter based on the builder's values.
     *
     * @return a new exporter's instance.
     */
    public JaegerThriftSpanExporter build() {
      if (thriftSender == null) {
        thriftSender = new HttpSender.Builder(endpoint).build();
      }
      return new JaegerThriftSpanExporter(thriftSender, serviceName);
    }

    private Builder() {}
  }
}
