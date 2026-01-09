/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.net.URI;
import org.junit.jupiter.api.Test;

class UriResolveTest {

  @Test
  void resolve() {
    assertThat(URI.create("http://localhost:8080").resolve("path").toString())
        .isEqualTo("http://localhost:8080/path");
  }
}
