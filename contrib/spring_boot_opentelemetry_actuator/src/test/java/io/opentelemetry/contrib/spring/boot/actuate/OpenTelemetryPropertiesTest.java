/*
 * Copyright 2019, OpenTelemetry Authors
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit tests for {@link OpenTelemetryProperties}. */
public class OpenTelemetryPropertiesTest {

  @Test
  public void shouldSetPropertiesToDefaultsIfNotProvided() {
    OpenTelemetryProperties properties = new OpenTelemetryProperties();
    assertTrue(properties.isEnabled());
    assertEquals(DEFAULT_SAMPLER, properties.getTracer().getSampler().getName());
    assertTrue(properties.getTracer().getSampler().getProperties().isEmpty());
    assertEquals(
        DEFAULT_SPAN_MAX_NUM_ATTRIBUTES, properties.getTracer().getMaxNumberOfAttributes());
    assertEquals(DEFAULT_SPAN_MAX_NUM_EVENTS, properties.getTracer().getMaxNumberOfEvents());
    assertEquals(DEFAULT_SPAN_MAX_NUM_LINKS, properties.getTracer().getMaxNumberOfLinks());
    assertEquals(
        DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT,
        properties.getTracer().getMaxNumberOfAttributesPerEvent());
    assertEquals(
        DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK,
        properties.getTracer().getMaxNumberOfAttributesPerLink());
    assertEquals(DEFAULT_CLOCK, properties.getTracer().getClockImpl());
    assertEquals(DEFAULT_IDS_GENERATOR, properties.getTracer().getIdsGeneratorImpl());
    assertEquals(DEFAULT_RESOURCE, properties.getTracer().getResourceImpl());
    assertEquals(DEFAULT_EXPORT_SAMPLED_ONLY, properties.getTracer().isExportSampledOnly());
    assertEquals(DEFAULT_LOG_SPANS, properties.getTracer().isLogSpans());
  }
}
