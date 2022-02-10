/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.viewconfig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.MeterSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.metrics.view.ViewBuilder;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
 * </pre>
 *
 * <p>Is equivalent to the following configuration:
 *
 * <pre>
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
 *             .build());
 * </pre>
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
      if (viewConfigSpec.getSelectorSpecification() == null) {
        throw new ConfigurationException(
            "Invalid view configuration - empty selector specification.");
      }
      if (viewConfigSpec.getViewSpecification() == null) {
        throw new ConfigurationException("Invalid view configuration - empty view specification.");
      }
    }

    for (ViewConfigSpecification viewConfigSpec : viewConfigSpecs) {
      assert viewConfigSpec.getSelectorSpecification() != null;
      assert viewConfigSpec.getViewSpecification() != null;
      meterProviderBuilder.registerView(
          toInstrumentSelector(viewConfigSpec.getSelectorSpecification()),
          toView(viewConfigSpec.getViewSpecification()));
    }
  }

  // Visible for testing
  static List<ViewConfigSpecification> loadViewConfig(File viewConfigYamlFile) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {
      return mapper.readValue(
          viewConfigYamlFile, new TypeReference<List<ViewConfigSpecification>>() {});
    } catch (IOException e) {
      throw new ConfigurationException(
          "Error loading view config from file: " + viewConfigYamlFile.getAbsolutePath(), e);
    }
  }

  // Visible for testing
  static View toView(ViewSpecification viewSpec) {
    ViewBuilder builder = View.builder();
    Optional.ofNullable(viewSpec.getName()).ifPresent(builder::setName);
    Optional.ofNullable(viewSpec.getDescription()).ifPresent(builder::setDescription);
    Optional.ofNullable(viewSpec.getAggregation())
        .map(ViewConfig::toAggregation)
        .ifPresent(builder::setAggregation);
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
