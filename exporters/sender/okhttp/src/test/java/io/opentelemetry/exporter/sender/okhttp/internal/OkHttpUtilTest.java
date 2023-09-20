/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OkHttpUtilTest {

  @ParameterizedTest
  @CsvSource({
    "https://one.name/existing/path,extra,https://one.name/existing/path/extra",
    "https://one.name,extra,https://one.name/extra",
    "https://one.name/path/with/slash/,extra,https://one.name/path/with/slash/extra",
    "https://one.name/path/with/slash/,extra/segment,https://one.name/path/with/slash/extra/segment",
    "https://one.name/path/with/slash/,/absoluteextra,https://one.name/path/with/slash/absoluteextra",
  })
  void appendPathToUri(String original, String path, String expected) {
    URI target = URI.create(original);
    assertEquals(expected, OkHttpUtil.appendPathToUri(target, path).toString());
  }
}
