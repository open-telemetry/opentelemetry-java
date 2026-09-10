/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javadocs;

import static io.opentelemetry.javadocs.JavaDocsCrawler.JAVA_DOC_DOWNLOADED_TEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
  void testGetArtifactsUsesASingleRequest() throws IOException, InterruptedException {
    String response =
        """
            {
              "response": {
                "numFound": 2,
                "docs": [
                  {"g": "group", "a": "artifact1", "latestVersion": "1.0"},
                  {"g": "group", "a": "artifact2", "latestVersion": "1.1"}
                ]
              }
            }
        """;
    ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

    when(mockMavenCentralRequest1.body()).thenReturn(response);
    when(mockMavenCentralRequest1.statusCode()).thenReturn(200);

    when(mockClient.send(any(), any())).thenReturn(mockMavenCentralRequest1);

    List<Artifact> artifacts = JavaDocsCrawler.getArtifacts(mockClient, "io.opentelemetry");

    verify(mockClient, times(1)).send(requestCaptor.capture(), any());

    String uri = requestCaptor.getValue().uri().toString();
    assertThat(uri)
        .startsWith("https://central.sonatype.com/solrsearch/select?q=g:io.opentelemetry");
    assertThat(uri).contains("rows=500");
    assertThat(uri).doesNotContain("start=");
    assertThat(artifacts)
        .extracting(Artifact::getGroup, Artifact::getName, Artifact::getVersion)
        .containsExactly(tuple("group", "artifact1", "1.0"), tuple("group", "artifact2", "1.1"));
  }

  @Test
  void testGetArtifactsFailsOnIncompleteResponse() throws IOException, InterruptedException {
    String response =
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

    when(mockMavenCentralRequest2.body()).thenReturn(response);
    when(mockMavenCentralRequest2.statusCode()).thenReturn(200);

    when(mockClient.send(any(), any())).thenReturn(mockMavenCentralRequest2);

    assertThatThrownBy(() -> JavaDocsCrawler.getArtifacts(mockClient, "io.opentelemetry"))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("io.opentelemetry");
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

  @Test
  void compareVersionsUsesSemanticOrdering() {
    assertThat(compareVersions("1.9.0", "1.49.0")).isLessThan(0);
    assertThat(compareVersions("1.49.0", "1.49.0")).isZero();
    assertThat(compareVersions("1.60.0", "1.49.0")).isGreaterThan(0);
    assertThat(compareVersions("1.60.0-alpha", "1.60.0")).isLessThan(0);
    assertThat(compareVersions("1.0.0-rc.2", "1.0.0-rc.10")).isLessThan(0);
    assertThat(compareVersions("1.0.0-rc.10", "1.0.0")).isLessThan(0);
  }

  @Test
  void crawlSkipsArtifactsBelowMinVersionUsingSemanticComparison()
      throws IOException, InterruptedException {
    Artifact oldArtifact = new Artifact("io.opentelemetry", "opentelemetry-context", "1.9.0");

    List<Artifact> updated =
        JavaDocsCrawler.crawlJavaDocs(mockClient, "1.49.0", List.of(oldArtifact));

    verify(mockClient, never()).send(any(HttpRequest.class), any());
    assertThat(updated).isEmpty();
  }

  private static int compareVersions(String left, String right) {
    return JavaDocsCrawler.SemanticVersion.parse(left)
        .compareTo(JavaDocsCrawler.SemanticVersion.parse(right));
  }
}
