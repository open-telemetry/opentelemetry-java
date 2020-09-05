/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import org.junit.jupiter.api.Test;

class ProcessResourceTest {

  private static final ProcessResource RESOURCE = new ProcessResource();

  @Test
  void normal() {
    Attributes attributes = RESOURCE.getAttributes();

    assertThat(attributes.get(ResourceAttributes.PROCESS_PID.key()).getLongValue())
        .isGreaterThan(1);
    assertThat(attributes.get(ResourceAttributes.PROCESS_EXECUTABLE_PATH.key()).getStringValue())
        .contains("java");
    assertThat(attributes.get(ResourceAttributes.PROCESS_COMMAND_LINE.key()).getStringValue())
        .contains(
            attributes.get(ResourceAttributes.PROCESS_EXECUTABLE_PATH.key()).getStringValue());
  }

  @Test
  void inDefault() {
    ReadableAttributes attributes = Resource.getDefault().getAttributes();
    assertThat(attributes.get(ResourceAttributes.PROCESS_PID.key())).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_EXECUTABLE_PATH.key())).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_COMMAND_LINE.key())).isNotNull();
  }
}
