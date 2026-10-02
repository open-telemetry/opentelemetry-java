/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class ResourceDetectorTest {

  private static final Resource RESOURCE =
      Resource.create(Attributes.of(AttributeKey.stringKey("k"), "v"));

  @Test
  void constant_unnamed() {
    ResourceDetector detector = ResourceDetector.constant(RESOURCE);

    assertThat(detector.getName()).isNull();
    assertThat(detector.getResource()).isEqualTo(RESOURCE);
    assertThat(detector.shouldReinvoke()).isFalse();
  }

  @Test
  void constant_named() {
    ResourceDetector detector = ResourceDetector.constant(RESOURCE, "my-detector");

    assertThat(detector.getName()).isEqualTo("my-detector");
    assertThat(detector.getResource()).isEqualTo(RESOURCE);
    assertThat(detector.shouldReinvoke()).isFalse();
  }

  @Test
  void constant_nullResource_throws() {
    assertThatThrownBy(() -> ResourceDetector.constant(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("resource");
    assertThatThrownBy(() -> ResourceDetector.constant(null, "name"))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("resource");
  }

  @Test
  void constant_nullName_throws() {
    assertThatThrownBy(() -> ResourceDetector.constant(RESOURCE, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("name");
  }
}
