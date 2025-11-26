/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javadocs;

import static io.opentelemetry.javadocs.JavaDocsCrawler.JAVA_DOC_DOWNLOADED_TEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JavaDocsCrawlerTest {
  @Mock HttpClient mockClient;
  @Mock HttpResponse<Object> mockMavenCentralRequest1;
  @Mock HttpResponse<Object> mockMavenCentralRequest2;
  @Mock HttpResponse<Object> mockJavaDocResponse;

  @Test
  void testGetArtifactsHandlesPagination() throws IOException, InterruptedException {
    String page1Response =
        """
            {
              "response": {
                "numFound": 40,
                "docs": [
                  {"g": "group", "a": "artifact1", "latestVersion": "1.0"},
                  {"g": "group", "a": "artifact2", "latestVersion": "1.1"}
                ]
              }
            }
        """;
    String page2Response =
        """
            {
              "response": {
                "numFound": 40,
                "docs": [
                  {"g": "group", "a": "artifact3", "latestVersion": "2.0"}
                ]
              }
            }
        """;

    when(mockMavenCentralRequest1.body()).thenReturn(page1Response);
    when(mockMavenCentralRequest1.statusCode()).thenReturn(200);
    when(mockMavenCentralRequest2.body()).thenReturn(page2Response);
    when(mockMavenCentralRequest2.statusCode()).thenReturn(200);

    when(mockClient.send(any(), any()))
        .thenReturn(mockMavenCentralRequest1)
        .thenReturn(mockMavenCentralRequest2);

    List<Artifact> artifacts = JavaDocsCrawler.getArtifacts(mockClient, "io.opentelemetry");

    // 2 calls for the pagination
    verify(mockClient, times(2)).send(any(), any());
    assertThat(artifacts.size()).isEqualTo(3);
  }

  @Test
  void testCrawler() throws IOException, InterruptedException {
    Artifact artifact = new Artifact("io.opentelemetry", "opentelemetry-context", "1.49.0");
    ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

    when(mockJavaDocResponse.body()).thenReturn(JAVA_DOC_DOWNLOADED_TEXT);
    when(mockJavaDocResponse.statusCode()).thenReturn(200);

    when(mockClient.send(any(), any())).thenReturn(mockJavaDocResponse);

    List<Artifact> updated = JavaDocsCrawler.crawlJavaDocs(mockClient, "1.49.0", List.of(artifact));

    verify(mockClient, times(1)).send(requestCaptor.capture(), any());

    assertThat(requestCaptor.getValue().uri().toString())
        .isEqualTo(
            "https://javadoc.io/doc/io.opentelemetry/opentelemetry-context/1.49.0/opentelemetry/context/package-summary.html");
    assertThat(updated).containsExactly(artifact);
  }
}
