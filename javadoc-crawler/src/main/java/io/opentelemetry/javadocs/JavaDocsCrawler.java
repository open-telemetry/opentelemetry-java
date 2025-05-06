/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javadocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The javadoc.io site relies on someone accessing the page for an artifact version in order to
 * update the contents of the site. This will query Maven Central for all artifacts under
 * io.opentelemetry in order to identify the latest versions. Then it will crawl the associated
 * pages on the javadoc.io site to trigger updates.
 */
public final class JavaDocsCrawler {
  private static final String GROUP = "io.opentelemetry";
  private static final String MAVEN_CENTRAL_BASE_URL =
      "https://search.maven.org/solrsearch/select?q=g:";
  private static final String JAVA_DOCS_BASE_URL = "https://javadoc.io/doc/";
  private static final int PAGE_SIZE = 20;
  private static final int THROTTLE_MS = 500;

  // visible for testing
  static final String JAVA_DOC_DOWNLOADED_TEXT = "Javadoc is being downloaded";

  private static final Logger logger = Logger.getLogger(JavaDocsCrawler.class.getName());
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static void main(String[] args) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    List<Artifact> artifacts = getArtifacts(client);
    if (artifacts.isEmpty()) {
      logger.log(Level.SEVERE, "No artifacts found");
      return;
    }
    logger.info(String.format(Locale.ROOT, "Found %d artifacts", artifacts.size()));

    List<String> updated = crawlJavaDocs(client, artifacts);
    if (updated.isEmpty()) {
      logger.info("No updates were needed");
      return;
    }

    logger.info("Artifacts that triggered updates:\n" + String.join("\n", updated));
  }

  static List<Artifact> getArtifacts(HttpClient client) throws IOException, InterruptedException {
    int start = 0;
    Integer numFound;
    List<Artifact> result = new ArrayList<>();

    do {
      if (start != 0) {
        Thread.sleep(THROTTLE_MS); // try not to DDoS the site, it gets knocked over easily
      }

      Map<?, ?> map = queryMavenCentral(client, start);

      numFound =
          Optional.ofNullable(map)
              .map(mavenResult -> (Map<?, ?>) mavenResult.get("response"))
              .map(response -> (Integer) response.get("numFound"))
              .orElse(null);

      List<Artifact> artifacts = convertToArtifacts(map);
      result.addAll(artifacts);

      start += PAGE_SIZE;
    } while (numFound != null && start < numFound);

    return result;
  }

  private static List<Artifact> convertToArtifacts(Map<?, ?> map) {
    return Optional.ofNullable(map)
        .map(mavenResults -> (Map<?, ?>) mavenResults.get("response"))
        .map(response -> (List<?>) response.get("docs"))
        .map(
            docs -> {
              List<Artifact> artifacts = new ArrayList<>();
              for (Object doc : docs) {
                Map<?, ?> docMap = (Map<?, ?>) doc;
                String artifact = (String) docMap.get("a");
                String version = (String) docMap.get("latestVersion");
                if (artifact != null && version != null) {
                  artifacts.add(new Artifact(artifact, version));
                }
              }
              return artifacts;
            })
        .orElseGet(ArrayList::new);
  }

  private static Map<?, ?> queryMavenCentral(HttpClient client, int start)
      throws IOException, InterruptedException {
    URI uri =
        URI.create(
            String.format(
                Locale.ROOT,
                "%s%s&rows=%d&start=%d&wt=json",
                MAVEN_CENTRAL_BASE_URL,
                GROUP,
                PAGE_SIZE,
                start));

    HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() != 200) {
      logger.log(
          Level.SEVERE,
          "Unexpected response code: " + response.statusCode() + ": " + response.body());
      throw new IOException("Unable to pull Maven central artifacts list");
    }
    return objectMapper.readValue(response.body(), Map.class);
  }

  static List<String> crawlJavaDocs(HttpClient client, List<Artifact> artifacts)
      throws IOException, InterruptedException {
    List<String> updatedArtifacts = new ArrayList<>();

    for (Artifact artifact : artifacts) {
      String[] parts = artifact.getName().split("-");
      StringBuilder path = new StringBuilder();
      path.append(JAVA_DOCS_BASE_URL)
          .append(GROUP)
          .append("/")
          .append(artifact.getName())
          .append("/")
          .append(artifact.getVersion())
          .append("/")
          .append(String.join("/", parts))
          .append("/package-summary.html");

      HttpRequest crawlRequest = HttpRequest.newBuilder(URI.create(path.toString())).GET().build();
      HttpResponse<String> crawlResponse =
          client.send(crawlRequest, HttpResponse.BodyHandlers.ofString());

      // gets a status code 303 when version exists and the site redirects it to use /latest/
      if (crawlResponse.statusCode() != 200 && crawlResponse.statusCode() != 303) {
        logger.log(
            Level.WARNING,
            String.format(
                Locale.ROOT,
                "Crawl failed for %s with status code %d at URL %s\nResponse: %s",
                artifact.getName(),
                crawlResponse.statusCode(),
                path,
                crawlResponse.body()));
        continue;
      }

      if (crawlResponse.body().contains(JAVA_DOC_DOWNLOADED_TEXT)) {
        updatedArtifacts.add(artifact.getName());
      }

      Thread.sleep(THROTTLE_MS); // some light throttling
    }
    return updatedArtifacts;
  }

  private JavaDocsCrawler() {}
}
