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

package io.opentelemetry.contrib.spring.boot;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link OpenTelemetryProperties}. */
@RunWith(JUnit4.class)
public class OpenTelemetryPropertiesTest {

  @Test
  public void shouldSetPropertiesToDefaultsIfNotProvided() {
    OpenTelemetryProperties properties = new OpenTelemetryProperties();
    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getTracer().getSampler().getName())
        .isEqualTo(OpenTelemetryProperties.DEFAULT_SAMPLER);
    assertThat(properties.getTracer().getSampler().getProperties().isEmpty()).isTrue();
    assertThat(properties.getTracer().getMaxNumberOfAttributes())
        .isEqualTo(OpenTelemetryProperties.DEFAULT_SPAN_MAX_NUM_ATTRIBUTES);
    assertThat(properties.getTracer().getMaxNumberOfEvents())
        .isEqualTo(OpenTelemetryProperties.DEFAULT_SPAN_MAX_NUM_EVENTS);
    assertThat(properties.getTracer().getMaxNumberOfLinks())
        .isEqualTo(OpenTelemetryProperties.DEFAULT_SPAN_MAX_NUM_LINKS);
    assertThat(properties.getTracer().getMaxNumberOfAttributesPerEvent())
        .isEqualTo(OpenTelemetryProperties.DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT);
    assertThat(properties.getTracer().getMaxNumberOfAttributesPerLink())
        .isEqualTo(OpenTelemetryProperties.DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK);
    assertThat(properties.getTracer().isExportSampledOnly())
        .isEqualTo(OpenTelemetryProperties.DEFAULT_EXPORT_SAMPLED_ONLY);
    assertThat(properties.getTracer().isLogSpans())
        .isEqualTo(OpenTelemetryProperties.DEFAULT_EXPORT_SPANS);
    assertThat(properties.getTracer().isExportInmemory())
        .isEqualTo(OpenTelemetryProperties.DEFAULT_EXPORT_SPANS);
  }
}
