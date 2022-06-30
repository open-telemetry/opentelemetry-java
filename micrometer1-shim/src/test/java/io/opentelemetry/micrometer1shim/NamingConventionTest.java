/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.NamingConvention;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("PreferJavaTimeOverload")
class NamingConventionTest {

  @RegisterExtension
  static final MicrometerTestingExtension testing =
      new MicrometerTestingExtension() {
        @Override
        MeterRegistry configureMeterRegistry(MeterRegistry registry) {
          registry
              .config()
              .namingConvention(
                  new NamingConvention() {
                    @Override
                    public String name(String name, Meter.Type type, String baseUnit) {
                      return "test." + name;
                    }

                    @Override
                    public String tagKey(String key) {
                      return "test." + key;
                    }

                    @Override
                    public String tagValue(String value) {
                      return "test." + value;
                    }
                  });
          return registry;
        }
      };

  final AtomicLong num = new AtomicLong(42);

  @Test
  void renameCounter() {
    Counter counter = Metrics.counter("renamedCounter", "tag", "value");
    counter.increment();

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("test.renamedCounter")
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create(INSTRUMENTATION_NAME, null, null))
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(
                                        attributeEntry("test.tag", "test.value")))));
  }

  @Test
  void renameDistributionSummary() {
    DistributionSummary summary = Metrics.summary("renamedSummary", "tag", "value");
    summary.record(42);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("test.renamedSummary")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(attributeEntry("test.tag", "test.value")))),
            metric ->
                assertThat(metric)
                    .hasName("test.renamedSummary.max")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(
                                        attributeEntry("test.tag", "test.value")))));
  }

  @Test
  void renameFunctionCounter() {
    Metrics.more().counter("renamedFunctionCounter", Tags.of("tag", "value"), num);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("test.renamedFunctionCounter")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(
                                        attributeEntry("test.tag", "test.value")))));
  }

  @Test
  void renameFunctionTimer() {
    Metrics.more()
        .timer(
            "renamedFunctionTimer",
            Tags.of("tag", "value"),
            num,
            AtomicLong::longValue,
            AtomicLong::doubleValue,
            TimeUnit.SECONDS);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("test.renamedFunctionTimer.count")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(attributeEntry("test.tag", "test.value")))),
            metric ->
                assertThat(metric)
                    .hasName("test.renamedFunctionTimer.sum")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(
                                        attributeEntry("test.tag", "test.value")))));
  }

  @Test
  void renameGauge() {
    Metrics.gauge("renamedGauge", Tags.of("tag", "value"), num);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("test.renamedGauge")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(
                                        attributeEntry("test.tag", "test.value")))));
  }

  @Test
  void renameLongTaskTimer() {
    LongTaskTimer timer = Metrics.more().longTaskTimer("renamedLongTaskTimer", "tag", "value");
    timer.start().stop();

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("test.renamedLongTaskTimer.active")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(attributeEntry("test.tag", "test.value")))),
            metric ->
                assertThat(metric)
                    .hasName("test.renamedLongTaskTimer.duration")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(
                                        attributeEntry("test.tag", "test.value")))));
  }

  @Test
  void renameTimer() {
    Timer timer = Metrics.timer("renamedTimer", "tag", "value");
    timer.record(10, TimeUnit.SECONDS);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("test.renamedTimer")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(attributeEntry("test.tag", "test.value")))),
            metric ->
                assertThat(metric)
                    .hasName("test.renamedTimer.max")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(
                                        attributeEntry("test.tag", "test.value")))));
  }
}
