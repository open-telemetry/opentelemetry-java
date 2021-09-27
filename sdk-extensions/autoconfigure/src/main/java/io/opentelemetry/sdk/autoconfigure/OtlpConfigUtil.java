/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

final class OtlpConfigUtil {

  static final String DATA_TYPE_TRACES = "traces";
  static final String DATA_TYPE_METRICS = "metrics";
  static final String PROTOCOL_GRPC = "grpc";
  static final String PROTOCOL_HTTP_PROTOBUF = "http/protobuf";

  static String getOtlpProtocol(String dataType, ConfigProperties config) {
    String protocol = config.getString("otel.exporter.otlp." + dataType + ".protocol");
    if (protocol == null) {
      protocol = config.getString("otel.exporter.otlp.protocol");
    }
    if (protocol == null) {
      protocol = config.getString("otel.experimental.exporter.otlp." + dataType + ".protocol");
    }
    if (protocol == null) {
      protocol = config.getString("otel.experimental.exporter.otlp.protocol");
    }
    return (protocol == null) ? PROTOCOL_GRPC : protocol;
  }

  static void configureOtlpExporterBuilder(
      String dataType,
      ConfigProperties config,
      Consumer<String> setEndpoint,
      BiConsumer<String, String> addHeader,
      Consumer<String> setCompression,
      Consumer<Duration> setTimeout,
      Consumer<byte[]> setTrustedCertificates) {
    String endpoint = config.getString("otel.exporter.otlp." + dataType + ".endpoint");
    if (endpoint == null) {
      endpoint = config.getString("otel.exporter.otlp.endpoint");
      String protocol = getOtlpProtocol(dataType, config);
      if (endpoint != null && protocol.equals(PROTOCOL_HTTP_PROTOBUF)) {
        URI endpointUri;
        try {
          endpointUri = new URI(endpoint);
        } catch (URISyntaxException e) {
          throw new ConfigurationException("Invalid endpoint, must be a URI: " + endpoint, e);
        }
        String signalPath = signalPath(dataType);
        endpoint = endpointUri.resolve(signalPath).toString();
      }
    }
    if (endpoint != null) {
      setEndpoint.accept(endpoint);
    }

    Map<String, String> headers = config.getMap("otel.exporter.otlp." + dataType + ".headers");
    if (headers.isEmpty()) {
      headers = config.getMap("otel.exporter.otlp.headers");
    }
    headers.forEach(addHeader);

    String compression = config.getString("otel.exporter.otlp." + dataType + ".compression");
    if (compression == null) {
      compression = config.getString("otel.exporter.otlp.compression");
    }
    if (compression != null) {
      setCompression.accept(compression);
    }

    Duration timeout = config.getDuration("otel.exporter.otlp." + dataType + ".timeout");
    if (timeout == null) {
      timeout = config.getDuration("otel.exporter.otlp.timeout");
    }
    if (timeout != null) {
      setTimeout.accept(timeout);
    }

    String certificate = config.getString("otel.exporter.otlp." + dataType + ".certificate");
    if (certificate == null) {
      certificate = config.getString("otel.exporter.otlp.certificate");
    }
    if (certificate != null) {
      Path path = Paths.get(certificate);
      if (!Files.exists(path)) {
        throw new ConfigurationException("Invalid OTLP certificate path: " + path);
      }
      final byte[] certificateBytes;
      try {
        certificateBytes = Files.readAllBytes(path);
      } catch (IOException e) {
        throw new ConfigurationException("Error reading OTLP certificate.", e);
      }
      setTrustedCertificates.accept(certificateBytes);
    }
  }

  private static String signalPath(String dataType) {
    switch (dataType) {
      case DATA_TYPE_METRICS:
        return "/v1/metrics";
      case DATA_TYPE_TRACES:
        return "/v1/traces";
      default:
        throw new IllegalArgumentException(
            "Cannot determine signal path for unrecognized data type: " + dataType);
    }
  }

  private OtlpConfigUtil() {}
}
