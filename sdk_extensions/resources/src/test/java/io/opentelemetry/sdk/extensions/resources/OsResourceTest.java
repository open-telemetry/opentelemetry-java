/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import org.junit.jupiter.api.Test;

class OsResourceTest {

  private static final OsResource RESOURCE = new OsResource();

  @Test
  void linux() {
    assumeThat(System.getProperty("os.name").toLowerCase()).startsWith("linux");
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_NAME.key()).getStringValue())
        .isEqualTo("LINUX");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION.key()).getStringValue())
        .isNotEmpty();
  }

  @Test
  void macos() {
    assumeThat(System.getProperty("os.name").toLowerCase()).startsWith("mac");
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_NAME.key()).getStringValue())
        .isEqualTo("DARWIN");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION.key()).getStringValue())
        .isNotEmpty();
  }

  @Test
  void windows() {
    assumeThat(System.getProperty("os.name").toLowerCase()).startsWith("windows");
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_NAME.key()).getStringValue())
        .isEqualTo("WINDOWS");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION.key()).getStringValue())
        .isNotEmpty();
  }

  @Test
  void inDefault() {
    ReadableAttributes attributes = Resource.getDefault().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_NAME.key())).isNotNull();
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION.key())).isNotNull();
  }
}
