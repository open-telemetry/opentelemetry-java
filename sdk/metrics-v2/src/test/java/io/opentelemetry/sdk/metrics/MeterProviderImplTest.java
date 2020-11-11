/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.attributes.SemanticAttributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MeterProviderImplTest {

  @Test
  void spi() {
    MeterProvider globalMeterProvider = OpenTelemetry.getGlobalMeterProvider();
    assertThat(globalMeterProvider).isInstanceOf(MeterProviderImpl.class);
  }

  @Test
  @Disabled("this exercises the whole pipeline, but isn't a test per se.")
  void pipeline() throws InterruptedException {
    MillisClock clock = MillisClock.getInstance();

    Resource resource = Resource.create(Attributes.of(SemanticAttributes.SERVICE_NAME, "testMe"));

    Accumulator accumulator = new Accumulator(clock);
    Processor processor = new Processor(resource, clock);
    MeterProvider meterProvider = new MeterProviderImpl(accumulator);

    new Controller(
            accumulator,
            processor,
            dataToExport -> {
              System.out.println("dataToExport = " + dataToExport);
              return CompletableResultCode.ofSuccess();
            })
        .start();

    Meter testMeter = meterProvider.get("testMeter");
    LongCounter counter =
        testMeter
            .longCounterBuilder("testCounter")
            .setDescription("for testing")
            .setUnit("units")
            .build();

    for (int i = 0; i < 50; i++) {
      counter.add(22, Labels.empty());
      counter.add(i, Labels.of("counting_up", "true"));
      Thread.sleep(5000);
    }
  }
}
