/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.configureOtlpHeaders;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.readFileBytes;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.validateEndpoint;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.internal.IncubatingExporterBuilderUtil;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class OtlpDeclarativeConfigUtil {

  /** Determine the configured OTLP protocol for the {@code dataType}. */
  public static String getStructuredConfigOtlpProtocol(DeclarativeConfigProperties config) {
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
      DeclarativeConfigProperties config,
      ConfigProvider configProvider,
      Consumer<ComponentLoader> setComponentLoader,
      Consumer<String> setEndpoint,
      BiConsumer<String, String> addHeader,
      Consumer<String> setCompression,
      Consumer<Duration> setTimeout,
      Consumer<byte[]> setTrustedCertificates,
      BiConsumer<byte[], byte[]> setClientTls,
      Consumer<RetryPolicy> setRetryPolicy,
      Consumer<MemoryMode> setMemoryMode,
      boolean isHttpProtobuf,
      Consumer<InternalTelemetryVersion> internalTelemetryVersionConsumer) {
    setComponentLoader.accept(config.getComponentLoader());

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

    List<DeclarativeConfigProperties> headers = config.getStructuredList("headers");
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

    DeclarativeConfigProperties tls =
        config.getStructured("tls", DeclarativeConfigProperties.empty());
    String certificatePath = tls.getString("ca_file");
    String clientKeyPath = tls.getString("key_file");
    String clientKeyChainPath = tls.getString("cert_file");

    if (clientKeyPath != null && clientKeyChainPath == null) {
      throw new ConfigurationException(
          "client_key_file provided without client_certificate_file - both client_key_file and client_certificate_file must be set");
    } else if (clientKeyPath == null && clientKeyChainPath != null) {
      throw new ConfigurationException(
          "client_certificate_file provided without client_key_file - both client_key_file and client_certificate_file must be set");
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

    IncubatingExporterBuilderUtil.configureExporterMemoryMode(config, setMemoryMode);

    InternalTelemetryVersion internalTelemetryVersion = getInternalTelemetryVersion(configProvider);
    if (internalTelemetryVersion != null) {
      internalTelemetryVersionConsumer.accept(internalTelemetryVersion);
    }
  }

  @Nullable
  private static InternalTelemetryVersion getInternalTelemetryVersion(
      ConfigProvider configProvider) {
    String internalTelemetryVersion =
        configProvider.getInstrumentationConfig("otel_sdk").getString("internal_telemetry_version");
    if (internalTelemetryVersion == null) {
      return null;
    }
    switch (internalTelemetryVersion.toLowerCase(Locale.ROOT)) {
      case "legacy":
        return InternalTelemetryVersion.LEGACY;
      case "latest":
        return InternalTelemetryVersion.LATEST;
      default:
        throw new DeclarativeConfigException(
            "Invalid sdk telemetry version: " + internalTelemetryVersion);
    }
  }

  private OtlpDeclarativeConfigUtil() {}
}
