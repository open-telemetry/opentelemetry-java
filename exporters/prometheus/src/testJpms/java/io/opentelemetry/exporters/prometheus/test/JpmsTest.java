/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.prometheus.test;

import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import org.junit.jupiter.api.Test;

class JpmsTest {

  @Test
  void noLinkageErrors() {
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(PrometheusHttpServer.builder().setPort(0).build())
            .build();
    meterProvider.close();
  }
}
