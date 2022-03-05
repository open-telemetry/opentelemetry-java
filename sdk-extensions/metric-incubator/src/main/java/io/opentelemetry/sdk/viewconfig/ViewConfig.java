/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.viewconfig;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.InstrumentSelectorBuilder;
import io.opentelemetry.sdk.metrics.view.MeterSelector;
import io.opentelemetry.sdk.metrics.view.MeterSelectorBuilder;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.metrics.view.ViewBuilder;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.yaml.snakeyaml.Yaml;

/**
 * Enables file based YAML configuration of Metric SDK {@link View}s.
 *
 * <p>For example, a YAML file with the following content:
 *
 * <pre>
 *   - selector:
 *       instrument_name: my-instrument
 *       instrument_type: COUNTER
 *       meter_name: my-meter
 *       meter_version: 1.0.0
 *       meter_schema_url: http://example.com
 *     view:
 *       name: new-instrument-name
 *       description: new-description
 *       aggregation: histogram
 *       attribute_keys:
 *         - foo
 *         - bar
 * </pre>
 *
 * <p>Is equivalent to the following configuration:
 *
 * <pre>{@code
 * SdkMeterProvider.builder()
 *     .registerView(
 *         InstrumentSelector.builder()
 *             .setInstrumentName("my-instrument")
 *             .setInstrumentType(InstrumentType.COUNTER)
 *             .setMeterSelector(
 *                 MeterSelector.builder()
 *                     .setName("my-meter")
 *                     .setVersion("1.0.0")
 *                     .setSchemaUrl("http://example.com")
 *                     .build())
 *             .build(),
 *         View.builder()
 *             .setName("new-instrument")
 *             .setDescription("new-description")
 *             .setAggregation(Aggregation.histogram())
 *             .filterAttributes(key -> new HashSet<>(Arrays.asList("foo", "bar")).contains(key))
 *             .build());
 * }</pre>
 */
public final class ViewConfig {

  private ViewConfig() {}

  /**
   * Load the view configuration YAML from the {@code inputStream} and apply it to the {@link
   * SdkMeterProviderBuilder}.
   *
   * @throws ConfigurationException if unable to interpret {@code inputStream} contents
   */
  public static void registerViews(
      SdkMeterProviderBuilder meterProviderBuilder, InputStream inputStream) {
    List<ViewConfigSpecification> viewConfigSpecs = loadViewConfig(inputStream);

    for (ViewConfigSpecification viewConfigSpec : viewConfigSpecs) {
      meterProviderBuilder.registerView(
          toInstrumentSelector(viewConfigSpec.getSelectorSpecification()),
          toView(viewConfigSpec.getViewSpecification()));
    }
  }

  // Visible for testing
  @SuppressWarnings("unchecked")
  static List<ViewConfigSpecification> loadViewConfig(InputStream inputStream) {
    Yaml yaml = new Yaml();
    try {
      List<ViewConfigSpecification> result = new ArrayList<>();
      List<Map<String, Object>> viewConfigs = yaml.load(inputStream);
      for (Map<String, Object> viewConfigSpecMap : viewConfigs) {
        Map<String, Object> selectorSpecMap =
            requireNonNull(
                getAsType(viewConfigSpecMap, "selector", Map.class), "selector is required");
        Map<String, Object> viewSpecMap =
            requireNonNull(getAsType(viewConfigSpecMap, "view", Map.class), "view is required");

        InstrumentType instrumentType =
            Optional.ofNullable(getAsType(selectorSpecMap, "instrument_type", String.class))
                .map(InstrumentType::valueOf)
                .orElse(null);
        List<String> attributeKeys =
            Optional.ofNullable(
                    ((List<Object>) getAsType(viewSpecMap, "attribute_keys", List.class)))
                .map(objects -> objects.stream().map(String::valueOf).collect(toList()))
                .orElse(null);

        result.add(
            ViewConfigSpecification.builder()
                .selectorSpecification(
                    SelectorSpecification.builder()
                        .instrumentName(getAsType(selectorSpecMap, "instrument_name", String.class))
                        .instrumentType(instrumentType)
                        .meterName(getAsType(selectorSpecMap, "meter_name", String.class))
                        .meterVersion(getAsType(selectorSpecMap, "meter_version", String.class))
                        .meterSchemaUrl(
                            getAsType(selectorSpecMap, "meter_schema_url", String.class))
                        .build())
                .viewSpecification(
                    ViewSpecification.builder()
                        .name(getAsType(viewSpecMap, "name", String.class))
                        .description(getAsType(viewSpecMap, "description", String.class))
                        .aggregation(getAsType(viewSpecMap, "aggregation", String.class))
                        .attributeKeys(attributeKeys)
                        .build())
                .build());
      }
      return result;
    } catch (RuntimeException e) {
      throw new ConfigurationException("Failed to parse view config", e);
    }
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private static <T> T getAsType(Map<String, Object> map, String key, Class<T> type) {
    Object value = map.get(key);
    if (value != null && !type.isInstance(value)) {
      throw new IllegalStateException(
          "Expected "
              + key
              + " to be type "
              + type.getName()
              + " but was "
              + value.getClass().getName());
    }
    return (T) value;
  }

  // Visible for testing
  static View toView(ViewSpecification viewSpec) {
    ViewBuilder builder = View.builder();
    String name = viewSpec.getName();
    if (name != null) {
      builder.setName(name);
    }
    String description = viewSpec.getDescription();
    if (description != null) {
      builder.setDescription(description);
    }
    String aggregation = viewSpec.getAggregation();
    if (aggregation != null) {
      builder.setAggregation(toAggregation(aggregation));
    }
    List<String> attributeKeys = viewSpec.getAttributeKeys();
    if (attributeKeys != null) {
      Set<String> keySet = new HashSet<>(attributeKeys);
      builder.setAttributeFilter(keySet::contains);
    }
    return builder.build();
  }

  // Visible for testing
  static Aggregation toAggregation(String aggregation) {
    try {
      return Aggregation.forName(aggregation);
    } catch (IllegalArgumentException e) {
      throw new ConfigurationException("Error creating aggregation", e);
    }
  }

  // Visible for testing
  static InstrumentSelector toInstrumentSelector(SelectorSpecification selectorSpec) {
    InstrumentSelectorBuilder builder = InstrumentSelector.builder();
    String instrumentName = selectorSpec.getInstrumentName();
    if (instrumentName != null) {
      builder.setName(instrumentName);
    }
    InstrumentType instrumentType = selectorSpec.getInstrumentType();
    if (instrumentType != null) {
      builder.setType(instrumentType);
    }

    MeterSelectorBuilder meterBuilder = MeterSelector.builder();
    String meterName = selectorSpec.getMeterName();
    if (meterName != null) {
      meterBuilder.setName(meterName);
    }
    String meterVersion = selectorSpec.getMeterVersion();
    if (meterVersion != null) {
      meterBuilder.setVersion(meterVersion);
    }
    String meterSchemaUrl = selectorSpec.getMeterSchemaUrl();
    if (meterSchemaUrl != null) {
      meterBuilder.setSchemaUrl(meterSchemaUrl);
    }
    builder.setMeterSelector(meterBuilder.build());

    return builder.build();
  }
}
