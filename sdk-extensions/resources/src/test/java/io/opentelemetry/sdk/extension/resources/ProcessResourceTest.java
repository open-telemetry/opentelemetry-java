/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class ProcessResourceTest {

  private static final Resource RESOURCE = ProcessResource.getInstance();

  @Test
  @SetSystemProperty(key = "os.name", value = "Linux 4.12")
  void notWindows() {
    Attributes attributes = RESOURCE.getAttributes();

    assertThat(attributes.get(ResourceAttributes.PROCESS_PID)).isGreaterThan(1);
    assertThat(attributes.get(ResourceAttributes.PROCESS_EXECUTABLE_PATH))
        .contains("java")
        .doesNotEndWith(".exe");
    assertThat(attributes.get(ResourceAttributes.PROCESS_COMMAND_LINE))
        .contains(attributes.get(ResourceAttributes.PROCESS_EXECUTABLE_PATH));
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "Windows 10")
  void windows() {
    Attributes attributes = RESOURCE.getAttributes();

    assertThat(attributes.get(ResourceAttributes.PROCESS_PID)).isGreaterThan(1);
    assertThat(attributes.get(ResourceAttributes.PROCESS_EXECUTABLE_PATH))
        .contains("java")
        .endsWith(".exe");
    assertThat(attributes.get(ResourceAttributes.PROCESS_COMMAND_LINE))
        .contains(attributes.get(ResourceAttributes.PROCESS_EXECUTABLE_PATH));
  }

  @Test
  void inDefault() {
    Attributes attributes = Resource.getDefault().getAttributes();
    assertThat(attributes.get(ResourceAttributes.PROCESS_PID)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_EXECUTABLE_PATH)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_COMMAND_LINE)).isNotNull();
  }
}
