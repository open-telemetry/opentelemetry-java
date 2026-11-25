/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javadocs;

/** Maven coordinates of an artifact whose Javadoc is crawled. */
public class Artifact {
  private final String group;
  private final String name;
  private final String version;

  public Artifact(String group, String name, String version) {
    this.group = group;
    this.name = name;
    this.version = version;
  }

  public String getGroup() {
    return group;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  @Override
  public String toString() {
    return group + ":" + name + ":" + version;
  }
}
