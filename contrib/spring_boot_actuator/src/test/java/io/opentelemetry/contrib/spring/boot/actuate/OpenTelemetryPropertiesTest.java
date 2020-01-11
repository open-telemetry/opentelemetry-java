/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.contrib.spring.boot.actuate;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.contrib.spring.boot.actuate.OpenTelemetryProperties.DEFAULT_CLOCK;
import static io.opentelemetry.contrib.spring.boot.actuate.OpenTelemetryProperties.DEFAULT_EXPORT_SAMPLED_ONLY;
import static io.opentelemetry.contrib.spring.boot.actuate.OpenTelemetryProperties.DEFAULT_IDS_GENERATOR;
import static io.opentelemetry.contrib.spring.boot.actuate.OpenTelemetryProperties.DEFAULT_LOG_SPANS;
import static io.opentelemetry.contrib.spring.boot.actuate.OpenTelemetryProperties.DEFAULT_RESOURCE;
import static io.opentelemetry.contrib.spring.boot.actuate.OpenTelemetryProperties.DEFAULT_SAMPLER;
import static io.opentelemetry.contrib.spring.boot.actuate.OpenTelemetryProperties.DEFAULT_SPAN_MAX_NUM_ATTRIBUTES;
import static io.opentelemetry.contrib.spring.boot.actuate.OpenTelemetryProperties.DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT;
import static io.opentelemetry.contrib.spring.boot.actuate.OpenTelemetryProperties.DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK;
import static io.opentelemetry.contrib.spring.boot.actuate.OpenTelemetryProperties.DEFAULT_SPAN_MAX_NUM_EVENTS;
import static io.opentelemetry.contrib.spring.boot.actuate.OpenTelemetryProperties.DEFAULT_SPAN_MAX_NUM_LINKS;

import org.junit.Test;

/** Unit tests for {@link OpenTelemetryProperties}. */
public class OpenTelemetryPropertiesTest {

  @Test
  public void shouldSetPropertiesToDefaultsIfNotProvided() {
    OpenTelemetryProperties properties = new OpenTelemetryProperties();
    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getTracer().getSampler().getName()).isEqualTo(DEFAULT_SAMPLER);
    assertThat(properties.getTracer().getSampler().getProperties().isEmpty()).isTrue();
    assertThat(properties.getTracer().getMaxNumberOfAttributes())
        .isEqualTo(DEFAULT_SPAN_MAX_NUM_ATTRIBUTES);
    assertThat(properties.getTracer().getMaxNumberOfEvents())
        .isEqualTo(DEFAULT_SPAN_MAX_NUM_EVENTS);
    assertThat(properties.getTracer().getMaxNumberOfLinks()).isEqualTo(DEFAULT_SPAN_MAX_NUM_LINKS);
    assertThat(properties.getTracer().getMaxNumberOfAttributesPerEvent())
        .isEqualTo(DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT);
    assertThat(properties.getTracer().getMaxNumberOfAttributesPerLink())
        .isEqualTo(DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK);
    assertThat(properties.getTracer().getClockImpl()).isEqualTo(DEFAULT_CLOCK);
    assertThat(properties.getTracer().getIdsGeneratorImpl()).isEqualTo(DEFAULT_IDS_GENERATOR);
    assertThat(properties.getTracer().getResourceImpl()).isEqualTo(DEFAULT_RESOURCE);
    assertThat(properties.getTracer().isExportSampledOnly()).isEqualTo(DEFAULT_EXPORT_SAMPLED_ONLY);
    assertThat(properties.getTracer().isLogSpans()).isEqualTo(DEFAULT_LOG_SPANS);
  }
}
