/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import org.junit.jupiter.api.Test;

class OsResourceTest {

  private static final OsResource RESOURCE = new OsResource();

  @Test
  void linux() {
    assumeThat(System.getProperty("os.name").toLowerCase()).startsWith("linux");
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_NAME)).isEqualTo("LINUX");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  void macos() {
    assumeThat(System.getProperty("os.name").toLowerCase()).startsWith("mac");
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_NAME)).isEqualTo("DARWIN");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  void windows() {
    assumeThat(System.getProperty("os.name").toLowerCase()).startsWith("windows");
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_NAME)).isEqualTo("WINDOWS");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  void inDefault() {
    ReadableAttributes attributes = Resource.getDefault().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_NAME)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotNull();
  }
}
