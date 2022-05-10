/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class GaugeTest {

  @RegisterExtension
  static final MicrometerTestingExtension testing = new MicrometerTestingExtension();

  @Test
  void testGauge() {
    // when
    Gauge gauge =
        Gauge.builder("testGauge", () -> 42)
            .description("This is a test gauge")
            .tags("tag", "value")
            .baseUnit("items")
            .register(Metrics.globalRegistry);

    // then
    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testGauge")
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create(INSTRUMENTATION_NAME, null, null))
                    .hasDescription("This is a test gauge")
                    .hasUnit("items")
                    .hasDoubleGaugeSatisfying(
                        doubleGauge ->
                            doubleGauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(42)
                                        .hasAttributes(attributeEntry("tag", "value")))));

    // when
    Metrics.globalRegistry.remove(gauge);
    assertThat(testing.collectAllMetrics()).isEmpty();
  }

  @Test
  void gaugesWithSameNameAndDifferentTags() {
    Gauge.builder("testGaugeWithTags", () -> 12)
        .description("First description wins")
        .baseUnit("items")
        .tags("tag", "1")
        .register(Metrics.globalRegistry);
    Gauge.builder("testGaugeWithTags", () -> 42)
        .description("ignored")
        .baseUnit("items")
        .tags("tag", "2")
        .register(Metrics.globalRegistry);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("testGaugeWithTags")
                    .hasDescription("First description wins")
                    .hasUnit("items")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasValue(12).hasAttributes(attributeEntry("tag", "1")),
                                point ->
                                    point.hasValue(42).hasAttributes(attributeEntry("tag", "2")))));
  }

  @Test
  void testWeakRefGauge() throws InterruptedException {
    AtomicLong num = new AtomicLong(42);
    Gauge.builder("testWeakRefGauge", num, AtomicLong::get)
        .strongReference(false)
        .register(Metrics.globalRegistry);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("testWeakRefGauge")
                    .hasDoubleGaugeSatisfying(
                        gauge -> gauge.hasPointsSatisfying(point -> point.hasValue(42))));

    WeakReference<AtomicLong> numWeakRef = new WeakReference<>(num);
    num = null;
    awaitGc(numWeakRef);

    assertThat(testing.collectAllMetrics()).isEmpty();
  }

  private static void awaitGc(WeakReference<?> ref) throws InterruptedException {
    while (ref.get() != null) {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      System.gc();
      System.runFinalization();
    }
  }
}
