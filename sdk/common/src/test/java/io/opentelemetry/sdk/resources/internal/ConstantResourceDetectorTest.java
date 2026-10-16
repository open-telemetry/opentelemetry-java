/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class ConstantResourceDetectorTest {

  private static final Resource RESOURCE =
      Resource.create(Attributes.of(AttributeKey.stringKey("k"), "v"));

  @Test
  void constant_unnamed() {
    ResourceDetector detector = new ConstantResourceDetector(null, RESOURCE);

    assertThat(detector.getName()).isNull();
    assertThat(detector.getResource()).isEqualTo(RESOURCE);
    assertThat(detector.shouldReinvoke()).isFalse();
  }

  @Test
  void constant_named() {
    ResourceDetector detector = new ConstantResourceDetector("my-detector", RESOURCE);

    assertThat(detector.getName()).isEqualTo("my-detector");
    assertThat(detector.getResource()).isEqualTo(RESOURCE);
    assertThat(detector.shouldReinvoke()).isFalse();
  }
}
