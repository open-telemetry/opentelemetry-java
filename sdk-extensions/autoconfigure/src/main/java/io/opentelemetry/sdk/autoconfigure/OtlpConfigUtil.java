/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;

final class OtlpConfigUtil {

  static final String DATA_TYPE_TRACES = "traces";
  static final String DATA_TYPE_METRICS = "metrics";
  static final String DATA_TYPE_LOGS = "logs";

  static final String PROTOCOL_GRPC = "grpc";
  static final String PROTOCOL_HTTP_PROTOBUF = "http/protobuf";

  static String getOtlpProtocol(String dataType, ConfigProperties config) {
    String protocol = config.getString("otel.exporter.otlp." + dataType + ".protocol");
    if (protocol == null) {
      protocol = config.getString("otel.exporter.otlp.protocol");
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
      Consumer<byte[]> setTrustedCertificates,
      BiConsumer<byte[], byte[]> setClientTls,
      Consumer<RetryPolicy> setRetryPolicy) {
    String protocol = getOtlpProtocol(dataType, config);
    boolean isHttpProtobuf = protocol.equals(PROTOCOL_HTTP_PROTOBUF);
    URL endpoint =
        validateEndpoint(
            config.getString("otel.exporter.otlp." + dataType + ".endpoint"), isHttpProtobuf);
    if (endpoint != null) {
      if (endpoint.getPath().isEmpty()) {
        endpoint = createUrl(endpoint, "/");
      }
    } else {
      endpoint = validateEndpoint(config.getString("otel.exporter.otlp.endpoint"), isHttpProtobuf);
      if (endpoint != null && isHttpProtobuf) {
        String path = endpoint.getPath();
        if (!path.endsWith("/")) {
          path += "/";
        }
        path += signalPath(dataType);
        endpoint = createUrl(endpoint, path);
      }
    }
    if (endpoint != null) {
      setEndpoint.accept(endpoint.toString());
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

    byte[] certificateBytes = readFileBytes(config, "otel.exporter.otlp", dataType, "certificate");
    if (certificateBytes != null) {
      setTrustedCertificates.accept(certificateBytes);
    }

    byte[] clientKeyBytes = readFileBytes(config, "otel.exporter.otlp", dataType, "client.key");
    byte[] clientKeyChainBytes =
        readFileBytes(config, "otel.exporter.otlp", dataType, "client.certificate");

    if (clientKeyBytes != null && clientKeyChainBytes == null) {
      throw new ConfigurationException("Client key provided but certification chain is missing");
    } else if (clientKeyBytes == null && clientKeyChainBytes != null) {
      throw new ConfigurationException("Client key chain provided but key is missing");
    } else if (clientKeyBytes != null && clientKeyChainBytes != null) {
      setClientTls.accept(clientKeyBytes, clientKeyChainBytes);
    }

    Boolean retryEnabled = config.getBoolean("otel.experimental.exporter.otlp.retry.enabled");
    if (retryEnabled != null && retryEnabled) {
      setRetryPolicy.accept(RetryPolicy.getDefault());
    }
  }

  static void configureOtlpAggregationTemporality(
      ConfigProperties config, Consumer<AggregationTemporality> setAggregationTemporality) {
    String temporalityStr = config.getString("otel.exporter.otlp.metrics.temporality");
    if (temporalityStr == null) {
      return;
    }
    AggregationTemporality temporality;
    try {
      temporality = AggregationTemporality.valueOf(temporalityStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ConfigurationException(
          "Unrecognized aggregation temporality: " + temporalityStr, e);
    }
    setAggregationTemporality.accept(temporality);
  }

  private static URL createUrl(URL context, String spec) {
    try {
      return new URL(context, spec);
    } catch (MalformedURLException e) {
      throw new ConfigurationException("Unexpected exception creating URL.", e);
    }
  }

  @Nullable
  private static URL validateEndpoint(@Nullable String endpoint, boolean allowPath) {
    if (endpoint == null) {
      return null;
    }
    URL endpointUrl;
    try {
      endpointUrl = new URL(endpoint);
    } catch (MalformedURLException e) {
      throw new ConfigurationException("OTLP endpoint must be a valid URL: " + endpoint, e);
    }
    if (!endpointUrl.getProtocol().equals("http") && !endpointUrl.getProtocol().equals("https")) {
      throw new ConfigurationException(
          "OTLP endpoint scheme must be http or https: " + endpointUrl.getProtocol());
    }
    if (endpointUrl.getQuery() != null) {
      throw new ConfigurationException(
          "OTLP endpoint must not have a query string: " + endpointUrl.getQuery());
    }
    if (endpointUrl.getRef() != null) {
      throw new ConfigurationException(
          "OTLP endpoint must not have a fragment: " + endpointUrl.getRef());
    }
    if (!allowPath && (!endpointUrl.getPath().isEmpty() && !endpointUrl.getPath().equals("/"))) {
      throw new ConfigurationException(
          "OTLP endpoint must not have a path: " + endpointUrl.getPath());
    }
    return endpointUrl;
  }

  @Nullable
  private static byte[] readFileBytes(
      ConfigProperties config, String prefix, String dataType, String suffix) {
    String propertyToRead = prefix + "." + dataType + "." + suffix;
    String filePath = config.getString(propertyToRead);
    if (filePath == null) {
      propertyToRead = prefix + "." + suffix;
      filePath = config.getString(propertyToRead);
    }
    if (filePath == null) {
      return null;
    }
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      throw new ConfigurationException(
          "Invalid OTLP certificate/key path: " + path + " (configured in property: " + propertyToRead + ")");
    }
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new ConfigurationException(
          "Error reading content of file (" + path + ") configured in " + propertyToRead, e);
    }
  }

  private static String signalPath(String dataType) {
    switch (dataType) {
      case DATA_TYPE_METRICS:
        return "v1/metrics";
      case DATA_TYPE_TRACES:
        return "v1/traces";
      case DATA_TYPE_LOGS:
        return "v1/logs";
      default:
        throw new IllegalArgumentException(
            "Cannot determine signal path for unrecognized data type: " + dataType);
    }
  }

  private OtlpConfigUtil() {}
}
