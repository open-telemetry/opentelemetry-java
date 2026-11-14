/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ResourceModel;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Validates that an external consumer can parse, inspect, customize, and create an {@link
 * OpenTelemetrySdk} strictly using the public declarative configuration API surface without
 * importing or relying on any incubator or internal packages.
 */
class PublicApiUsageTest {

  private static final String MINIMAL_CONFIG =
      "file_format: \"1.2\"\n"
          + "resource:\n"
          + "  attributes:\n"
          + "    - name: service.name\n"
          + "      value: test-service\n";

  @Test
  void parseAndCreate_endToEndPublicApiWorkflow() {
    InputStream inputStream =
        new ByteArrayInputStream(MINIMAL_CONFIG.getBytes(StandardCharsets.UTF_8));

    // 1. Parse configuration strictly via public API
    OpenTelemetryConfigurationModel model = DeclarativeConfiguration.parse(inputStream);
    assertThat(model).isNotNull();
    assertThat(model.getFileFormat()).isEqualTo("1.2");

    // 2. Inspect and customize model using only public model methods
    ResourceModel resourceModel = model.getResource();
    assertThat(resourceModel).isNotNull();

    // 3. Create SDK via DeclarativeConfiguration.create(model)
    DeclarativeConfigResult result = DeclarativeConfiguration.create(model);
    assertThat(result).isNotNull();

    Resource resource = result.getResource();
    assertThat(resource.getAttribute(AttributeKey.stringKey("service.name")))
        .isEqualTo("test-service");

    OpenTelemetrySdk sdk = result.getSdk();
    assertThat(sdk).isNotNull();
    try {
      Tracer tracer = sdk.getTracer("test-tracer");
      assertThat(tracer).isNotNull();
    } finally {
      sdk.close();
    }
  }

  @Test
  void parseAndCreate_convenienceMethod() {
    InputStream inputStream =
        new ByteArrayInputStream(MINIMAL_CONFIG.getBytes(StandardCharsets.UTF_8));

    DeclarativeConfigResult result = DeclarativeConfiguration.parseAndCreate(inputStream);
    assertThat(result).isNotNull();

    OpenTelemetrySdk sdk = result.getSdk();
    assertThat(sdk).isNotNull();
    try {
      assertThat(result.getResource().getAttribute(AttributeKey.stringKey("service.name")))
          .isEqualTo("test-service");
    } finally {
      sdk.close();
    }
  }
}
