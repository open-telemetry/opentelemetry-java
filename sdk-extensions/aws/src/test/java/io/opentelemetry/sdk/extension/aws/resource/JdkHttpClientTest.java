/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import org.junit.ClassRule;
import org.junit.Test;

public class JdkHttpClientTest {

  @ClassRule
  public static WireMockClassRule server = new WireMockClassRule(wireMockConfig().dynamicPort());

  @Test
  public void testFetchString() throws IOException {
    stubFor(any(urlPathEqualTo("/path")).willReturn(ok("expected result")));

    Map<String, String> requestPropertyMap = ImmutableMap.of("key1", "value1", "key2", "value2");
    String urlStr = String.format("http://localhost:%s%s", server.port(), "/path");
    JdkHttpClient jdkHttpClient = new JdkHttpClient();
    String result = jdkHttpClient.fetchString("GET", urlStr, requestPropertyMap, null);

    assertThat(result).isEqualTo("expected result");
    verify(getRequestedFor(urlEqualTo("/path")).withHeader("key1", equalTo("value1")));
    verify(getRequestedFor(urlEqualTo("/path")).withHeader("key2", equalTo("value2")));
  }

  @Test
  public void testFailedFetchString() {
    Map<String, String> requestPropertyMap = ImmutableMap.of("key1", "value1", "key2", "value2");
    String urlStr = String.format("http://localhost:%s%s", server.port(), "/path");
    JdkHttpClient jdkHttpClient = new JdkHttpClient();
    String result = jdkHttpClient.fetchString("GET", urlStr, requestPropertyMap, null);
    assertThat(result).isEmpty();
  }
}
