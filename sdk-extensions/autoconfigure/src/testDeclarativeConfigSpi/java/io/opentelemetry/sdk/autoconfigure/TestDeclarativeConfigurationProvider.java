/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfigurationParser;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfigurationProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class TestDeclarativeConfigurationProvider implements DeclarativeConfigurationProvider {
  @Override
  public OpenTelemetryConfigurationModel getConfigurationModel() {
    String yaml =
        "file_format: \"1.0\"\n"
            + "resource:\n"
            + "  detection/development:\n"
            + "    detectors:\n"
            + "      - test:\n"
            + "  attributes:\n"
            + "    - name: service.name\n"
            + "      value: test\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - simple:\n"
            + "        exporter:\n"
            + "          console: {}\n";

    return DeclarativeConfigurationParser.parse(
        new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
  }
}
