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

package io.opentelemetry.exporters.zipkin;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;
import zipkin2.Span;
import zipkin2.codec.BytesEncoder;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;
import zipkin2.reporter.urlconnection.URLConnectionSender;

/**
 * Configurations for {@link ZipkinSpanExporter}.
 *
 * @since 0.4.0
 */
@AutoValue
@Immutable
public abstract class ZipkinExporterConfiguration {

  ZipkinExporterConfiguration() {}

  abstract String getServiceName();

  abstract Sender getSender();

  abstract BytesEncoder<Span> getEncoder();

  /**
   * Returns a new {@link Builder} with defaults set to an "unknown" serviceName and using the
   * {@link SpanBytesEncoder#JSON_V2} encoder.
   *
   * @return a {@code Builder}.
   * @since 0.4.0
   */
  public static Builder builder() {
    return new AutoValue_ZipkinExporterConfiguration.Builder()
        .setEncoder(SpanBytesEncoder.JSON_V2)
        .setServiceName("unknown");
  }

  /**
   * Builds a HTTP exporter for <a href="https://zipkin.io/zipkin-api/#/">Zipkin V2</a> format.
   *
   * @param endpoint The Zipkin endpoint URL, ex. "http://zipkinhost:9411/api/v2/spans".
   * @param serviceName The serviceName with which to identify Spans. See {@link
   *     Builder#setServiceName(String)} for details.
   */
  public static ZipkinExporterConfiguration create(String endpoint, String serviceName) {
    return ZipkinExporterConfiguration.builder()
        .setEndpoint(endpoint)
        .setServiceName(serviceName)
        .build();
  }

  /**
   * Builds a HTTP exporter for <a href="https://zipkin.io/zipkin-api/#/">Zipkin V2</a> format.
   *
   * @param endpoint The Zipkin endpoint URL, ex. "http://zipkinhost:9411/api/v2/spans".
   */
  public static ZipkinExporterConfiguration create(String endpoint) {
    return create(endpoint, "unknown");
  }

  /**
   * Builder for {@link ZipkinExporterConfiguration}.
   *
   * @since 0.4.0
   */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Label of the remote node in the service graph, such as "favstar". Avoid names with variables
     * or unique identifiers embedded. Defaults to "unknown".
     *
     * <p>This is a primary label for trace lookup and aggregation, so it should be intuitive and
     * consistent. Many use a name from service discovery.
     *
     * <p>Note: this value, if set, will be superceded by the value of {@link
     * io.opentelemetry.sdk.resources.ResourceConstants#SERVICE_NAME} if it has been set in the
     * {@link io.opentelemetry.sdk.resources.Resource} associated with the Tracer that created the
     * spans.
     *
     * @since 0.4.0
     * @see io.opentelemetry.sdk.resources.Resource
     * @see io.opentelemetry.sdk.resources.ResourceConstants
     */
    public abstract Builder setServiceName(String serviceName);

    /**
     * Sets the Zipkin sender. Implements the client side of the span transport. A {@link
     * URLConnectionSender} is a good default.
     *
     * <p>The {@link Sender#close()} method will be called when the exporter is shut down.
     *
     * @param sender the Zipkin sender implementation.
     * @since 0.4.0
     */
    public abstract Builder setSender(Sender sender);

    /**
     * Sets the {@link BytesEncoder}, which controls the format used by the {@link Sender}. Defaults
     * to the {@link SpanBytesEncoder#JSON_V2}.
     *
     * @param encoder the {@code BytesEncoder} to use.
     * @since 0.4.0
     * @see SpanBytesEncoder
     */
    public abstract Builder setEncoder(BytesEncoder<Span> encoder);

    /**
     * Sets the zipkin endpoint. This will use the endpoint to assign a {@link URLConnectionSender}
     * instance to this builder.
     *
     * @param endpoint The Zipkin endpoint URL, ex. "http://zipkinhost:9411/api/v2/spans".
     * @since 0.4.0
     * @see URLConnectionSender
     */
    public Builder setEndpoint(String endpoint) {
      setSender(URLConnectionSender.create(endpoint));
      return this;
    }

    /**
     * Builds a {@link ZipkinExporterConfiguration}.
     *
     * @return a {@code ZipkinExporterConfiguration}.
     * @since 0.4.0
     */
    public abstract ZipkinExporterConfiguration build();
  }
}
