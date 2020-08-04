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

package io.opentelemetry.sdk.extensions.trace.aws.resource;

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
