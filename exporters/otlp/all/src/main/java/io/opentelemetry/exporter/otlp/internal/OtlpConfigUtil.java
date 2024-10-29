/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class OtlpConfigUtil {

  private static final Logger logger = Logger.getLogger(OtlpConfigUtil.class.getName());

  public static final String DATA_TYPE_TRACES = "traces";
  public static final String DATA_TYPE_METRICS = "metrics";
  public static final String DATA_TYPE_LOGS = "logs";

  public static final String PROTOCOL_GRPC = "grpc";
  public static final String PROTOCOL_HTTP_PROTOBUF = "http/protobuf";

  /** Determine the configured OTLP protocol for the {@code dataType}. */
  public static String getOtlpProtocol(String dataType, ConfigProperties config) {
    String protocol = config.getString("otel.exporter.otlp." + dataType + ".protocol");
    if (protocol != null) {
      return protocol;
    }
    return config.getString("otel.exporter.otlp.protocol", PROTOCOL_GRPC);
  }

  /** Determine the configured OTLP protocol for the {@code dataType}. */
  public static String getStructuredConfigOtlpProtocol(StructuredConfigProperties config) {
    // NOTE: The default OTLP protocol is different for declarative config than for env var / system
    // property based config. This is intentional. OpenTelemetry changed the default protocol
    // recommendation from grpc to http/protobuf, but the autoconfigure's env var / system property
    // based config did not update to reflect this before stabilizing, and changing is a breaking
    // change requiring a major version bump. Declarative config is not yet stable and therefore can
    // switch to the current default recommendation, which aligns also aligns with the behavior of
    // the OpenTelemetry Java Agent 2.x+.
    return config.getString("protocol", PROTOCOL_HTTP_PROTOBUF);
  }

  /** Invoke the setters with the OTLP configuration for the {@code dataType}. */
  @SuppressWarnings("TooManyParameters")
  public static void configureOtlpExporterBuilder(
      String dataType,
      ConfigProperties config,
      Consumer<String> setEndpoint,
      BiConsumer<String, String> addHeader,
      Consumer<String> setCompression,
      Consumer<Duration> setTimeout,
      Consumer<byte[]> setTrustedCertificates,
      BiConsumer<byte[], byte[]> setClientTls,
      Consumer<RetryPolicy> setRetryPolicy,
      Consumer<MemoryMode> setMemoryMode) {
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

    configureOtlpHeaders(config, dataType, addHeader);

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

    String certificatePath =
        config.getString(
            determinePropertyByType(config, "otel.exporter.otlp", dataType, "certificate"));
    String clientKeyPath =
        config.getString(
            determinePropertyByType(config, "otel.exporter.otlp", dataType, "client.key"));
    String clientKeyChainPath =
        config.getString(
            determinePropertyByType(config, "otel.exporter.otlp", dataType, "client.certificate"));

    if (clientKeyPath != null && clientKeyChainPath == null) {
      throw new ConfigurationException(
          "client key provided without client certificate - both client key and client certificate must be set");
    } else if (clientKeyPath == null && clientKeyChainPath != null) {
      throw new ConfigurationException(
          "client certificate provided without client key - both client key and client_certificate must be set");
    }

    byte[] certificateBytes = readFileBytes(certificatePath);
    if (certificateBytes != null) {
      setTrustedCertificates.accept(certificateBytes);
    }

    byte[] clientKeyBytes = readFileBytes(clientKeyPath);
    byte[] clientKeyChainBytes = readFileBytes(clientKeyChainPath);

    if (clientKeyBytes != null && clientKeyChainBytes != null) {
      setClientTls.accept(clientKeyBytes, clientKeyChainBytes);
    }

    Boolean retryDisabled = config.getBoolean("otel.java.exporter.otlp.retry.disabled");
    if (retryDisabled == null) {
      Boolean experimentalRetryEnabled =
          config.getBoolean("otel.experimental.exporter.otlp.retry.enabled");
      if (experimentalRetryEnabled != null) {
        retryDisabled = !experimentalRetryEnabled;
      }
    }
    if (retryDisabled != null && retryDisabled) {
      setRetryPolicy.accept(null);
    }

    ExporterBuilderUtil.configureExporterMemoryMode(config, setMemoryMode);
  }

  /** Invoke the setters with the OTLP configuration for the {@code dataType}. */
  @SuppressWarnings("TooManyParameters")
  public static void configureOtlpExporterBuilder(
      String dataType,
      StructuredConfigProperties config,
      Consumer<String> setEndpoint,
      BiConsumer<String, String> addHeader,
      Consumer<String> setCompression,
      Consumer<Duration> setTimeout,
      Consumer<byte[]> setTrustedCertificates,
      BiConsumer<byte[], byte[]> setClientTls,
      Consumer<RetryPolicy> setRetryPolicy,
      Consumer<MemoryMode> setMemoryMode) {
    String protocol = getStructuredConfigOtlpProtocol(config);
    boolean isHttpProtobuf = protocol.equals(PROTOCOL_HTTP_PROTOBUF);
    URL endpoint = validateEndpoint(config.getString("endpoint"), isHttpProtobuf);
    if (endpoint != null) {
      setEndpoint.accept(endpoint.toString());
    }

    String headerList = config.getString("headers_list");
    if (headerList != null) {
      ConfigProperties headersListConfig =
          DefaultConfigProperties.createFromMap(
              Collections.singletonMap("otel.exporter.otlp.headers", headerList));
      configureOtlpHeaders(headersListConfig, dataType, addHeader);
    }

    List<StructuredConfigProperties> headers = config.getStructuredList("headers");
    if (headers != null) {
      headers.forEach(
          header -> {
            String name = header.getString("name");
            String value = header.getString("value");
            if (name != null && value != null) {
              addHeader.accept(name, value);
            }
          });
    }

    String compression = config.getString("compression");
    if (compression != null) {
      setCompression.accept(compression);
    }

    Integer timeoutMs = config.getInt("timeout");
    if (timeoutMs != null) {
      setTimeout.accept(Duration.ofMillis(timeoutMs));
    }

    String certificatePath = config.getString("certificate");
    String clientKeyPath = config.getString("client_key");
    String clientKeyChainPath = config.getString("client_certificate");

    if (clientKeyPath != null && clientKeyChainPath == null) {
      throw new ConfigurationException(
          "client_key provided without client_certificate - both client_key and client_certificate must be set");
    } else if (clientKeyPath == null && clientKeyChainPath != null) {
      throw new ConfigurationException(
          "client_certificate provided without client_key - both client_key and client_certificate must be set");
    }
    byte[] certificateBytes = readFileBytes(certificatePath);
    if (certificateBytes != null) {
      setTrustedCertificates.accept(certificateBytes);
    }
    byte[] clientKeyBytes = readFileBytes(clientKeyPath);
    byte[] clientKeyChainBytes = readFileBytes(clientKeyChainPath);
    if (clientKeyBytes != null && clientKeyChainBytes != null) {
      setClientTls.accept(clientKeyBytes, clientKeyChainBytes);
    }

    ExporterBuilderUtil.configureExporterMemoryMode(config, setMemoryMode);
  }

  private static void configureOtlpHeaders(
      ConfigProperties config, String dataType, BiConsumer<String, String> addHeader) {
    Map<String, String> headers = config.getMap("otel.exporter.otlp." + dataType + ".headers");
    if (headers.isEmpty()) {
      headers = config.getMap("otel.exporter.otlp.headers");
    }
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      try {
        // headers are encoded as URL - see
        // https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#specifying-headers-via-environment-variables
        addHeader.accept(key, URLDecoder.decode(value, StandardCharsets.UTF_8.displayName()));
      } catch (Exception e) {
        throw new ConfigurationException("Cannot decode header value: " + value, e);
      }
    }
  }

  private static URL createUrl(URL context, String spec) {
    try {
      return new URL(context, spec);
    } catch (MalformedURLException e) {
      throw new ConfigurationException("Unexpected exception creating URL.", e);
    }
  }

  @Nullable
  private static URL validateEndpoint(@Nullable String endpoint, boolean isHttpProtobuf) {
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
    if (!isHttpProtobuf
        && (!endpointUrl.getPath().isEmpty() && !endpointUrl.getPath().equals("/"))) {
      throw new ConfigurationException(
          "OTLP endpoint must not have a path: " + endpointUrl.getPath());
    }
    if ((endpointUrl.getPort() == 4317 && isHttpProtobuf)
        || (endpointUrl.getPort() == 4318 && !isHttpProtobuf)) {
      int expectedPort = isHttpProtobuf ? 4318 : 4317;
      String protocol = isHttpProtobuf ? PROTOCOL_HTTP_PROTOBUF : PROTOCOL_GRPC;
      logger.warning(
          "OTLP exporter endpoint port is likely incorrect for protocol version \""
              + protocol
              + "\". The endpoint "
              + endpointUrl
              + " has port "
              + endpointUrl.getPort()
              + ". Typically, the \""
              + protocol
              + "\" version of OTLP uses port "
              + expectedPort
              + ".");
    }
    return endpointUrl;
  }

  @Nullable
  private static byte[] readFileBytes(@Nullable String filePath) {
    if (filePath == null) {
      return null;
    }
    File file = new File(filePath);
    if (!file.exists()) {
      throw new ConfigurationException("Invalid OTLP certificate/key path: " + filePath);
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

  private static String determinePropertyByType(
      ConfigProperties config, String prefix, String dataType, String suffix) {
    String propertyToRead = prefix + "." + dataType + "." + suffix;
    if (configContainsKey(config, propertyToRead)) {
      return propertyToRead;
    }
    return prefix + "." + suffix;
  }

  private static boolean configContainsKey(ConfigProperties config, String propertyToRead) {
    return config.getString(propertyToRead) != null;
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
