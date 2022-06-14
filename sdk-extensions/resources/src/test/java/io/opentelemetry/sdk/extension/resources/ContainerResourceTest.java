/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import static io.opentelemetry.sdk.extension.resources.ContainerResource.buildResource;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ContainerResourceTest {

  @Test
  void buildResource_Invalid(@TempDir Path tempFolder) throws IOException {
    // invalid containerId (non-hex)
    Path cgroup =
        createCGroup(
            tempFolder.resolve("cgroup1"),
            "13:name=systemd:/podruntime/docker/kubepods/ac679f8a8319c8cf7d38e1adf263bc08d23zzzz");
    assertThat(buildResource(cgroup)).isEqualTo(Resource.empty());

    // unrecognized format (last "-" is after last ".")
    cgroup =
        createCGroup(
            tempFolder.resolve("cgroup1"),
            "13:name=systemd:/podruntime/docker/kubepods/ac679f8.a8319c8cf7d38e1adf263bc08-d23zzzz");
    assertThat(buildResource(cgroup)).isEqualTo(Resource.empty());

    // test invalid file
    cgroup = tempFolder.resolve("DoesNotExist");
    assertThat(buildResource(cgroup)).isEqualTo(Resource.empty());
  }

  @Test
  void buildResource_Valid(@TempDir Path tempFolder) throws IOException {
    // with suffix
    Path cgroup =
        createCGroup(
            tempFolder.resolve("cgroup1"),
            "13:name=systemd:/podruntime/docker/kubepods/ac679f8a8319c8cf7d38e1adf263bc08d23.aaaa");
    assertThat(getContainerId(buildResource(cgroup)))
        .isEqualTo("ac679f8a8319c8cf7d38e1adf263bc08d23");

    // with prefix and suffix
    Path cgroup2 =
        createCGroup(
            tempFolder.resolve("cgroup2"),
            "13:name=systemd:/podruntime/docker/kubepods/crio-dc679f8a8319c8cf7d38e1adf263bc08d23.stuff");
    assertThat(getContainerId(buildResource(cgroup2)))
        .isEqualTo("dc679f8a8319c8cf7d38e1adf263bc08d23");

    // just container id
    Path cgroup3 =
        createCGroup(
            tempFolder.resolve("cgroup3"),
            "13:name=systemd:/pod/d86d75589bf6cc254f3e2cc29debdf85dde404998aa128997a819ff991827356");
    assertThat(getContainerId(buildResource(cgroup3)))
        .isEqualTo("d86d75589bf6cc254f3e2cc29debdf85dde404998aa128997a819ff991827356");

    // with prefix
    Path cgroup4 =
        createCGroup(
            tempFolder.resolve("cgroup4"),
            "//\n"
                + "1:name=systemd:/podruntime/docker/kubepods/docker-dc579f8a8319c8cf7d38e1adf263bc08d23"
                + "2:name=systemd:/podruntime/docker/kubepods/docker-dc579f8a8319c8cf7d38e1adf263bc08d23"
                + "3:name=systemd:/podruntime/docker/kubepods/docker-dc579f8a8319c8cf7d38e1adf263bc08d23");
    assertThat(getContainerId(buildResource(cgroup4)))
        .isEqualTo("dc579f8a8319c8cf7d38e1adf263bc08d23");

    // with two dashes in prefix
    Path cgroup5 =
        createCGroup(
            tempFolder.resolve("cgroup5"),
            "11:perf_event:/kubepods.slice/kubepods-burstable.slice/kubepods-burstable-pod4415fd05_2c0f_4533_909b_f2180dca8d7c.slice/cri-containerd-713a77a26fe2a38ebebd5709604a048c3d380db1eb16aa43aca0b2499e54733c.scope");
    assertThat(getContainerId(buildResource(cgroup5)))
        .isEqualTo("713a77a26fe2a38ebebd5709604a048c3d380db1eb16aa43aca0b2499e54733c");
  }

  private static String getContainerId(Resource resource) {
    return resource.getAttribute(ResourceAttributes.CONTAINER_ID);
  }

  private static Path createCGroup(Path path, String line) throws IOException {
    return Files.write(path, line.getBytes(StandardCharsets.UTF_8));
  }
}
