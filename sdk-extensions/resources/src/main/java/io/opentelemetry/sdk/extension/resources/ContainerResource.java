/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

/** Factory for {@link Resource} retrieving Container ID information. */
public final class ContainerResource {

  private static final Logger logger = Logger.getLogger(ContainerResource.class.getName());
  private static final Path UNIQUE_HOST_NAME_FILE_NAME = Paths.get("/proc/self/cgroup");
  private static final Pattern HEX_EXTRACTOR = Pattern.compile("^([a-fA-F0-9]+)$");
  private static final Resource INSTANCE = buildResource(UNIQUE_HOST_NAME_FILE_NAME);

  // package private for testing
  static Resource buildResource(Path path) {
    Optional<String> containerId = extractContainerId(path);

    if (containerId.isPresent()) {
      return Resource.create(Attributes.of(ResourceAttributes.CONTAINER_ID, containerId.get()));
    } else {
      return Resource.empty();
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
  @IgnoreJRERequirement
  @Nullable
  @SuppressWarnings("DefaultCharset")
  private static Optional<String> extractContainerId(Path cgroupFilePath) {
    if (!Files.exists(cgroupFilePath) || !Files.isReadable(cgroupFilePath)) {
      return Optional.empty();
    }
    try (Stream<String> lines = Files.lines(cgroupFilePath)) {
      return lines
          .filter(line -> !line.isEmpty())
          .map(line -> getIdFromLine(line))
          .filter(Objects::nonNull)
          .findFirst();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to read file: " + e.getMessage());
    }
    return Optional.empty();
  }

  @SuppressWarnings("SystemOut")
  private static String getIdFromLine(String line) {
    // This cgroup output line should have the container id in it
    System.out.println("Processing: " + line);
    String[] sections = line.split("/");
    if (sections.length <= 1) {
      return null;
    }

    String lastSection = sections[sections.length - 1];
    int startIdx = lastSection.indexOf("-");
    int endIdx = lastSection.lastIndexOf(".");

    Matcher matcher =
        HEX_EXTRACTOR.matcher(
            lastSection.substring(
                startIdx == -1 ? 0 : startIdx + 1, endIdx == -1 ? lastSection.length() : endIdx));
    if (matcher.matches() && matcher.group(1) != null && !matcher.group(1).isEmpty()) {
      return matcher.group(1);
    }
    return null;
  }

  private ContainerResource() {}
}
