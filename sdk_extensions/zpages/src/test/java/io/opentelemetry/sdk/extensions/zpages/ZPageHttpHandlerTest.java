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
    assertThat(ZPageHttpHandler.parseQueryMap(uri)).isEmpty();
  }

  @Test
  void parseNormalQuery() throws URISyntaxException, UnsupportedEncodingException {
    URI uri =
        new URI("http://localhost:8000/tracez/tracez?zspanname=Test&ztype=1&zsubtype=5&noval");
    assertThat(ZPageHttpHandler.parseQueryMap(uri))
        .containsOnly(entry("zspanname", "Test"), entry("ztype", "1"), entry("zsubtype", "5"));
  }
}
