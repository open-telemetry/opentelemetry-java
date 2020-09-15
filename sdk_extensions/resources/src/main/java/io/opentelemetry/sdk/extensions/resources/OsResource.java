/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.extensions.resources;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.resources.ResourceProvider;
import javax.annotation.Nullable;

/** {@link ResourceProvider} which provides information about the current operating system. */
public class OsResource extends ResourceProvider {

  private static final String RESOURCE_PROVIDER_NAME = "OsResource";

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

    Attributes.Builder attributes = Attributes.newBuilder();

    String osName = getOs(os);
    if (osName != null) {
      attributes.setAttribute(ResourceAttributes.OS_NAME.key(), osName);
    }

    String version = null;
    try {
      version = System.getProperty("os.version");
    } catch (SecurityException e) {
      // Ignore
    }
    String osDescription = version != null ? os + ' ' + version : os;
    attributes.setAttribute(ResourceAttributes.OS_DESCRIPTION.key(), osDescription);

    return attributes.build();
  }

  @Override
  public String getName() {
    return RESOURCE_PROVIDER_NAME;
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
