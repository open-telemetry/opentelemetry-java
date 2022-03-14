/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorageRegistry;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class IdentityTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(MetricStorageRegistry.class);

  private InMemoryMetricReader reader;
  private SdkMeterProviderBuilder builder;

  @BeforeEach
  void setup() {
    reader = InMemoryMetricReader.createDelta();
    builder =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .setMinimumCollectionInterval(Duration.ZERO);
  }

  @Test
  void sameMeterSameInstrumentNoViews() {
    // Instruments are the same if their name, type, value type, description, and unit are all
    // equal.
    SdkMeterProvider meterProvider = builder.build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(20)));

    meterProvider.get("meter2").counterBuilder("counter2").ofDoubles().build().add(10);
    meterProvider.get("meter2").counterBuilder("counter2").ofDoubles().build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter2"))
                    .hasName("counter2")
                    .hasDoubleSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(20)));

    meterProvider
        .get("meter3")
        .counterBuilder("counter3")
        .setDescription("description3")
        .build()
        .add(10);
    meterProvider
        .get("meter3")
        .counterBuilder("counter3")
        .setDescription("description3")
        .build()
        .add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter3"))
                    .hasName("counter3")
                    .hasDescription("description3")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(20)));

    meterProvider.get("meter4").counterBuilder("counter4").setUnit("unit4").build().add(10);
    meterProvider.get("meter4").counterBuilder("counter4").setUnit("unit4").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter4"))
                    .hasName("counter4")
                    .hasUnit("unit4")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(20)));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void sameMeterDifferentInstrumentNoViews() {
    SdkMeterProvider meterProvider = builder.build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter2").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter2")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void differentMeterSameInstrumentNoViews() {
    // Meters are the same if their name, version, and scope are all equals
    SdkMeterProvider meterProvider = builder.build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter2").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter2"))
                    .hasName("counter1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider
        .meterBuilder("meter1")
        .setInstrumentationVersion("version1")
        .build()
        .counterBuilder("counter1")
        .build()
        .add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create("meter1", "version1", null))
                    .hasName("counter1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    meterProvider
        .meterBuilder("meter1")
        .setInstrumentationVersion("version1")
        .build()
        .counterBuilder("counter1")
        .build()
        .add(10);
    meterProvider
        .meterBuilder("meter1")
        .setInstrumentationVersion("version1")
        .setSchemaUrl("schema1")
        .build()
        .counterBuilder("counter1")
        .build()
        .add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create("meter1", "version1", null))
                    .hasName("counter1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create("meter1", "version1", "schema1"))
                    .hasName("counter1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  @SuppressLogger(MetricStorageRegistry.class)
  void sameMeterConflictingInstrumentDescriptionNoViews() {
    // Instruments with the same name but different descriptions are in conflict.
    SdkMeterProvider meterProvider = builder.build();

    meterProvider
        .get("meter1")
        .counterBuilder("counter1")
        .setDescription("description1")
        .build()
        .add(10);
    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData -> {
              assertThat(metricData)
                  .hasInstrumentationScope(forMeter("meter1"))
                  .hasName("counter1")
                  .hasLongSum()
                  .points()
                  .satisfiesExactly(point -> assertThat(point).hasValue(10));
              assertThat(metricData.getDescription()).isBlank();
            });

    assertThat(logs.getEvents())
        .allSatisfy(
            logEvent ->
                assertThat(logEvent.getMessage()).contains("Found duplicate metric definition"))
        .hasSize(1);
  }

  @Test
  @SuppressLogger(MetricStorageRegistry.class)
  void sameMeterConflictingInstrumentUnitNoViews() {
    // Instruments with the same name but different units are in conflict.
    SdkMeterProvider meterProvider = builder.build();

    meterProvider.get("meter1").counterBuilder("counter1").setUnit("unit1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasUnit("unit1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData -> {
              assertThat(metricData)
                  .hasInstrumentationScope(forMeter("meter1"))
                  .hasName("counter1")
                  .hasLongSum()
                  .points()
                  .satisfiesExactly(point -> assertThat(point).hasValue(10));
              assertThat(metricData.getUnit()).isEqualTo("1");
            });

    assertThat(logs.getEvents())
        .allSatisfy(
            logEvent ->
                assertThat(logEvent.getMessage()).contains("Found duplicate metric definition"))
        .hasSize(1);
  }

  @Test
  @SuppressLogger(MetricStorageRegistry.class)
  void sameMeterConflictingInstrumentTypeNoViews() {
    // Instruments with the same name but different types are in conflict.
    SdkMeterProvider meterProvider = builder.build();

    meterProvider.get("meter1").upDownCounterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasLongSum()
                    .isNotMonotonic()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasLongSum()
                    .isMonotonic()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    assertThat(logs.getEvents())
        .allSatisfy(
            logEvent ->
                assertThat(logEvent.getMessage()).contains("Found duplicate metric definition"))
        .hasSize(1);
  }

  @Test
  @SuppressLogger(MetricStorageRegistry.class)
  void sameMeterConflictingInstrumentValueTypeNoViews() {
    // Instruments with the same name but different instrument value types are in conflict.
    SdkMeterProvider meterProvider = builder.build();

    meterProvider.get("meter1").counterBuilder("counter1").ofDoubles().build().add(10);
    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDoubleSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    assertThat(logs.getEvents())
        .allSatisfy(
            logEvent ->
                assertThat(logEvent.getMessage()).contains("Found duplicate metric definition"))
        .hasSize(1);
  }

  @Test
  void sameMeterSameInstrumentSingleView() {
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setDescription("description1").build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(20)));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void differentMeterSameInstrumentSingleView() {
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setDescription("description1").build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter2").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter2"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void sameMeterDifferentInstrumentSingleView() {
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setDescription("description1").build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter2").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter2")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void sameMeterDifferentInstrumentViewSelectingInstrumentName() {
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setName("counter1").build(),
                View.builder().setDescription("description1").build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter2").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData -> {
              assertThat(metricData)
                  .hasInstrumentationScope(forMeter("meter1"))
                  .hasName("counter2")
                  .hasLongSum()
                  .points()
                  .satisfiesExactly(point -> assertThat(point).hasValue(10));
              assertThat(metricData.getDescription()).isBlank();
            });

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void sameMeterDifferentInstrumentViewSelectingInstrumentType() {
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setDescription("description1").build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").upDownCounterBuilder("counter2").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData -> {
              assertThat(metricData)
                  .hasInstrumentationScope(forMeter("meter1"))
                  .hasName("counter2")
                  .hasLongSum()
                  .points()
                  .satisfiesExactly(point -> assertThat(point).hasValue(10));
              assertThat(metricData.getDescription()).isBlank();
            });

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void differentMeterSameInstrumentViewSelectingMeterName() {
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setMeterName("meter1").build(),
                View.builder().setDescription("description1").build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter2").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData -> {
              assertThat(metricData)
                  .hasInstrumentationScope(forMeter("meter2"))
                  .hasName("counter1")
                  .hasLongSum()
                  .points()
                  .satisfiesExactly(point -> assertThat(point).hasValue(10));
              assertThat(metricData.getDescription()).isBlank();
            });

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void differentMeterSameInstrumentViewSelectingMeterVersion() {
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setMeterVersion("version1").build(),
                View.builder().setDescription("description1").build())
            .build();

    meterProvider
        .meterBuilder("meter1")
        .setInstrumentationVersion("version1")
        .build()
        .counterBuilder("counter1")
        .build()
        .add(10);
    meterProvider
        .meterBuilder("meter1")
        .setInstrumentationVersion("version2")
        .build()
        .counterBuilder("counter1")
        .build()
        .add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create("meter1", "version1", null))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData -> {
              assertThat(metricData)
                  .hasInstrumentationScope(
                      InstrumentationScopeInfo.create("meter1", "version2", null))
                  .hasName("counter1")
                  .hasLongSum()
                  .points()
                  .satisfiesExactly(point -> assertThat(point).hasValue(10));
              assertThat(metricData.getDescription()).isBlank();
            });

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void differentMeterSameInstrumentViewSelectingMeterSchema() {
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setMeterSchemaUrl("schema1").build(),
                View.builder().setDescription("description1").build())
            .build();

    meterProvider
        .meterBuilder("meter1")
        .setSchemaUrl("schema1")
        .build()
        .counterBuilder("counter1")
        .build()
        .add(10);
    meterProvider
        .meterBuilder("meter1")
        .setSchemaUrl("schema2")
        .build()
        .counterBuilder("counter1")
        .build()
        .add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create("meter1", null, "schema1"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData -> {
              assertThat(metricData)
                  .hasInstrumentationScope(
                      InstrumentationScopeInfo.create("meter1", null, "schema2"))
                  .hasName("counter1")
                  .hasLongSum()
                  .points()
                  .satisfiesExactly(point -> assertThat(point).hasValue(10));
              assertThat(metricData.getDescription()).isBlank();
            });

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void differentMeterDifferentInstrumentViewSelectingInstrumentNameAndMeterName() {
    // A view selecting based on meter name and instrument name should not affect different
    // instruments in the selected meter, or the same instrument in a different meter.
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setMeterName("meter1").setName("counter1").build(),
                View.builder().setDescription("description1").build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter2").build().add(10);
    meterProvider.get("meter2").upDownCounterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData -> {
              assertThat(metricData)
                  .hasInstrumentationScope(forMeter("meter1"))
                  .hasName("counter2")
                  .hasLongSum()
                  .points()
                  .satisfiesExactly(point -> assertThat(point).hasValue(10));
              assertThat(metricData.getDescription()).isBlank();
            },
            metricData -> {
              assertThat(metricData)
                  .hasInstrumentationScope(forMeter("meter2"))
                  .hasName("counter1")
                  .hasLongSum()
                  .points()
                  .satisfiesExactly(point -> assertThat(point).hasValue(10));
              assertThat(metricData.getDescription()).isBlank();
            });

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  @SuppressLogger(MetricStorageRegistry.class)
  void sameMeterSameInstrumentConflictingViewDescriptions() {
    // Registering multiple views that select the same instrument(s) and change the description
    // produces an identity conflict as description is part of instrument identity.
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setDescription("description1").build())
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setDescription("description2").build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(20)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDescription("description2")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(20)));

    assertThat(logs.getEvents())
        .allSatisfy(
            logEvent ->
                assertThat(logEvent.getMessage()).contains("Found duplicate metric definition"))
        .hasSize(1);
  }

  @Test
  @SuppressLogger(MetricStorageRegistry.class)
  void sameMeterSameInstrumentConflictingViewAggregations() {
    // Registering multiple views that select the same instrument(s) and change the aggregation
    // produces an identity conflict as aggregation is part of instrument identity.
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setAggregation(Aggregation.defaultAggregation()).build())
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setAggregation(Aggregation.lastValue()).build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(20)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasLongGauge()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    assertThat(logs.getEvents())
        .allSatisfy(
            logEvent ->
                assertThat(logEvent.getMessage()).contains("Found duplicate metric definition"))
        .hasSize(1);
  }

  @Test
  @SuppressLogger(MetricStorageRegistry.class)
  void sameMeterDifferentInstrumentConflictingViewName() {
    // A view that selects multiple instruments and sets the name produces an identity conflict. If
    // it could, views could be used to merge compatible instruments, which is out of scope.
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setName("counter-new").build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter2").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter-new")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter-new")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    assertThat(logs.getEvents())
        .allSatisfy(
            logEvent ->
                assertThat(logEvent.getMessage()).contains("Found duplicate metric definition"))
        .hasSize(1);
  }

  @Test
  void differentMeterDifferentInstrumentViewSetsName() {
    // A view can select multiple instruments and set the name without producing a conflict if the
    // instruments belong to different meters.
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setName("counter-new").build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter2").counterBuilder("counter2").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter-new")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter2"))
                    .hasName("counter-new")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    assertThat(logs.getEvents()).hasSize(0);
  }

  @Test
  void sameMeterDifferentInstrumentCompatibleViews() {
    // Multiple views can select the same instrument(s) without conflict if they produce instruments
    // with unique identities. For example, one view might change part of the instrument identity
    // (description, aggregation) and another might change the aggregation and name to avoid
    // identity conflicts.
    SdkMeterProvider meterProvider =
        builder
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder().setDescription("description1").build())
            .registerView(
                InstrumentSelector.builder().setName("counter1").build(),
                View.builder()
                    .setName("counter1-gauge")
                    .setAggregation(Aggregation.lastValue())
                    .build())
            .build();

    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);
    meterProvider.get("meter1").counterBuilder("counter1").build().add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1")
                    .hasDescription("description1")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(20)),
            metricData ->
                assertThat(metricData)
                    .hasInstrumentationScope(forMeter("meter1"))
                    .hasName("counter1-gauge")
                    .hasLongGauge()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(10)));

    assertThat(logs.getEvents()).hasSize(0);
  }

  private static InstrumentationScopeInfo forMeter(String meterName) {
    return InstrumentationScopeInfo.create(meterName);
  }
}
