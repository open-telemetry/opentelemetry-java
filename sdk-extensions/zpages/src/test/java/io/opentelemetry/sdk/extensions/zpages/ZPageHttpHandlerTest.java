/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.zpages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ZPageHttpHandler}. */
public final class ZPageHttpHandlerTest {
  @Test
  void parseEmptyQuery() throws URISyntaxException, UnsupportedEncodingException {
    URI uri = new URI("http://localhost:8000/tracez");
    String queryString = "";
    assertThat(ZPageHttpHandler.parseQueryString(uri.getRawQuery())).isEmpty();
    assertThat(ZPageHttpHandler.parseQueryString(queryString)).isEmpty();
  }

  @Test
  void parseNormalQuery() throws URISyntaxException, UnsupportedEncodingException {
    URI uri =
        new URI("http://localhost:8000/tracez/tracez?zspanname=Test&ztype=1&zsubtype=5&noval");
    String queryString = "zspanname=Test&ztype=1&zsubtype=5&noval";
    assertThat(ZPageHttpHandler.parseQueryString(uri.getRawQuery()))
        .containsOnly(entry("zspanname", "Test"), entry("ztype", "1"), entry("zsubtype", "5"));
    assertThat(ZPageHttpHandler.parseQueryString(queryString))
        .containsOnly(entry("zspanname", "Test"), entry("ztype", "1"), entry("zsubtype", "5"));
  }
}
