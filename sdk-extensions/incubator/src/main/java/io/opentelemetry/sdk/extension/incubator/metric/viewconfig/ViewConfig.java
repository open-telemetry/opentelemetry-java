/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.metric.viewconfig;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentSelectorBuilder;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.ViewBuilder;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregationUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/**
 * Enables file based YAML configuration of Metric SDK {@link View}s.
 *
 * <p>For example, a YAML file with the following content:
 *
 * <pre>
 *   - selector:
 *       instrument_name: my-instrument
 *       instrument_type: COUNTER
 *       instrument_unit: ms
 *       meter_name: my-meter
 *       meter_version: 1.0.0
 *       meter_schema_url: http://example.com
 *     view:
 *       name: new-instrument-name
 *       description: new-description
 *       aggregation: explicit_bucket_histogram
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
 *             .setName("my-instrument")
 *             .setType(InstrumentType.COUNTER)
 *             .setUnit("ms")
 *             .setMeterName("my-meter")
 *             .setMeterVersion("1.0.0")
 *             .setMeterSchemaUrl("http://example.com")
 *             .build(),
 *         View.builder()
 *             .setName("new-instrument")
 *             .setDescription("new-description")
 *             .setAggregation(Aggregation.explicitBucketHistogram())
 *             .setAttributesFilter(key -> new HashSet<>(Arrays.asList("foo", "bar")).contains(key))
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
    LoadSettings settings = LoadSettings.builder().build();
    Load yaml = new Load(settings);
    try {
      List<ViewConfigSpecification> result = new ArrayList<>();

      List<Map<String, Object>> viewConfigs =
          (List<Map<String, Object>>) yaml.loadFromInputStream(inputStream);

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
                        .instrumentUnit(getAsType(selectorSpecMap, "instrument_unit", String.class))
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
                        .aggregationArgs(getAsType(viewSpecMap, "aggregation_args", Map.class))
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
      Map<String, Object> aggregationArgs =
          viewSpec.getAggregationArgs() == null
              ? Collections.emptyMap()
              : viewSpec.getAggregationArgs();
      builder.setAggregation(toAggregation(aggregation, aggregationArgs));
    }
    List<String> attributeKeys = viewSpec.getAttributeKeys();
    if (attributeKeys != null) {
      Set<String> keySet = new HashSet<>(attributeKeys);
      builder.setAttributeFilter(keySet::contains);
    }
    return builder.build();
  }

  // Visible for testing
  static Aggregation toAggregation(String aggregationName, Map<String, Object> aggregationArgs) {
    Aggregation aggregation;
    try {
      aggregation = AggregationUtil.forName(aggregationName);
    } catch (IllegalArgumentException e) {
      throw new ConfigurationException("Error creating aggregation", e);
    }
    if (Aggregation.explicitBucketHistogram().equals(aggregation)) {
      List<Double> bucketBoundaries = getBucketBoundaries(aggregationArgs);
      if (bucketBoundaries != null) {
        return Aggregation.explicitBucketHistogram(bucketBoundaries);
      }
    }
    if (Aggregation.base2ExponentialBucketHistogram().equals(aggregation)) {
      Integer maxBuckets;
      try {
        maxBuckets = getAsType(aggregationArgs, "max_buckets", Integer.class);
      } catch (IllegalStateException e) {
        throw new ConfigurationException("max_buckets must be an integer", e);
      }
      // TODO: support configuring max_scale
      if (maxBuckets != null) {
        return Aggregation.base2ExponentialBucketHistogram(maxBuckets, 20);
      }
    }
    return aggregation;
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private static List<Double> getBucketBoundaries(Map<String, Object> aggregationArgs) {
    List<Object> boundaryObjects =
        ((List<Object>) getAsType(aggregationArgs, "bucket_boundaries", List.class));
    if (boundaryObjects == null) {
      return null;
    }
    return boundaryObjects.stream()
        .map(
            o -> {
              if (!(o instanceof Number)) {
                throw new ConfigurationException("bucket_boundaries must be an array of numbers");
              }
              return ((Number) o).doubleValue();
            })
        .collect(toList());
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
    String instrumentUnit = selectorSpec.getInstrumentUnit();
    if (instrumentUnit != null) {
      builder.setUnit(instrumentUnit);
    }

    String meterName = selectorSpec.getMeterName();
    if (meterName != null) {
      builder.setMeterName(meterName);
    }
    String meterVersion = selectorSpec.getMeterVersion();
    if (meterVersion != null) {
      builder.setMeterVersion(meterVersion);
    }
    String meterSchemaUrl = selectorSpec.getMeterSchemaUrl();
    if (meterSchemaUrl != null) {
      builder.setMeterSchemaUrl(meterSchemaUrl);
    }

    return builder.build();
  }
}
