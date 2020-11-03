/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.zpages;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ZPageServer}. */
public final class ZPageServerTest {
  @Test
  void tracezSpanProcessorOnlyAddedOnce() throws IOException {
    // tracezSpanProcessor is not added yet
    assertThat(ZPageServer.getIsTracezSpanProcesserAdded()).isFalse();
    HttpServer server = null;
    try {
      server = HttpServer.create(new InetSocketAddress(0), 5);
      ZPageServer.registerAllPagesToHttpServer(server);
      // tracezSpanProcessor is added
      assertThat(ZPageServer.getIsTracezSpanProcesserAdded()).isTrue();
    } finally {
      if (server != null) {
        server.stop(0);
      }
    }
  }
}
