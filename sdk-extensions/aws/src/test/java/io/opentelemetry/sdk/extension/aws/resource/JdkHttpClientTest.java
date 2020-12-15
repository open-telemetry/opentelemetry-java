/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class JdkHttpClientTest {

  @RegisterExtension public static MockWebServerExtension server = new MockWebServerExtension();

  @Test
  void testFetchString() {
    server.enqueue(HttpResponse.of("expected result"));

    Map<String, String> requestPropertyMap = ImmutableMap.of("key1", "value1", "key2", "value2");
    String urlStr = String.format("http://localhost:%s%s", server.httpPort(), "/path");
    JdkHttpClient jdkHttpClient = new JdkHttpClient();
    String result = jdkHttpClient.fetchString("GET", urlStr, requestPropertyMap, null);

    assertThat(result).isEqualTo("expected result");

    AggregatedHttpRequest request1 = server.takeRequest().request();
    assertThat(request1.path()).isEqualTo("/path");
    assertThat(request1.headers().get("key1")).isEqualTo("value1");
    assertThat(request1.headers().get("key2")).isEqualTo("value2");
  }

  @Test
  void testFailedFetchString() {
    Map<String, String> requestPropertyMap = ImmutableMap.of("key1", "value1", "key2", "value2");
    String urlStr = String.format("http://localhost:%s%s", server.httpPort(), "/path");
    JdkHttpClient jdkHttpClient = new JdkHttpClient();
    String result = jdkHttpClient.fetchString("GET", urlStr, requestPropertyMap, null);
    assertThat(result).isEmpty();
  }
}
