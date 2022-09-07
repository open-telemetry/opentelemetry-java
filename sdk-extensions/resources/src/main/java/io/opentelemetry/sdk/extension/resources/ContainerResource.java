/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

/** Factory for {@link Resource} retrieving Container ID information. */
public final class ContainerResource {

  private static final Logger logger = Logger.getLogger(ContainerResource.class.getName());
  private static final String UNIQUE_HOST_NAME_FILE_NAME = "/proc/self/cgroup";
  private static final Resource INSTANCE = buildSingleton(UNIQUE_HOST_NAME_FILE_NAME);

  @IgnoreJRERequirement
  private static Resource buildSingleton(String uniqueHostNameFileName) {
    // can't initialize this statically without running afoul of animalSniffer on paths
    return buildResource(Paths.get(uniqueHostNameFileName));
  }

  // package private for testing
  static Resource buildResource(Path path) {
    String containerId = extractContainerId(path);

    if (containerId == null || containerId.isEmpty()) {
      return Resource.empty();
    } else {
      return Resource.create(Attributes.of(ResourceAttributes.CONTAINER_ID, containerId));
    }
  }

  /** Returns resource with container information. */
  public static Resource get() {
    return INSTANCE;
  }

  /**
   * Each line of cgroup file looks like "14:name=systemd:/docker/.../... A hex string is expected
   * inside the last section separated by '/' Each segment of the '/' can contain metadata separated
   * by either '.' (at beginning) or '-' (at end)
   *
   * @return containerId
   */
  @IgnoreJRERequirement
  @Nullable
  private static String extractContainerId(Path cgroupFilePath) {
    if (!Files.exists(cgroupFilePath) || !Files.isReadable(cgroupFilePath)) {
      return null;
    }
    try (Stream<String> lines = Files.lines(cgroupFilePath)) {
      Optional<String> value =
          lines
              .filter(line -> !line.isEmpty())
              .map(ContainerResource::getIdFromLine)
              .filter(Objects::nonNull)
              .findFirst();
      if (value.isPresent()) {
        return value.get();
      }
    } catch (Exception e) {
      logger.log(Level.WARNING, "Unable to read file", e);
    }
    return null;
  }

  @Nullable
  private static String getIdFromLine(String line) {
    // This cgroup output line should have the container id in it
    int lastSlashIdx = line.lastIndexOf('/');
    if (lastSlashIdx < 0) {
      return null;
    }

    String containerId;

    String lastSection = line.substring(lastSlashIdx + 1);
    int colonIdx = lastSection.lastIndexOf(':');

    if (colonIdx != -1) {
      // since k8s v1.24+, containerId is divided by the last colon when container is containerd and
      // cgroupDriver is systemd
      // case:
      // 'kubepods-pod87a18a64_b74a_454a_b10b_a4a36059d0a3.slice:cri-containerd:05c48c82caff3be3d7f1e896981dd410e81487538936914f32b624d168de9db0'
      containerId = lastSection.substring(colonIdx + 1);
    } else {
      int startIdx = lastSection.lastIndexOf('-');
      int endIdx = lastSection.lastIndexOf('.');

      startIdx = startIdx == -1 ? 0 : startIdx + 1;
      if (endIdx == -1) {
        endIdx = lastSection.length();
      }
      if (startIdx > endIdx) {
        return null;
      }

      containerId = lastSection.substring(startIdx, endIdx);
    }

    if (OtelEncodingUtils.isValidBase16String(containerId) && !containerId.isEmpty()) {
      return containerId;
    } else {
      return null;
    }
  }

  private ContainerResource() {}
}
