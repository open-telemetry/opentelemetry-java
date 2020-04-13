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
import com.google.common.base.Preconditions;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;

/**
 * Configurations for {@link ZipkinSpanExporter}.
 *
 * @since 0.3.0
 */
@AutoValue
@Immutable
public abstract class ZipkinExporterConfiguration {

  ZipkinExporterConfiguration() {}

  /**
   * Returns the service name.
   *
   * @return the service name.
   * @since 0.3.0
   */
  public abstract String getServiceName();

  /**
   * Returns the Zipkin V2 URL.
   *
   * @return the Zipkin V2 URL.
   * @since 0.3.0
   */
  public abstract String getV2Url();

  /**
   * Returns the Zipkin sender.
   *
   * @return the Zipkin sender.
   * @since 0.3.0
   */
  @Nullable
  public abstract Sender getSender();

  /**
   * Returns the {@link SpanBytesEncoder}.
   *
   * <p>Default is {@link SpanBytesEncoder#JSON_V2}.
   *
   * @return the {@code SpanBytesEncoder}
   * @since 0.3.0
   */
  public abstract SpanBytesEncoder getEncoder();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a {@code Builder}.
   * @since 0.3.0
   */
  public static Builder builder() {
    return new AutoValue_ZipkinExporterConfiguration.Builder()
        .setV2Url("")
        .setEncoder(SpanBytesEncoder.JSON_V2);
  }

  /**
   * Builder for {@link ZipkinExporterConfiguration}.
   *
   * @since 0.3.0
   */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the service name.
     *
     * @param serviceName the service name.
     * @return this.
     * @since 0.3.0
     */
    public abstract Builder setServiceName(String serviceName);

    /**
     * Sets the Zipkin V2 URL, e.g.: "http://127.0.0.1:9411/api/v2/spans".
     *
     * <p>At least one of {@code V2Url} and {@code Sender} needs to be specified. If both {@code
     * V2Url} and {@code Sender} are set, {@code Sender} takes precedence.
     *
     * @param v2Url the Zipkin V2 URL.
     * @return this.
     * @since 0.3.0
     */
    public abstract Builder setV2Url(String v2Url);

    /**
     * Sets the Zipkin sender.
     *
     * <p>At least one of {@code V2Url} and {@code Sender} needs to be specified. If both {@code
     * V2Url} and {@code Sender} are set, {@code Sender} takes precedence.
     *
     * @param sender the Zipkin sender.
     * @return this.
     * @since 0.3.0
     */
    public abstract Builder setSender(Sender sender);

    /**
     * Sets the {@link SpanBytesEncoder}.
     *
     * @param encoder the {@code SpanBytesEncoder}.
     * @return this
     * @since 0.3.0
     */
    public abstract Builder setEncoder(SpanBytesEncoder encoder);

    abstract String getV2Url();

    @Nullable
    abstract Sender getSender();

    abstract ZipkinExporterConfiguration autoBuild();

    /**
     * Builds a {@link ZipkinExporterConfiguration}.
     *
     * @return a {@code ZipkinExporterConfiguration}.
     * @since 0.22
     */
    public ZipkinExporterConfiguration build() {
      Preconditions.checkArgument(
          !getV2Url().isEmpty() || getSender() != null,
          "Neither Zipkin V2 URL nor Zipkin sender is specified.");
      return autoBuild();
    }
  }
}
