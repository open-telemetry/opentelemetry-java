/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ParentBasedSamplerBuilderTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(ParentBasedSamplerBuilder.class);

  @Test
  @SuppressLogger(ParentBasedSamplerBuilder.class)
  void emitsWarningForAllChildSamplerSetters() {
    Sampler ratioSampler = Sampler.traceIdRatioBased(0.5);
    Sampler.parentBasedBuilder(ratioSampler)
        .setRemoteParentNotSampled(ratioSampler)
        .setRemoteParentSampled(ratioSampler)
        .setLocalParentSampled(ratioSampler)
        .setLocalParentNotSampled(ratioSampler)
        .build();

    assertThat(logs.getEvents()).hasSize(5);
    assertThat(logs.getEvents().get(0).getMessage())
        .contains("TraceIdRatioBasedSampler is being used as a child sampler (root)");
    assertThat(logs.getEvents().get(1).getMessage())
        .contains(
            "TraceIdRatioBasedSampler is being used as a child sampler (remoteParentNotSampled)");
    assertThat(logs.getEvents().get(2).getMessage())
        .contains(
            "TraceIdRatioBasedSampler is being used as a child sampler (remoteParentSampled)");
    assertThat(logs.getEvents().get(3).getMessage())
        .contains("TraceIdRatioBasedSampler is being used as a child sampler (localParentSampled)");
    assertThat(logs.getEvents().get(4).getMessage())
        .contains(
            "TraceIdRatioBasedSampler is being used as a child sampler (localParentNotSampled)");
  }
}
