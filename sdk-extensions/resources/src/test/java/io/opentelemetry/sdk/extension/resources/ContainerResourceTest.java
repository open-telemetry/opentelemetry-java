/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ContainerResourceTest {

  // Invalid because ID is not a hex string
  private static final String INVALID_CGROUP_LINE_1 =
      "13:name=systemd:/podruntime/docker/kubepods/ac679f8a8319c8cf7d38e1adf263bc08d23zzzz";

  // with suffix
  private static final String CGROUP_LINE_1 =
      "13:name=systemd:/podruntime/docker/kubepods/ac679f8a8319c8cf7d38e1adf263bc08d23.aaaa";
  private static final String EXPECTED_CGROUP_1 = "ac679f8a8319c8cf7d38e1adf263bc08d23";

  // with prefix and suffix
  private static final String CGROUP_LINE_2 =
      "13:name=systemd:/podruntime/docker/kubepods/crio-dc679f8a8319c8cf7d38e1adf263bc08d23.stuff";
  private static final String EXPECTED_CGROUP_2 = "dc679f8a8319c8cf7d38e1adf263bc08d23";

  // just container id
  private static final String CGROUP_LINE_3 =
      "13:name=systemd:/pod/d86d75589bf6cc254f3e2cc29debdf85dde404998aa128997a819ff991827356";
  private static final String EXPECTED_CGROUP_3 =
      "d86d75589bf6cc254f3e2cc29debdf85dde404998aa128997a819ff991827356";

  // with prefix
  private static final String CGROUP_LINE_4 =
      "13:name=systemd:/podruntime/docker/kubepods/docker-dc579f8a8319c8cf7d38e1adf263bc08d23";
  private static final String EXPECTED_CGROUP_4 = "dc579f8a8319c8cf7d38e1adf263bc08d23";

  @SuppressWarnings("DefaultCharset")
  public File createCGroup(File file, String line) throws IOException {
    file.createNewFile();
    try (FileWriter os = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(os)) {
      bw.write("");
      bw.write(line);
      bw.flush();
    }
    return file;
  }

  @Test
  public void testInvalidContainer(@TempDir File tempFolder) throws IOException {
    File cgroup = createCGroup(new File(tempFolder, "cgroup1"), INVALID_CGROUP_LINE_1);
    assertEquals(null, new ContainerResource(cgroup.getPath()).extractContainerId());
  }

  @Test
  public void testContainer(@TempDir File tempFolder) throws IOException {
    File cgroup = createCGroup(new File(tempFolder, "cgroup1"), CGROUP_LINE_1);
    assertEquals(EXPECTED_CGROUP_1, new ContainerResource(cgroup.getPath()).extractContainerId());

    File cgroup2 = createCGroup(new File(tempFolder, "cgroup2"), CGROUP_LINE_2);
    assertEquals(EXPECTED_CGROUP_2, new ContainerResource(cgroup2.getPath()).extractContainerId());

    File cgroup3 = createCGroup(new File(tempFolder, "cgroup3"), CGROUP_LINE_3);
    assertEquals(EXPECTED_CGROUP_3, new ContainerResource(cgroup3.getPath()).extractContainerId());

    File cgroup4 = createCGroup(new File(tempFolder, "cgroup4"), CGROUP_LINE_4);
    assertEquals(EXPECTED_CGROUP_4, new ContainerResource(cgroup4.getPath()).extractContainerId());
  }
}
