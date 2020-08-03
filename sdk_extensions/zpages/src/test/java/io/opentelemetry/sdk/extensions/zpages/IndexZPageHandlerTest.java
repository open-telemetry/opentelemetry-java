/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
