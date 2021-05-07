/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetSystemProperty;

class OsResourceTest {

  @Test
  @SetSystemProperty(key = "os.name", value = "Linux 4.11")
  void linux() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE))
        .isEqualTo(ResourceAttributes.OsTypeValues.LINUX);
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "MacOS X 11")
  void macos() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE))
        .isEqualTo(ResourceAttributes.OsTypeValues.DARWIN);
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "Windows 10")
  void windows() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE))
        .isEqualTo(ResourceAttributes.OsTypeValues.WINDOWS);
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "FreeBSD 10")
  void freebsd() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE))
        .isEqualTo(ResourceAttributes.OsTypeValues.FREEBSD);
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "NetBSD 10")
  void netbsd() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE))
        .isEqualTo(ResourceAttributes.OsTypeValues.NETBSD);
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "OpenBSD 10")
  void openbsd() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE))
        .isEqualTo(ResourceAttributes.OsTypeValues.OPENBSD);
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "DragonFlyBSD 10")
  void dragonflybsd() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE))
        .isEqualTo(ResourceAttributes.OsTypeValues.DRAGONFLYBSD);
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "HP-UX 10")
  void hpux() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE))
        .isEqualTo(ResourceAttributes.OsTypeValues.HPUX);
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "AIX 10")
  void aix() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE))
        .isEqualTo(ResourceAttributes.OsTypeValues.AIX);
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "Solaris 10")
  void solaris() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE))
        .isEqualTo(ResourceAttributes.OsTypeValues.SOLARIS);
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "Z/OS 10")
  void zos() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE))
        .isEqualTo(ResourceAttributes.OsTypeValues.Z_OS);
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Test
  @SetSystemProperty(key = "os.name", value = "RagOS 10")
  void unknown() {
    Attributes attributes = OsResource.buildResource().getAttributes();
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isNull();
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotEmpty();
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @ExtendWith(SecurityManagerExtension.class)
  static class SecurityManagerEnabled {
    @Test
    void empty() {
      assertThat(OsResource.buildResource()).isEqualTo(Resource.empty());
    }
  }
}
