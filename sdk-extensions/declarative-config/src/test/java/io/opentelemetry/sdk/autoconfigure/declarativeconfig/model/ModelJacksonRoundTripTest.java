/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableSamplerPropertyModel;
import org.junit.jupiter.api.Test;

/**
 * Locks in the Jackson binding of the generated models. Uses a plain {@link ObjectMapper} to
 * exercise the generated annotations directly, not the production mapper's configuration.
 */
class ModelJacksonRoundTripTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void deserializesThroughBuilderWithSnakeCaseNames() throws Exception {
    // @JsonProperty is on the getter and withX, not the field, so Jackson binds through the
    // builder using the snake_case name.
    BatchSpanProcessorModel model = new BatchSpanProcessorModel().withScheduleDelay(5000);

    String json = mapper.writeValueAsString(model);
    assertThat(json).isEqualTo("{\"schedule_delay\":5000}");

    assertThat(mapper.readValue(json, BatchSpanProcessorModel.class)).isEqualTo(model);
  }

  @Test
  void additionalPropertiesFlattenAndRoundTrip() throws Exception {
    // additionalProperties contents flatten to top-level keys via @JsonAnyGetter/@JsonAnySetter
    // rather than serializing under an "additionalProperties" key.
    ExperimentalComposableSamplerPropertyModel model =
        new ExperimentalComposableSamplerPropertyModel().withAdditionalProperty("foo", "bar");

    String json = mapper.writeValueAsString(model);
    assertThat(json).isEqualTo("{\"foo\":\"bar\"}");

    assertThat(mapper.readValue(json, ExperimentalComposableSamplerPropertyModel.class))
        .isEqualTo(model);
  }
}
