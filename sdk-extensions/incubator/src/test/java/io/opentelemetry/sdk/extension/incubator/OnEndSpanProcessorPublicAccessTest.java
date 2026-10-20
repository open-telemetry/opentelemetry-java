/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.extension.incubator.trace.OnEndSpanProcessor;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

// Lives outside the trace package to verify OnEndSpanProcessor.create() is callable by users.
class OnEndSpanProcessorPublicAccessTest {

  @Test
  void createFromOutsidePackage() {
    List<ReadableSpan> endedSpans = new ArrayList<>();
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(OnEndSpanProcessor.create(endedSpans::add))
            .build();

    tracerProvider.get("test").spanBuilder("span").startSpan().end();

    assertThat(endedSpans).hasSize(1);
    assertThat(endedSpans.get(0).getName()).isEqualTo("span");
    tracerProvider.close();
  }
}
