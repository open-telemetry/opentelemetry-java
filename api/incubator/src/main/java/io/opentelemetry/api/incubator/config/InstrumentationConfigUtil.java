/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * A collection of convenience methods to extract instrumentation config from {@link
 * ConfigProvider#getInstrumentationConfig()}.
 */
public class InstrumentationConfigUtil {

  // TODO (jack-berg): add helper function to access nested structures with dot notation

  /**
   * Return a map representation of the peer service map entries in {@code
   * .instrumentation.general.peer.service_mapping}, or null if none is configured.
   */
  @Nullable
  public static Map<String, String> peerServiceMapping(ConfigProvider configProvider) {
    Optional<List<StructuredConfigProperties>> optServiceMappingList =
        Optional.ofNullable(configProvider.getInstrumentationConfig())
            .map(instrumentationConfig -> instrumentationConfig.getStructured("general"))
            .map(generalConfig -> generalConfig.getStructured("peer"))
            .map(httpConfig -> httpConfig.getStructuredList("service_mapping"));
    if (!optServiceMappingList.isPresent()) {
      return null;
    }
    Map<String, String> serviceMapping = new HashMap<>();
    optServiceMappingList
        .get()
        .forEach(
            entry -> {
              String peer = entry.getString("peer");
              String service = entry.getString("service");
              if (peer != null && service != null) {
                serviceMapping.put(peer, service);
              }
            });
    return serviceMapping;
  }

  /**
   * Return {@code .instrumentation.general.http.client.request_captured_headers}, or null if none
   * is configured.
   */
  @Nullable
  public static List<String> httpClientRequestCapturedHeaders(ConfigProvider configProvider) {
    return Optional.ofNullable(configProvider.getInstrumentationConfig())
        .map(instrumentationConfig -> instrumentationConfig.getStructured("general"))
        .map(generalConfig -> generalConfig.getStructured("http"))
        .map(httpConfig -> httpConfig.getStructured("client"))
        .map(clientConfig -> clientConfig.getScalarList("request_captured_headers", String.class))
        .orElse(null);
  }

  /**
   * Return {@code .instrumentation.general.http.client.response_captured_headers}, or null if none
   * is configured.
   */
  @Nullable
  public static List<String> httpClientResponseCapturedHeaders(ConfigProvider configProvider) {
    return Optional.ofNullable(configProvider.getInstrumentationConfig())
        .map(instrumentationConfig -> instrumentationConfig.getStructured("general"))
        .map(generalConfig -> generalConfig.getStructured("http"))
        .map(httpConfig -> httpConfig.getStructured("client"))
        .map(clientConfig -> clientConfig.getScalarList("response_captured_headers", String.class))
        .orElse(null);
  }

  /**
   * Return {@code .instrumentation.general.http.server.request_captured_headers}, or null if none
   * is configured.
   */
  @Nullable
  public static List<String> httpServerRequestCapturedHeaders(ConfigProvider configProvider) {
    return Optional.ofNullable(configProvider.getInstrumentationConfig())
        .map(instrumentationConfig -> instrumentationConfig.getStructured("general"))
        .map(generalConfig -> generalConfig.getStructured("http"))
        .map(httpConfig -> httpConfig.getStructured("server"))
        .map(clientConfig -> clientConfig.getScalarList("request_captured_headers", String.class))
        .orElse(null);
  }

  /**
   * Return {@code .instrumentation.general.http.server.response_captured_headers}, or null if none
   * is configured.
   */
  @Nullable
  public static List<String> httpSeverResponseCapturedHeaders(ConfigProvider configProvider) {
    return Optional.ofNullable(configProvider.getInstrumentationConfig())
        .map(instrumentationConfig -> instrumentationConfig.getStructured("general"))
        .map(generalConfig -> generalConfig.getStructured("http"))
        .map(httpConfig -> httpConfig.getStructured("server"))
        .map(clientConfig -> clientConfig.getScalarList("response_captured_headers", String.class))
        .orElse(null);
  }

  /** Return {@code .instrumentation.java.<instrumentationName>}, or null if none is configured. */
  @Nullable
  public static StructuredConfigProperties javaInstrumentationConfig(
      ConfigProvider configProvider, String instrumentationName) {
    return Optional.ofNullable(configProvider.getInstrumentationConfig())
        .map(instrumentationConfig -> instrumentationConfig.getStructured("java"))
        .map(generalConfig -> generalConfig.getStructured(instrumentationName))
        .orElse(null);
  }

  private InstrumentationConfigUtil() {}
}
