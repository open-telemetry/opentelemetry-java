/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DockerHelperTest {

  @Test
  void testCgroupFileMissing() {
    DockerHelper dockerHelper = new DockerHelper("a_file_never_existing");
    assertThat(dockerHelper.getContainerId()).isEmpty();
  }

  @Test
  void testContainerIdMissing(@TempDir File tempFolder) throws IOException {
    File file = new File(tempFolder, "no_container_id");
    String content = "13:pids:/\n" + "12:hugetlb:/\n" + "11:net_prio:/";
    Files.write(content.getBytes(Charsets.UTF_8), file);

    DockerHelper dockerHelper = new DockerHelper(file.getPath());
    assertThat(dockerHelper.getContainerId()).isEmpty();
  }

  @Test
  void testGetContainerId(@TempDir File tempFolder) throws IOException {
    File file = new File(tempFolder, "cgroup");
    String expected = "386a1920640799b5bf5a39bd94e489e5159a88677d96ca822ce7c433ff350163";
    String content = "dummy\n11:devices:/ecs/bbc36dd0-5ee0-4007-ba96-c590e0b278d2/" + expected;
    Files.write(content.getBytes(Charsets.UTF_8), file);

    DockerHelper dockerHelper = new DockerHelper(file.getPath());
    assertThat(dockerHelper.getContainerId()).isEqualTo(expected);
  }
}
