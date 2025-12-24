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
      ParentBasedSamplerBuilder builder = Sampler.parentBasedBuilder(Sampler.alwaysOn());

      builder.setRemoteParentSampled(ratioSampler);
      builder.setRemoteParentSampled(ratioSampler);
      builder.build();

      assertTrue(handler.warnings.stream().anyMatch(msg -> msg.contains("remoteParentSampled")));
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
      ParentBasedSamplerBuilder builder = Sampler.parentBasedBuilder(Sampler.alwaysOn());

      builder.setRemoteParentNotSampled(ratioSampler);
      builder.setRemoteParentNotSampled(ratioSampler);

      builder.setLocalParentSampled(ratioSampler);
      builder.setLocalParentSampled(ratioSampler);

      builder.setLocalParentNotSampled(ratioSampler);
      builder.setLocalParentNotSampled(ratioSampler);

      builder.build();

      assertTrue(handler.warnings.stream().anyMatch(msg -> msg.contains("remoteParentNotSampled")));
      assertTrue(handler.warnings.stream().anyMatch(msg -> msg.contains("localParentSampled")));
      assertTrue(handler.warnings.stream().anyMatch(msg -> msg.contains("localParentNotSampled")));
    } finally {
      logger.removeHandler(handler);
    }
  }

  static final class TestLogHandler extends Handler {

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
