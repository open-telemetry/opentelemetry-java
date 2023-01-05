/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal;

import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Duration;
import javax.annotation.Nullable;

/**
 * {@link SpanExporter} SPI implementation for {@link ZipkinSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class ZipkinSpanExporterProvider implements ConfigurableSpanExporterProvider {
  @Override
  public String getName() {
    return "zipkin";
  }

  @Override
  public SpanExporter createExporter(ConfigProperties config) {
    ZipkinSpanExporterBuilder builder = ZipkinSpanExporter.builder();

    String endpoint = config.getString("otel.exporter.zipkin.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    Duration timeout = config.getDuration("otel.exporter.zipkin.timeout");
    if (timeout != null) {
      builder.setReadTimeout(timeout);
    }

    String certificatePath = config.getString("otel.exporter.zipkin.certificate");
    String clientKeyPath = config.getString("otel.exporter.zipkin.client.key");
    String clientKeyChainPath = config.getString("otel.exporter.zipkin.client.certificate");

    if (clientKeyPath != null && clientKeyChainPath == null) {
      throw new ConfigurationException("Client key provided but certification chain is missing");
    } else if (clientKeyPath == null && clientKeyChainPath != null) {
      throw new ConfigurationException("Client key chain provided but key is missing");
    }

    byte[] certificateBytes = readFileBytes(certificatePath);
    if (certificateBytes != null) {
      builder.setTrustedCertificates(certificateBytes);
    }

    byte[] clientKeyBytes = readFileBytes(clientKeyPath);
    byte[] clientKeyChainBytes = readFileBytes(clientKeyChainPath);

    if (clientKeyBytes != null && clientKeyChainBytes != null) {
      builder.setClientTls(clientKeyBytes, clientKeyChainBytes);
    }

    return builder.build();
  }

  @Nullable
  private static byte[] readFileBytes(@Nullable String filePath) {
    if (filePath == null) {
      return null;
    }
    File file = new File(filePath);
    if (!file.exists()) {
      throw new ConfigurationException("Invalid Zipkin certificate/key path: " + filePath);
    }
    try {
      RandomAccessFile raf = new RandomAccessFile(file, "r");
      byte[] bytes = new byte[(int) raf.length()];
      raf.readFully(bytes);
      return bytes;
    } catch (IOException e) {
      throw new ConfigurationException("Error reading content of file (" + filePath + ")", e);
    }
  }
}
