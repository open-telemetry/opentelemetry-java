/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigTestUtil.createTempFileWithContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;

class FileConfigurationCreateTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @RegisterExtension
  LogCapturer logCapturer =
      LogCapturer.create().captureForLogger(FileConfiguration.class.getName(), Level.TRACE);

  /**
   * Verify each example in <a
   * href="https://github.com/open-telemetry/opentelemetry-configuration/tree/main/examples">open-telemetry/opentelemetry-configuration/examples</a>
   * can pass {@link FileConfiguration#parseAndCreate(InputStream)}.
   */
  @Test
  void parseAndCreate_Examples(@TempDir Path tempDir)
      throws IOException, CertificateEncodingException {
    // Write certificates to temp files
    String certificatePath =
        createTempFileWithContent(
            tempDir, "certificate.cert", serverTls.certificate().getEncoded());
    String clientKeyPath =
        createTempFileWithContent(tempDir, "clientKey.key", clientTls.privateKey().getEncoded());
    String clientCertificatePath =
        createTempFileWithContent(
            tempDir, "clientCertificate.cert", clientTls.certificate().getEncoded());

    File examplesDir = new File(System.getenv("CONFIG_EXAMPLE_DIR"));
    assertThat(examplesDir.isDirectory()).isTrue();

    for (File example : Objects.requireNonNull(examplesDir.listFiles())) {
      // Skip anchors.yaml because support for merge (i.e. "<<: *anchor") was explicitly removed in
      // snakeyaml-engine:
      // https://bitbucket.org/snakeyaml/snakeyaml-engine/issues/18/merge-tag-support
      // As discussed in this issue merge is supported in snakeyaml:
      // https://bitbucket.org/snakeyaml/snakeyaml-engine/issues/14/read-in-yaml-with-merge-then-dump-strips
      // TODO(jack-berg): decide if we should try to support anchors, or remove anchors example from
      // opentelemetry-configuration
      if (example.getName().equals("anchors.yaml")) {
        continue;
      }

      // Rewrite references to cert files in examples
      String exampleContent =
          new String(Files.readAllBytes(example.toPath()), StandardCharsets.UTF_8);
      String rewrittenExampleContent =
          exampleContent
              .replaceAll(
                  "certificate: .*\n", "certificate: " + certificatePath + System.lineSeparator())
              .replaceAll(
                  "client_key: .*\n", "client_key: " + clientKeyPath + System.lineSeparator())
              .replaceAll(
                  "client_certificate: .*\n",
                  "client_certificate: " + clientCertificatePath + System.lineSeparator());
      InputStream is =
          new ByteArrayInputStream(rewrittenExampleContent.getBytes(StandardCharsets.UTF_8));

      // Verify that file can be parsed and interpreted without error
      assertThatCode(() -> cleanup.addCloseable(FileConfiguration.parseAndCreate(is)))
          .as("Example file: " + example.getName())
          .doesNotThrowAnyException();
    }
  }

  @Test
  void parseAndCreate_Exception_CleansUpPartials() {
    // Trigger an exception after some components have been configured by adding a valid batch
    // exporter with OTLP exporter, following by invalid batch exporter which references invalid
    // exporter "foo".
    String yaml =
        "file_format: \"0.3\"\n"
            + "logger_provider:\n"
            + "  processors:\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          otlp: {}\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          foo: {}\n";

    assertThatThrownBy(
            () ->
                FileConfiguration.parseAndCreate(
                    new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.sdk.logs.export.LogRecordExporter with name \"foo\".");
    logCapturer.assertContains(
        "Error encountered interpreting model. Closing partially configured components.");
    logCapturer.assertContains(
        "Closing io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter");
    logCapturer.assertContains("Closing io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor");
  }
}
