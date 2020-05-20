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
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import java.util.Map;
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
   * Returns a new {@link Builder}, defaulted to use the {@link SpanBytesEncoder#JSON_V2} encoder.
   *
   * @return a {@code Builder}.
   * @since 0.4.0
   */
  public static Builder builder() {
    return new AutoValue_ZipkinExporterConfiguration.Builder().setEncoder(SpanBytesEncoder.JSON_V2);
  }

  /**
   * Builder for {@link ZipkinExporterConfiguration}.
   *
   * <p>Configuration options for {@link ZipkinExporterConfiguration} can be read from system
   * properties, environment variables, or {@link java.util.Properties} objects.
   *
   * <p>For system properties and {@link java.util.Properties} objects, {@link
   * ZipkinExporterConfiguration} will look for the following names:
   *
   * <ul>
   *   <li>{@code otel.zipkin.service.name}: to set the service name.
   *   <li>{@code otel.zipkin.endpoint}: to set the endpoint URL.
   * </ul>
   *
   * <p>For environment variables, {@link ZipkinExporterConfiguration} will look for the following
   * names:
   *
   * <ul>
   *   <li>{@code OTEL_ZIPKIN_SERVICE_NAME}: to set the service name.
   *   <li>{@code OTEL_ZIPKIN_ENDPOINT}: to set the endpoint URL.
   * </ul>
   *
   * @since 0.4.0
   */
  @AutoValue.Builder
  public abstract static class Builder extends ConfigBuilder<Builder> {

    private static final String KEY_SERVICE_NAME = "otel.zipkin.service.name";
    private static final String KEY_ENDPOINT = "otel.zipkin.endpoint";

    Builder() {}

    /**
     * Label of the remote node in the service graph, such as "favstar". Avoid names with variables
     * or unique identifiers embedded. Defaults to "unknown".
     *
     * <p>This is a primary label for trace lookup and aggregation, so it should be intuitive and
     * consistent. Many use a name from service discovery.
     *
     * <p>Note: this value, will be superseded by the value of {@link
     * io.opentelemetry.sdk.resources.ResourceConstants#SERVICE_NAME} if it has been set in the
     * {@link io.opentelemetry.sdk.resources.Resource} associated with the Tracer that created the
     * spans.
     *
     * <p>This property is required to be set.
     *
     * @see io.opentelemetry.sdk.resources.Resource
     * @see io.opentelemetry.sdk.resources.ResourceConstants
     * @since 0.4.0
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
     * @see SpanBytesEncoder
     * @since 0.4.0
     */
    public abstract Builder setEncoder(BytesEncoder<Span> encoder);

    /**
     * Sets the zipkin endpoint. This will use the endpoint to assign a {@link URLConnectionSender}
     * instance to this builder.
     *
     * @param endpoint The Zipkin endpoint URL, ex. "http://zipkinhost:9411/api/v2/spans".
     * @see URLConnectionSender
     * @since 0.4.0
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
  }
}
