/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class FileConfigTestUtil {

  private FileConfigTestUtil() {}

  static String createTempFileWithContent(Path dir, String filename, byte[] content)
      throws IOException {
    Path path = dir.resolve(filename);
    Files.write(path, content);
    return path.toString();
  }
}
