/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.component;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class SpanProcessorComponentProvider implements ComponentProvider<SpanProcessor> {
  @Override
  public Class<SpanProcessor> getType() {
    return SpanProcessor.class;
  }

  @Override
  public String getName() {
    return "test";
  }

  @Override
  public SpanProcessor create(StructuredConfigProperties config) {
    return new TestSpanProcessor(config);
  }

  public static class TestSpanProcessor implements SpanProcessor {

    public final StructuredConfigProperties config;

    private TestSpanProcessor(StructuredConfigProperties config) {
      this.config = config;
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {}

    @Override
    public boolean isStartRequired() {
      return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {}

    @Override
    public boolean isEndRequired() {
      return true;
    }

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }
  }
}
