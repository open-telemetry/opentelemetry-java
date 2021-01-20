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

class OsResourceTest {

  private static final OsResource RESOURCE = new OsResource();

  @Test
  @SetSystemProperty(key = "os.name", value = "Linux 4.11")
  void linux() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isEqualTo("LINUX");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "MacOS X 11")
  void macos() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isEqualTo("DARWIN");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "Windows 10")
  void windows() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isEqualTo("WINDOWS");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "FreeBSD 10")
  void freebsd() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isEqualTo("FREEBSD");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "NetBSD 10")
  void netbsd() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isEqualTo("NETBSD");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "OpenBSD 10")
  void openbsd() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isEqualTo("OPENBSD");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "DragonFlyBSD 10")
  void dragonflybsd() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isEqualTo("DRAGONFLYBSD");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "HP-UX 10")
  void hpux() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isEqualTo("HPUX");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "AIX 10")
  void aix() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isEqualTo("AIX");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "Solaris 10")
  void solaris() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isEqualTo("SOLARIS");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "Z/OS 10")
  void zos() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isEqualTo("ZOS");
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "RagOS 10")
  void unknown() {
    Attributes attributes = RESOURCE.getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isNull();
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  void inDefault() {
    Attributes attributes = Resource.getDefault().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotNull();
  }
}
