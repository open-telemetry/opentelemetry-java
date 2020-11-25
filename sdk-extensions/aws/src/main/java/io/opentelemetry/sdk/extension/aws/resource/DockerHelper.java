/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import com.google.common.annotations.VisibleForTesting;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class DockerHelper {

  private static final Logger logger = Logger.getLogger(DockerHelper.class.getName());
  private static final int CONTAINER_ID_LENGTH = 64;
  private static final String DEFAULT_CGROUP_PATH = "/proc/self/cgroup";

  private final String cgroupPath;

  DockerHelper() {
    this(DEFAULT_CGROUP_PATH);
  }

  @VisibleForTesting
  DockerHelper(String cgroupPath) {
    this.cgroupPath = cgroupPath;
  }

  /**
   * Get docker container id from local cgroup file.
   *
   * @return docker container ID. Empty string if it can`t be found.
   */
  @SuppressWarnings("DefaultCharset")
  public String getContainerId() {
    try (BufferedReader br = new BufferedReader(new FileReader(cgroupPath))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.length() > CONTAINER_ID_LENGTH) {
          return line.substring(line.length() - CONTAINER_ID_LENGTH);
        }
      }
    } catch (FileNotFoundException e) {
      logger.log(Level.WARNING, "Failed to read container id, cgroup file does not exist.");
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to read container id: " + e.getMessage());
    }

    return "";
  }
}
