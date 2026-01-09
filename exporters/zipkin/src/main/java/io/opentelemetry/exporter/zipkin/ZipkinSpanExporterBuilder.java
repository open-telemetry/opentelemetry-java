/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import java.net.InetAddress;
import java.time.Duration;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import zipkin2.Span;
import zipkin2.reporter.BytesEncoder;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.SpanBytesEncoder;
import zipkin2.reporter.okhttp3.OkHttpSender;

/** Builder class for {@link ZipkinSpanExporter}. */
public final class ZipkinSpanExporterBuilder {
  private BytesEncoder<Span> encoder = SpanBytesEncoder.JSON_V2;
  private Supplier<InetAddress> localIpAddressSupplier = LocalInetAddressSupplier.getInstance();
  @Nullable private BytesMessageSender sender;
  private String endpoint = ZipkinSpanExporter.DEFAULT_ENDPOINT;
  // compression is enabled by default, because this is the default of OkHttpSender,
  // which is created when no custom sender is set (see OkHttpSender.Builder)
  private boolean compressionEnabled = true;
  private int readTimeoutMillis = (int) TimeUnit.SECONDS.toMillis(10);
  private Supplier<MeterProvider> meterProviderSupplier =
      () -> GlobalOpenTelemetry.getOrNoop().getMeterProvider();
  private InternalTelemetryVersion internalTelemetryVersion = InternalTelemetryVersion.LEGACY;

  /**
   * Sets the Zipkin sender. Implements the client side of the span transport. An {@link
   * OkHttpSender} is a good default.
   *
   * <p>The {@link BytesMessageSender#close()} method will be called when the exporter is shut down.
   *
   * @param sender the Zipkin sender implementation.
   * @return this.
   * @deprecated Use {@link #setSender(BytesMessageSender)} insteead.
   */
  @Deprecated
  public ZipkinSpanExporterBuilder setSender(zipkin2.reporter.Sender sender) {
    return setSender((BytesMessageSender) sender);
  }

  /**
   * Sets the Zipkin sender. Implements the client side of the span transport. An {@link
   * OkHttpSender} is a good default.
   *
   * <p>The {@link BytesMessageSender#close()} method will be called when the exporter is shut down.
   *
   * @param sender the Zipkin sender implementation.
   * @return this.
   * @since 1.35.0
   */
  public ZipkinSpanExporterBuilder setSender(BytesMessageSender sender) {
    requireNonNull(sender, "sender");
    this.sender = sender;
    return this;
  }

  /**
   * Sets the {@link zipkin2.codec.BytesEncoder}, which controls the format used by the {@link
   * BytesMessageSender}. Defaults to the {@link zipkin2.codec.SpanBytesEncoder#JSON_V2}.
   *
   * @param encoder the {@code BytesEncoder} to use.
   * @return this.
   * @see zipkin2.codec.SpanBytesEncoder
   * @deprecated Use {@link #setEncoder(BytesEncoder)} instead.
   */
  @Deprecated
  public ZipkinSpanExporterBuilder setEncoder(zipkin2.codec.BytesEncoder<Span> encoder) {
    requireNonNull(encoder, "encoder");
    return setEncoder(new BytesEncoderAdapter(encoder));
  }

  /**
   * Sets the {@link BytesEncoder}, which controls the format used by the {@link
   * BytesMessageSender}. Defaults to the {@link SpanBytesEncoder#JSON_V2}.
   *
   * @param encoder the {@code BytesEncoder} to use.
   * @return this.
   * @see SpanBytesEncoder
   * @since 1.35.0
   */
  public ZipkinSpanExporterBuilder setEncoder(BytesEncoder<Span> encoder) {
    requireNonNull(encoder, "encoder");
    this.encoder = encoder;
    return this;
  }

  /**
   * Sets the Supplier of InetAddress. This Supplier will be used by the {@link
   * OtelToZipkinSpanTransformer} when creating the Zipkin local endpoint. The default
   * implementation uses a Supplier that returns a single unchanging IP address that is captured at
   * creation time.
   *
   * @param localIpAddressSupplier - A supplier that returns an InetAddress that may be null.
   * @return this
   * @since 1.18.0
   */
  public ZipkinSpanExporterBuilder setLocalIpAddressSupplier(
      Supplier<InetAddress> localIpAddressSupplier) {
    requireNonNull(localIpAddressSupplier, "localIpAddressSupplier");
    this.localIpAddressSupplier = localIpAddressSupplier;
    return this;
  }

  /**
   * Sets the zipkin endpoint. This will use the endpoint to assign an {@link OkHttpSender} instance
   * to this builder.
   *
   * @param endpoint The Zipkin endpoint URL, ex. "http://zipkinhost:9411/api/v2/spans".
   * @return this.
   * @see OkHttpSender
   */
  public ZipkinSpanExporterBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Sets the method used to compress payloads. If unset, gzip compression is enabled. Currently
   * supported compression methods include "gzip" and "none".
   *
   * <p>The compression method is ignored when a custom Zipkin sender is set via {@link
   * #setSender(BytesMessageSender)}.
   *
   * @param compressionMethod The compression method, ex. "gzip".
   * @return this.
   * @see OkHttpSender
   * @since 1.20.0
   */
  public ZipkinSpanExporterBuilder setCompression(String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    checkArgument(
        compressionMethod.equals("gzip") || compressionMethod.equals("none"),
        "Unsupported compression method. Supported compression methods include: gzip, none.");
    this.compressionEnabled = compressionMethod.equals("gzip");
    return this;
  }

  /**
   * Sets the maximum time to wait for the export of a batch of spans. If unset, defaults to 10s.
   *
   * @return this.
   * @since 1.2.0
   */
  public ZipkinSpanExporterBuilder setReadTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    long timeoutMillis = timeout == 0 ? Long.MAX_VALUE : unit.toMillis(timeout);
    this.readTimeoutMillis = (int) Math.min(timeoutMillis, Integer.MAX_VALUE);
    return this;
  }

  /**
   * Sets the maximum time to wait for the export of a batch of spans. If unset, defaults to 10s.
   *
   * @return this.
   * @since 1.2.0
   */
  public ZipkinSpanExporterBuilder setReadTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    setReadTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    return this;
  }

  /**
   * Sets the {@link MeterProvider} to use to collect metrics related to export. If not set, uses
   * {@link GlobalOpenTelemetry#getMeterProvider()}.
   *
   * @return this.
   * @since 1.17.0
   */
  public ZipkinSpanExporterBuilder setMeterProvider(MeterProvider meterProvider) {
    requireNonNull(meterProvider, "meterProvider");
    this.meterProviderSupplier = () -> meterProvider;
    return this;
  }

  /**
   * Sets the {@link InternalTelemetryVersion} defining which self-monitoring metrics this exporter
   * collects.
   *
   * @since 1.51.0
   */
  public ZipkinSpanExporterBuilder setInternalTelemetryVersion(InternalTelemetryVersion level) {
    requireNonNull(level, "level");
    this.internalTelemetryVersion = level;
    return this;
  }

  String toString(boolean includePrefixAndSuffix) {
    StringJoiner joiner =
        includePrefixAndSuffix
            ? new StringJoiner(", ", "ZipkinSpanExporterBuilder{", "}")
            : new StringJoiner(", ");
    joiner.add("endpoint=" + endpoint);
    joiner.add("compressionEnabled=" + compressionEnabled);
    joiner.add("readTimeoutMillis=" + readTimeoutMillis);
    joiner.add("internalTelemetrySchemaVersion=" + internalTelemetryVersion);
    // Note: omit sender because we can't log the configuration in any readable way
    // Note: omit encoder because we can't log the configuration in any readable way
    // Note: omit localIpAddressSupplier because we can't log the configuration in any readable way
    // Note: omit meterProviderSupplier because we can't log the configuration in any readable way
    return joiner.toString();
  }

  /**
   * Builds a {@link ZipkinSpanExporter}.
   *
   * @return a {@code ZipkinSpanExporter}.
   */
  public ZipkinSpanExporter build() {
    BytesMessageSender sender = this.sender;
    if (sender == null) {
      sender =
          OkHttpSender.newBuilder()
              .endpoint(endpoint)
              .compressionEnabled(compressionEnabled)
              .readTimeout(readTimeoutMillis)
              .build();
    }
    OtelToZipkinSpanTransformer transformer =
        OtelToZipkinSpanTransformer.create(localIpAddressSupplier);
    return new ZipkinSpanExporter(
        this,
        encoder,
        sender,
        meterProviderSupplier,
        internalTelemetryVersion,
        endpoint,
        transformer);
  }
}
