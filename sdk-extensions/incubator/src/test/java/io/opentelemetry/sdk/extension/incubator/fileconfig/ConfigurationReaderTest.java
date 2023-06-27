/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Attributes;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogrecordLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpentelemetryConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Resource;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Sampler;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProvider;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ConfigurationReaderTest {

  @Test
  void read_ExampleFile() throws IOException {
    OpentelemetryConfiguration expected = new OpentelemetryConfiguration();

    expected.setFileFormat("0.1");

    Resource resource = new Resource();
    expected.setResource(resource);
    Attributes attributes = new Attributes();
    resource.setAttributes(attributes);
    attributes.setServiceName("unknown_service");

    AttributeLimits attributeLimits = new AttributeLimits();
    expected.setAttributeLimits(attributeLimits);
    attributeLimits.setAttributeValueLengthLimit(4096);
    attributeLimits.setAttributeCountLimit(128);

    TracerProvider tracerProvider = new TracerProvider();
    expected.setTracerProvider(tracerProvider);

    SpanLimits spanLimits = new SpanLimits();
    tracerProvider.setSpanLimits(spanLimits);
    spanLimits.setAttributeValueLengthLimit(4096);
    spanLimits.setAttributeCountLimit(128);
    spanLimits.setEventCountLimit(128);
    spanLimits.setLinkCountLimit(128);
    spanLimits.setEventAttributeCountLimit(128);
    spanLimits.setLinkAttributeCountLimit(128);

    Sampler sampler = new Sampler();
    tracerProvider.setSampler(sampler);
    sampler.setId("parent_based");
    Sampler rootSampler = new Sampler();
    rootSampler.setId("trace_id_ratio_based");
    sampler.setAdditionalProperty("root", ImmutableMap.of(
        "id", "trace_id_ratio_based",
        "ratio", 0.0001
    ));
    sampler.setAdditionalProperty("remote_parent_sampled", ImmutableMap.of(
        "id", "always_on"
    ));
    sampler.setAdditionalProperty("remote_parent_not_sampled", ImmutableMap.of(
        "id", "always_off"
    ));
    sampler.setAdditionalProperty("local_parent_sampled", ImmutableMap.of(
        "id", "always_on"
    ));
    sampler.setAdditionalProperty("local_parent_not_sampled", ImmutableMap.of(
        "id", "always_off"
    ));

    expected.setMeterProvider(new MeterProvider());

    LoggerProvider loggerProvider = new LoggerProvider();
    expected.setLoggerProvider(loggerProvider);

    LogrecordLimits logRecordLimits = new LogrecordLimits();
    loggerProvider.setLogrecordLimits(logRecordLimits);
    logRecordLimits.setAttributeValueLengthLimit(4096);
    logRecordLimits.setAttributeCountLimit(128);

    try (FileInputStream configExampleFile =
        new FileInputStream(System.getenv("CONFIG_EXAMPLE_FILE"))) {
      OpentelemetryConfiguration configuration = ConfigurationReader.parse(configExampleFile);

      assertThat(configuration.getTracerProvider().getSampler()).isEqualTo(sampler);

      // assertThat(configuration).isEqualTo(expected);
    }
  }
}
