/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class ParentBasedSamplerBuilderTest {

  @Test
  void emitsWarningWhenTraceIdRatioBasedUsedAsChildSampler() {
    Logger logger = Logger.getLogger(ParentBasedSamplerBuilder.class.getName());
    TestLogHandler handler = new TestLogHandler();
    logger.addHandler(handler);

    try {
      Sampler ratioSampler = Sampler.traceIdRatioBased(0.5);

      Sampler.parentBasedBuilder(Sampler.alwaysOn()).setRemoteParentSampled(ratioSampler).build();

      assertTrue(
          handler.warnings.stream()
              .anyMatch(
                  msg ->
                      msg.contains("TraceIdRatioBasedSampler is being used as a child sampler")));
    } finally {
      logger.removeHandler(handler);
    }
  }

  @Test
  void emitsWarningForAllChildSamplerSetters() {
    Logger logger = Logger.getLogger(ParentBasedSamplerBuilder.class.getName());
    TestLogHandler handler = new TestLogHandler();
    logger.addHandler(handler);

    try {
      Sampler ratioSampler = Sampler.traceIdRatioBased(0.5);

      Sampler.parentBasedBuilder(Sampler.alwaysOn())
          .setRemoteParentNotSampled(ratioSampler)
          .setLocalParentSampled(ratioSampler)
          .setLocalParentNotSampled(ratioSampler)
          .build();

      assertTrue(handler.warnings.stream().anyMatch(msg -> msg.contains("remoteParentNotSampled")));
      assertTrue(handler.warnings.stream().anyMatch(msg -> msg.contains("localParentSampled")));
      assertTrue(handler.warnings.stream().anyMatch(msg -> msg.contains("localParentNotSampled")));
    } finally {
      logger.removeHandler(handler);
    }
  }

  static class TestLogHandler extends Handler {

    final List<String> warnings = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
      if (Level.WARNING.equals(record.getLevel())) {
        warnings.add(record.getMessage());
      }
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}
  }
}
