/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.viewconfig;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.MeterSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.metrics.view.ViewBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class ViewConfig {

  private ViewConfig() {}

  /**
   * Load the view configuration YAML from the file and apply it to the {@link
   * SdkMeterProviderBuilder}.
   *
   * @throws ConfigurationException if unable to interpret file contents
   */
  @SuppressWarnings("NullAway")
  public static void registerViews(
      SdkMeterProviderBuilder meterProviderBuilder, File viewConfigYamlFile) {
    List<ViewConfigSpecification> viewConfigSpecs = loadViewConfig(viewConfigYamlFile);

    for (ViewConfigSpecification viewConfigSpec : viewConfigSpecs) {
      assert viewConfigSpec.getSelectorSpecification() != null;
      assert viewConfigSpec.getViewSpecification() != null;
      meterProviderBuilder.registerView(
          toInstrumentSelector(viewConfigSpec.getSelectorSpecification()),
          toView(viewConfigSpec.getViewSpecification()));
    }
  }

  // Visible for testing
  @SuppressWarnings("unchecked")
  static List<ViewConfigSpecification> loadViewConfig(File viewConfigYamlFile) {
    Yaml yaml = new Yaml();
    try {
      List<ViewConfigSpecification> result = new ArrayList<>();
      List<Map<String, Object>> viewConfigs =
          yaml.load(Files.newBufferedReader(viewConfigYamlFile.toPath(), StandardCharsets.UTF_8));
      for (Map<String, Object> viewConfigSpecMap : viewConfigs) {
        Map<String, Object> selectorSpecMap =
            Objects.requireNonNull(
                getAsType(viewConfigSpecMap, "selector", Map.class), "selector is required");
        Map<String, Object> viewSpecMap =
            Objects.requireNonNull(
                getAsType(viewConfigSpecMap, "view", Map.class), "view is required");

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
    } catch (IOException e) {
      throw new ConfigurationException(
          "An error occurred reading view config file:  " + viewConfigYamlFile.getAbsolutePath(),
          e);
    } catch (RuntimeException e) {
      throw new ConfigurationException(
          "Failed to parse view config file: " + viewConfigYamlFile.getAbsolutePath(), e);
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
    Optional.ofNullable(viewSpec.getName()).ifPresent(builder::setName);
    Optional.ofNullable(viewSpec.getDescription()).ifPresent(builder::setDescription);
    Optional.ofNullable(viewSpec.getAggregation())
        .map(ViewConfig::toAggregation)
        .ifPresent(builder::setAggregation);
    Optional.ofNullable(viewSpec.getAttributeKeys())
        .ifPresent(
            attributeKeys -> {
              Set<String> keySet = new HashSet<>(attributeKeys);
              builder.filterAttributes(keySet::contains);
            });
    return builder.build();
  }

  // Visible for testing
  static Aggregation toAggregation(String aggregation) {
    switch (aggregation) {
      case "sum":
        return Aggregation.sum();
      case "last_value":
        return Aggregation.lastValue();
      case "drop":
        return Aggregation.drop();
      case "histogram":
        return Aggregation.explicitBucketHistogram();
      default:
        throw new ConfigurationException("Unrecognized aggregation " + aggregation);
    }
  }

  // Visible for testing
  static InstrumentSelector toInstrumentSelector(SelectorSpecification selectorSpec) {
    InstrumentSelector.Builder builder = InstrumentSelector.builder();
    Optional.ofNullable(selectorSpec.getInstrumentName()).ifPresent(builder::setInstrumentName);
    Optional.ofNullable(selectorSpec.getInstrumentType()).ifPresent(builder::setInstrumentType);

    MeterSelector.Builder meterBuilder = MeterSelector.builder();
    Optional.ofNullable(selectorSpec.getMeterName()).ifPresent(meterBuilder::setName);
    Optional.ofNullable(selectorSpec.getMeterVersion()).ifPresent(meterBuilder::setVersion);
    Optional.ofNullable(selectorSpec.getMeterSchemaUrl()).ifPresent(meterBuilder::setSchemaUrl);
    builder.setMeterSelector(meterBuilder.build());

    return builder.build();
  }
}
