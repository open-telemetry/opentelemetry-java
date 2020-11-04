/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.zpages;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link IndexZPageHandler}. */
public final class IndexZPageHandlerTest {
  private static final ZPageHandler tracezZPageHandler = new TracezZPageHandler(null);
  private final Map<String, String> emptyQueryMap = ImmutableMap.of();

  @Test
  void emitHtmlCorrectly() {
    OutputStream output = new ByteArrayOutputStream();
    IndexZPageHandler indexZPageHandler =
        new IndexZPageHandler(ImmutableList.of(tracezZPageHandler));

    indexZPageHandler.emitHtml(emptyQueryMap, output);

    assertThat(output.toString()).contains("<a href=\"" + tracezZPageHandler.getUrlPath() + "\">");
    assertThat(output.toString()).contains(">" + tracezZPageHandler.getPageName() + "</h2></a>");
    assertThat(output.toString())
        .contains("<p>" + tracezZPageHandler.getPageDescription() + "</p>");
  }
}
