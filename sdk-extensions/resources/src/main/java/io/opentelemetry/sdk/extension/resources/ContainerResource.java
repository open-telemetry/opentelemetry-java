/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/** Factory for {@link Resource} retrieving Container ID information. */
public final class ContainerResource {

  private static final Logger logger = Logger.getLogger(ContainerResource.class.getName());
  private static final String UNIQUE_HOST_NAME_FILE_NAME = "/proc/self/cgroup";
  private static final Pattern HEX_EXTRACTOR =
      Pattern.compile("^([\\w]*?-)?([a-fA-F0-9]+)(\\.[\\w]*?)?$");
  private static final Resource INSTANCE = buildResource();

  static Resource buildResource() {
    String containerId = extractContainerId(UNIQUE_HOST_NAME_FILE_NAME);

    if (containerId == null) {
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
   * by either '.' or '-'
   *
   * <p>We see this with CRI-O "crio-abcdef1234567890ABCDEF.freetext", then use {@linkplain
   * ContainerResource#HEX_EXTRACTOR} to extract the container hex id
   *
   * <p>package private for testing purposes
   *
   * @return containerId
   */
  @Nullable
  @SuppressWarnings("DefaultCharset")
  static String extractContainerId(String cgroupFilePath) {
    File nameFile = new File(cgroupFilePath);
    if (!nameFile.exists() || !nameFile.canRead()) {
      return null;
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(nameFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isEmpty()) {
          continue;
        }
        // This cgroup output line should have the container id in it
        String[] sections = line.split(File.separator);
        if (sections.length <= 1) {
          continue;
        }
        String lastSection = sections[sections.length - 1];
        Matcher matcher = HEX_EXTRACTOR.matcher(lastSection);
        if (matcher.matches() && matcher.group(2) != null && !matcher.group(2).isEmpty()) {
          return matcher.group(2);
        }
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to read file: " + e.getMessage());
    }
    return null;
  }

  private ContainerResource() {}
}
