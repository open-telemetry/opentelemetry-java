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
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The javadoc.io site relies on someone accessing the page for an artifact version in order to
 * update the contents of the site. This will query Maven Central for all artifacts under
 * io.opentelemetry in order to identify the latest versions. Then it will crawl the associated
 * pages on the javadoc.io site to trigger updates.
 */
public final class JavaDocsCrawler {
  // Track list of groups and the minimum artifact versions that should be crawled. Update to the
  // latest periodically to avoid crawling artifacts that stopped being published.
  private static final Map<String, String> GROUPS_AND_MIN_VERSION =
      Map.of(
          "io.opentelemetry", "1.49.0",
          "io.opentelemetry.instrumentation", "2.15.0",
          "io.opentelemetry.contrib", "1.46.0",
          "io.opentelemetry.semconv", "1.32.0",
          "io.opentelemetry.proto", "1.3.2");

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

    for (Map.Entry<String, String> groupAndMinVersion : GROUPS_AND_MIN_VERSION.entrySet()) {
      String group = groupAndMinVersion.getKey();

      List<Artifact> artifacts = getArtifacts(client, group);
      if (artifacts.isEmpty()) {
        logger.log(Level.SEVERE, "No artifacts found for group " + group);
        continue;
      }
      logger.info(
          String.format(Locale.ROOT, "Found %d artifacts for group " + group, artifacts.size()));

      List<Artifact> updated = crawlJavaDocs(client, groupAndMinVersion.getValue(), artifacts);
      if (updated.isEmpty()) {
        logger.info("No updates were needed for group " + group);
        continue;
      }

      logger.info(
          "Artifacts that triggered updates for group "
              + group
              + ":\n"
              + updated.stream().map(Artifact::toString).collect(Collectors.joining("\n")));
    }
  }

  static List<Artifact> getArtifacts(HttpClient client, String group)
      throws IOException, InterruptedException {
    int start = 0;
    Integer numFound;
    List<Artifact> result = new ArrayList<>();

    do {
      if (start != 0) {
        Thread.sleep(THROTTLE_MS); // try not to DDoS the site, it gets knocked over easily
      }

      Map<?, ?> map = queryMavenCentral(client, group, start);

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
                String group = Objects.requireNonNull((String) docMap.get("g"), "g");
                String artifact = Objects.requireNonNull((String) docMap.get("a"), "a");
                String version =
                    Objects.requireNonNull((String) docMap.get("latestVersion"), "latestVersion");
                artifacts.add(new Artifact(Objects.requireNonNull(group), artifact, version));
              }
              return artifacts;
            })
        .orElseGet(ArrayList::new);
  }

  private static Map<?, ?> queryMavenCentral(HttpClient client, String group, int start)
      throws IOException, InterruptedException {
    URI uri =
        URI.create(
            String.format(
                Locale.ROOT,
                "%s%s&rows=%d&start=%d&wt=json",
                MAVEN_CENTRAL_BASE_URL,
                group,
                PAGE_SIZE,
                start));

    HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() != 200) {
      logger.log(
          Level.SEVERE,
          "Unexpected response code "
              + response.statusCode()
              + " for uri: "
              + uri.toASCIIString()
              + "\n"
              + response.body());
      throw new IOException("Unable to pull Maven central artifacts list");
    }
    return objectMapper.readValue(response.body(), Map.class);
  }

  static List<Artifact> crawlJavaDocs(
      HttpClient client, String minVersion, List<Artifact> artifacts)
      throws IOException, InterruptedException {
    List<Artifact> updatedArtifacts = new ArrayList<>();

    for (Artifact artifact : artifacts) {
      if (artifact.getVersion().compareTo(minVersion) < 0) {
        logger.info(
            String.format(
                "Skipping crawling %s due to version %s being less than minVersion %s",
                artifact, artifact.getVersion(), minVersion));
        continue;
      }

      String[] parts = artifact.getName().split("-");
      StringBuilder path = new StringBuilder();
      path.append(JAVA_DOCS_BASE_URL)
          .append(artifact.getGroup())
          .append("/")
          .append(artifact.getName())
          .append("/")
          .append(artifact.getVersion())
          .append("/")
          .append(String.join("/", parts))
          .append("/package-summary.html");

      HttpRequest crawlRequest = HttpRequest.newBuilder(URI.create(path.toString())).GET().build();
      logger.info(String.format("Crawling %s at: %s", artifact, path));
      HttpResponse<String> crawlResponse =
          client.send(crawlRequest, HttpResponse.BodyHandlers.ofString());

      // gets a status code 303 when version exists and the site redirects it to use /latest/
      if (crawlResponse.statusCode() != 200 && crawlResponse.statusCode() != 303) {
        logger.log(
            Level.WARNING,
            String.format(
                Locale.ROOT,
                "Crawl failed for %s with status code %d at URL %s\nResponse: %s",
                artifact,
                crawlResponse.statusCode(),
                path,
                crawlResponse.body()));
        continue;
      }

      if (crawlResponse.body().contains(JAVA_DOC_DOWNLOADED_TEXT)) {
        updatedArtifacts.add(artifact);
      }

      Thread.sleep(THROTTLE_MS); // some light throttling
    }
    return updatedArtifacts;
  }

  private JavaDocsCrawler() {}
}
