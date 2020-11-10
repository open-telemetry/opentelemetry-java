/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.resources.ResourceProvider;
import javax.annotation.Nullable;

/** {@link ResourceProvider} which provides information about the current operating system. */
public class OsResource extends ResourceProvider {

  @Override
  protected Attributes getAttributes() {
    final String os;
    try {
      os = System.getProperty("os.name");
    } catch (SecurityException t) {
      // Security manager enabled, can't provide much os information.
      return Attributes.empty();
    }

    if (os == null) {
      return Attributes.empty();
    }

    AttributesBuilder attributes = Attributes.builder();

    String osName = getOs(os);
    if (osName != null) {
      attributes.put(ResourceAttributes.OS_NAME, osName);
    }

    String version = null;
    try {
      version = System.getProperty("os.version");
    } catch (SecurityException e) {
      // Ignore
    }
    String osDescription = version != null ? os + ' ' + version : os;
    attributes.put(ResourceAttributes.OS_DESCRIPTION, osDescription);

    return attributes.build();
  }

  @Nullable
  private static String getOs(String os) {
    os = os.toLowerCase();
    if (os.startsWith("windows")) {
      return "WINDOWS";
    } else if (os.startsWith("linux")) {
      return "LINUX";
    } else if (os.startsWith("mac")) {
      return "DARWIN";
    } else if (os.startsWith("freebsd")) {
      return "FREEBSD";
    } else if (os.startsWith("netbsd")) {
      return "NETBSD";
    } else if (os.startsWith("openbsd")) {
      return "OPENBSD";
    } else if (os.startsWith("dragonflybsd")) {
      return "DRAGONFLYBSD";
    } else if (os.startsWith("hp-ux")) {
      return "HPUX";
    } else if (os.startsWith("aix")) {
      return "AIX";
    } else if (os.startsWith("solaris")) {
      return "SOLARIS";
    } else if (os.startsWith("z/os")) {
      return "ZOS";
    }
    return null;
  }
}
