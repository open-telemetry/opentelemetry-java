package io.opentelemetry.sdk.extension.incubator.metric.viewconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.Test;

public class ViewConfigAdditionalTest {

  @Test
  public void testViewConfigAdditionAttrs() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    AutoConfiguredOpenTelemetrySdk.builder()
        .setResultAsGlobal(false)
        .addPropertiesSupplier(
            () ->
                ImmutableMap.of(
                    "otel.traces.exporter",
                    "none",
                    "otel.metrics.exporter",
                    "none",
                    "otel.experimental.metrics.view.config",
                    "classpath:/view-config-customizer-test.yaml"))
        .addMeterProviderCustomizer(
            (meterProviderBuilder, configProperties) ->
                meterProviderBuilder.registerMetricReader(reader))
        .build()
        .getOpenTelemetrySdk()
        .getSdkMeterProvider()
        .get("test-meter")
        .counterBuilder("counter")
        .buildWithCallback(
            callback -> {
              // Attributes with keys baz and qux should be filtered out
              callback.record(
                  1,
                  Attributes.builder()
                      .build());
            });

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metricData ->
                OpenTelemetryAssertions.assertThat(metricData)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(1)
                                        .hasAttributes(
                                            attributeEntry("foo", "val")))));
  }
}
