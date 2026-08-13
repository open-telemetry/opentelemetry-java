/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.all;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;

/**
 * Verifies that every OSGi bundle whose {@code META-INF/services/} directory registers SPI
 * implementations also declares the corresponding {@code Provide-Capability:
 * osgi.serviceloader;osgi.serviceloader="<spi>"} in its manifest.
 */
class OsgiServiceLoaderManifestTest {

  @Test
  void allOsgiBundlesAdvertiseTheirServiceLoaderRegistrations() throws IOException {
    List<String> lines = Files.readAllLines(Path.of(System.getenv("ARTIFACTS_AND_JARS")));
    // violations: "<baseName>: META-INF/services/<spi> not in Provide-Capability"
    List<String> violations = new ArrayList<>();

    for (String line : lines) {
      String[] parts = line.split(":", 2);
      String baseName = parts[0];
      String absolutePath = parts[1];

      try (JarFile jar = new JarFile(new File(absolutePath))) {
        Manifest manifest = jar.getManifest();
        if (manifest == null) {
          continue;
        }
        Attributes mainAttrs = manifest.getMainAttributes();

        // Only check OSGi bundles.
        String bundleManifestVersion = mainAttrs.getValue("Bundle-ManifestVersion");
        if (bundleManifestVersion == null) {
          continue;
        }

        // Collect all SPI interface names from META-INF/services/.
        List<String> registeredSpis = new ArrayList<>();
        jar.stream()
            .map(JarEntry::getName)
            .filter(name -> name.startsWith("META-INF/services/") && !name.endsWith("/"))
            .forEach(name -> registeredSpis.add(name.substring("META-INF/services/".length())));

        if (registeredSpis.isEmpty()) {
          continue;
        }

        // Parse Provide-Capability for osgi.serviceloader entries.
        String provideCapability = mainAttrs.getValue("Provide-Capability");
        List<String> advertisedSpis = parseOsgiServiceLoaderCapabilities(provideCapability);

        for (String spi : registeredSpis) {
          if (!advertisedSpis.contains(spi)) {
            violations.add(baseName + ": META-INF/services/" + spi + " not in Provide-Capability");
          }
        }
      }
    }

    assertThat(violations)
        .as(
            "OSGi bundles with META-INF/services registrations missing from Provide-Capability.\n"
                + "Add the missing SPI to osgiServiceLoaderProvides in the module's build.gradle.kts.")
        .isEmpty();
  }

  /**
   * Parses the {@code Provide-Capability} manifest header and returns all {@code
   * osgi.serviceloader} service type names.
   *
   * <p>Example: {@code osgi.serviceloader;osgi.serviceloader="com.example.Foo",
   * osgi.serviceloader;osgi.serviceloader="com.example.Bar"} → {@code ["com.example.Foo",
   * "com.example.Bar"]}
   */
  private static List<String> parseOsgiServiceLoaderCapabilities(String provideCapability) {
    List<String> result = new ArrayList<>();
    if (provideCapability == null || provideCapability.isEmpty()) {
      return result;
    }
    // JarFile already unfolds line-folded headers. Split into individual capability clauses
    // on commas immediately followed by an OSGi namespace (osgi.*).
    String[] clauses = provideCapability.split(",(?=\\s*osgi\\.)");
    for (String clause : clauses) {
      clause = clause.trim();
      if (!clause.startsWith("osgi.serviceloader")) {
        continue;
      }
      // Extract osgi.serviceloader="<value>"
      int eq = clause.indexOf("osgi.serviceloader=\"");
      if (eq < 0) {
        continue;
      }
      int start = eq + "osgi.serviceloader=\"".length();
      int end = clause.indexOf('"', start);
      if (end > start) {
        result.add(clause.substring(start, end));
      }
    }
    return result;
  }
}
