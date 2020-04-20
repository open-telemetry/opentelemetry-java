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
import io.opentelemetry.internal.Utils;
import javax.annotation.Nullable;
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

  abstract String getEndpoint();

  @Nullable // note: this is needed to fool autovalue. It's not really nullable.
  abstract Sender getSender();

  abstract BytesEncoder<Span> getEncoder();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a {@code Builder}.
   * @since 0.4.0
   */
  public static Builder builder() {
    return new AutoValue_ZipkinExporterConfiguration.Builder()
        // trick auto-value so that we can check if either this or the sender are set at
        // build time
        .setEndpoint("")
        .setEncoder(SpanBytesEncoder.JSON_V2);
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
     * @since 0.4.0
     */
    public abstract Builder setServiceName(String serviceName);

    /**
     * Sets the Zipkin Endpoint URL, e.g.: "http://127.0.0.1:9411/api/v2/spans".
     *
     * <p>At least one of {@code endpoint} or {@code Sender} needs to be specified. If both {@code
     * V2Url} and {@code Sender} are set, {@code Sender} takes precedence.
     *
     * @param endpointUrl the Zipkin Endpoint URL.
     * @since 0.4.0
     */
    public abstract Builder setEndpoint(String endpointUrl);

    /**
     * Sets the Zipkin sender. Implements the client side of the span transport. Defaults to {@link
     * URLConnectionSender} if unspecified.
     *
     * <p>At least one of {@code endpoint} or {@code Sender} needs to be specified. If both {@code
     * V2Url} and {@code Sender} are set, {@code Sender} takes precedence.
     *
     * <p>Note: if you provide a {@link Sender} instance via this method, the {@link Sender#close()}
     * method will be called when the exporter is shut down.
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

    abstract String getEndpoint();

    @Nullable
    abstract Sender getSender();

    abstract ZipkinExporterConfiguration autoBuild();

    /**
     * Builds a {@link ZipkinExporterConfiguration}.
     *
     * @return a {@code ZipkinExporterConfiguration}.
     * @since 0.4.0
     */
    public ZipkinExporterConfiguration build() {
      Utils.checkArgument(
          !getEndpoint().isEmpty() || getSender() != null,
          "Neither Zipkin V2 URL nor Zipkin sender is specified.");
      if (getSender() == null) {
        setSender(URLConnectionSender.create(getEndpoint()));
      }
      return autoBuild();
    }
  }
}
