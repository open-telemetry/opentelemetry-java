/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * A collection of convenience methods to extract instrumentation config from {@link
 * ConfigProvider#getInstrumentationConfig()}.
 */
public class InstrumentationConfigUtil {

  /**
   * Return a map representation of the peer service map entries in {@code
   * .instrumentation.general.peer.service_mapping}, or null if none is configured.
   *
   * @throws DeclarativeConfigException if an unexpected type is encountered accessing the property
   */
  @Nullable
  public static Map<String, String> peerServiceMapping(ConfigProvider configProvider) {
    List<DeclarativeConfigProperties> serviceMappingList =
        getOrNull(
            configProvider,
            config -> config.getStructuredList("service_mapping"),
            "general",
            "peer");
    if (serviceMappingList == null) {
      return null;
    }
    Map<String, String> serviceMapping = new LinkedHashMap<>();
    serviceMappingList.forEach(
        entry -> {
          String peer = entry.getString("peer");
          String service = entry.getString("service");
          if (peer != null && service != null) {
            serviceMapping.put(peer, service);
          }
        });
    return serviceMapping.isEmpty() ? null : serviceMapping;
  }

  /**
   * Return {@code .instrumentation.general.http.client.request_captured_headers}, or null if none
   * is configured.
   *
   * @throws DeclarativeConfigException if an unexpected type is encountered accessing the property
   */
  @Nullable
  public static List<String> httpClientRequestCapturedHeaders(ConfigProvider configProvider) {
    return getOrNull(
        configProvider,
        config -> config.getScalarList("request_captured_headers", String.class),
        "general",
        "http",
        "client");
  }

  /**
   * Return {@code .instrumentation.general.http.client.response_captured_headers}, or null if none
   * is configured.
   *
   * @throws DeclarativeConfigException if an unexpected type is encountered accessing the property
   */
  @Nullable
  public static List<String> httpClientResponseCapturedHeaders(ConfigProvider configProvider) {
    return getOrNull(
        configProvider,
        config -> config.getScalarList("response_captured_headers", String.class),
        "general",
        "http",
        "client");
  }

  /**
   * Return {@code .instrumentation.general.http.server.request_captured_headers}, or null if none
   * is configured.
   *
   * @throws DeclarativeConfigException if an unexpected type is encountered accessing the property
   */
  @Nullable
  public static List<String> httpServerRequestCapturedHeaders(ConfigProvider configProvider) {
    return getOrNull(
        configProvider,
        config -> config.getScalarList("request_captured_headers", String.class),
        "general",
        "http",
        "server");
  }

  /**
   * Return {@code .instrumentation.general.http.server.response_captured_headers}, or null if none
   * is configured.
   *
   * @throws DeclarativeConfigException if an unexpected type is encountered accessing the property
   */
  @Nullable
  public static List<String> httpServerResponseCapturedHeaders(ConfigProvider configProvider) {
    return getOrNull(
        configProvider,
        config -> config.getScalarList("response_captured_headers", String.class),
        "general",
        "http",
        "server");
  }

  /**
   * Return {@code .instrumentation.java.<instrumentationName>}, or null if none is configured.
   *
   * @throws DeclarativeConfigException if an unexpected type is encountered accessing the property
   */
  @Nullable
  public static DeclarativeConfigProperties javaInstrumentationConfig(
      ConfigProvider configProvider, String instrumentationName) {
    return getOrNull(configProvider, config -> config.getStructured(instrumentationName), "java");
  }

  /**
   * Walk down the {@code segments} of {@link ConfigProvider#getInstrumentationConfig()} and call
   * {@code accessor} on the terminal node. Returns null if null is encountered walking the {@code
   * segments}, or if {@code accessor} returns null.
   *
   * <p>See other methods in {@link InstrumentationConfigUtil} for usage examples.
   */
  @Nullable
  public static <T> T getOrNull(
      ConfigProvider configProvider,
      Function<DeclarativeConfigProperties, T> accessor,
      String... segments) {
    DeclarativeConfigProperties config = configProvider.getInstrumentationConfig();
    for (String segment : segments) {
      config = config.getStructured(segment);
      if (config == null) {
        return null;
      }
    }
    return accessor.apply(config);
  }

  private InstrumentationConfigUtil() {}

  /**
   * Return {@code .instrumentation.java.<instrumentationName>}, after converting it to the {@code
   * modelType} using the {@code objectMapper}. If no configuration exists for the {@code
   * instrumentationName}, returns {@code null}.
   *
   * <p>This method is a convenience method for a common instrumentation library workflow:
   *
   * <ul>
   *   <li>During initialization, an instrumentation library is given an {@link ConfigProvider} and
   *       must initialize according to the relevant config
   *   <li>It checks if the user has provided configuration for it, and if so...
   *   <li>It converts the configuration to an in-memory model representing all of its relevant
   *       properties
   *   <li>It initializes using the strongly typed in-memory model
   * </ul>
   *
   * <p>Conversion is done using {@link ObjectMapper#convertValue(Object, Class)} from {@code
   * com.fasterxml.jackson.databind}, and assumes the {@code modelType} is a POJO written /
   * annotated to support jackson databinding.
   *
   * <p>NOTE: callers MUST add their own dependency on {@code
   * com.fasterxml.jackson.core:jackson-databind}. This module's dependency is {@code compileOnly}
   * since jackson is a large dependency that many users will not require. It's very possible to
   * convert between {@link DeclarativeConfigProperties} (or a map representation from {@link
   * DeclarativeConfigProperties#toMap(DeclarativeConfigProperties)}) and a target model type
   * without jackson. This method is provided as an optional convenience method.
   *
   * @throws IllegalArgumentException if conversion fails. See {@link
   *     ObjectMapper#convertValue(Object, Class)} for details.
   */
  @Nullable
  public static <T> T getInstrumentationConfigModel(
      ConfigProvider configProvider,
      String instrumentationName,
      ObjectMapper objectMapper,
      Class<T> modelType) {
    DeclarativeConfigProperties properties =
        javaInstrumentationConfig(configProvider, instrumentationName);
    if (properties == null) {
      return null;
    }
    Map<String, Object> configPropertiesMap = DeclarativeConfigProperties.toMap(properties);
    return objectMapper.convertValue(configPropertiesMap, modelType);
  }
}
