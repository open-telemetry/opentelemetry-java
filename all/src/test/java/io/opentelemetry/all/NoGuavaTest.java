/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.all;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSender;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Prevents Guava API usage in published code, even when Guava is available transitively.
 *
 * <p>This replaces the narrower Guava beta-API check, whose annotation inspection fails with javac
 * 25 and {@code --release 8}. Only published JARs are scanned: tests may use Guava, and transitive
 * dependencies are not prohibited.
 */
class NoGuavaTest {

  @ParameterizedTest
  @MethodSource("artifactsAndJars")
  void noGuavaReferences(String artifactId, String absolutePath) throws IOException {
    try (JarFile jarFile = new JarFile(new File(absolutePath))) {
      noClasses()
          .that()
          // futureUnaryCall exposes ListenableFuture. Retain its existing completion semantics.
          .doNotBelongToAnyOf(UpstreamGrpcSender.class)
          .and()
          // Generated into main alongside the test server API; this unused stub returns
          // ListenableFuture. Moving generation to test-only sources is a separate cleanup.
          .doNotHaveFullyQualifiedName(
              "io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2."
                  + "SamplingManagerGrpc$SamplingManagerFutureStub")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.google.common..")
          .as(artifactId + " should not reference Guava except in gRPC future adapters")
          .check(new ClassFileImporter().importJar(jarFile));
    }
  }

  private static Stream<Arguments> artifactsAndJars() throws IOException {
    return Files.readAllLines(Path.of(System.getenv("ARTIFACTS_AND_JARS"))).stream()
        .map(
            line -> {
              String[] parts = line.split(":", 2);
              return Arguments.argumentSet(parts[0], parts[0], parts[1]);
            });
  }
}
