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

package io.opentelemetry.sdk.trace.config;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.trace.Samplers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceConfig}. */
@RunWith(JUnit4.class)
public class TraceConfigTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void defaultTraceConfig() {
    assertThat(TraceConfig.getDefault().getSampler()).isEqualTo(Samplers.alwaysOn());
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributes()).isEqualTo(32);
    assertThat(TraceConfig.getDefault().getMaxNumberOfEvents()).isEqualTo(128);
    assertThat(TraceConfig.getDefault().getMaxNumberOfLinks()).isEqualTo(32);
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent()).isEqualTo(32);
    assertThat(TraceConfig.getDefault().getMaxNumberOfAttributesPerLink()).isEqualTo(32);
  }

  @Test
  public void updateTraceConfig_NullSampler() {
    thrown.expect(NullPointerException.class);
    TraceConfig.getDefault().toBuilder().setSampler(null).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfAttributes() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributes(0).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfEvents() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setMaxNumberOfEvents(0).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfLinks() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setMaxNumberOfLinks(0).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerEvent() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributesPerEvent(0).build();
  }

  @Test
  public void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerLink() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setMaxNumberOfAttributesPerLink(0).build();
  }

  @Test
  public void updateTraceConfig_InvalidSamplerProbability() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setSamplerProbability(2).build();
  }

  @Test
  public void updateTraceConfig_NegativeSamplerProbability() {
    thrown.expect(IllegalArgumentException.class);
    TraceConfig.getDefault().toBuilder().setSamplerProbability(-1).build();
  }

  @Test
  public void updateTraceConfig_OffSamplerProbability() {
    TraceConfig traceConfig = TraceConfig.getDefault().toBuilder().setSamplerProbability(0).build();
    assertThat(traceConfig.getSampler()).isEqualTo(Samplers.alwaysOff());
  }

  @Test
  public void updateTraceConfig_OnSamplerProbability() {
    TraceConfig traceConfig = TraceConfig.getDefault().toBuilder().setSamplerProbability(1).build();
    assertThat(traceConfig.getSampler()).isEqualTo(Samplers.alwaysOn());
  }

  @Test
  public void updateTraceConfig_All() {
    TraceConfig traceConfig =
        TraceConfig.getDefault()
            .toBuilder()
            .setSampler(Samplers.alwaysOff())
            .setMaxNumberOfAttributes(8)
            .setMaxNumberOfEvents(10)
            .setMaxNumberOfLinks(11)
            .setMaxNumberOfAttributesPerEvent(1)
            .setMaxNumberOfAttributesPerLink(2)
            .build();
    assertThat(traceConfig.getSampler()).isEqualTo(Samplers.alwaysOff());
    assertThat(traceConfig.getMaxNumberOfAttributes()).isEqualTo(8);
    assertThat(traceConfig.getMaxNumberOfEvents()).isEqualTo(10);
    assertThat(traceConfig.getMaxNumberOfLinks()).isEqualTo(11);
    assertThat(traceConfig.getMaxNumberOfAttributesPerEvent()).isEqualTo(1);
    assertThat(traceConfig.getMaxNumberOfAttributesPerLink()).isEqualTo(2);
  }

  public static class SystemPropertiesTest {
    @Rule public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
      System.setProperty("otel.config.sampler.probability", "0.3");
      System.setProperty("otel.config.max.attrs", "5");
      System.setProperty("otel.config.max.events", "6");
      System.setProperty("otel.config.max.links", "9");
      System.setProperty("otel.config.max.event.attrs", "7");
      System.setProperty("otel.config.max.link.attrs", "10");
    }

    @After
    public void tearDown() {
      System.clearProperty("otel.config.sampler.probability");
      System.clearProperty("otel.config.max.attrs");
      System.clearProperty("otel.config.max.events");
      System.clearProperty("otel.config.max.links");
      System.clearProperty("otel.config.max.event.attrs");
      System.clearProperty("otel.config.max.link.attrs");
    }

    @Test
    public void updateTraceConfig_SystemProperties() {
      TraceConfig traceConfig = TraceConfig.getDefault();
      assertThat(traceConfig.getSampler()).isEqualTo(Samplers.probability(0.3));
      assertThat(traceConfig.getMaxNumberOfAttributes()).isEqualTo(5);
      assertThat(traceConfig.getMaxNumberOfEvents()).isEqualTo(6);
      assertThat(traceConfig.getMaxNumberOfLinks()).isEqualTo(9);
      assertThat(traceConfig.getMaxNumberOfAttributesPerEvent()).isEqualTo(7);
      assertThat(traceConfig.getMaxNumberOfAttributesPerLink()).isEqualTo(10);
    }

    @Test
    public void updateTraceConfig_InvalidSamplerProbability() {
      System.setProperty("otel.config.sampler.probability", "-1");
      thrown.expect(IllegalArgumentException.class);
      TraceConfig.getDefault();
    }

    @Test
    public void updateTraceConfig_NonPositiveMaxNumberOfAttributes() {
      System.setProperty("otel.config.max.attrs", "-5");
      thrown.expect(IllegalArgumentException.class);
      TraceConfig.getDefault();
    }

    @Test
    public void updateTraceConfig_NonPositiveMaxNumberOfEvents() {
      System.setProperty("otel.config.max.events", "-6");
      thrown.expect(IllegalArgumentException.class);
      TraceConfig.getDefault();
    }

    @Test
    public void updateTraceConfig_NonPositiveMaxNumberOfLinks() {
      System.setProperty("otel.config.max.links", "-9");
      thrown.expect(IllegalArgumentException.class);
      TraceConfig.getDefault();
    }

    @Test
    public void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerEvent() {
      System.setProperty("otel.config.max.event.attrs", "-7");
      thrown.expect(IllegalArgumentException.class);
      TraceConfig.getDefault();
    }

    @Test
    public void updateTraceConfig_NonPositiveMaxNumberOfAttributesPerLink() {
      System.setProperty("otel.config.max.link.attrs", "-10");
      thrown.expect(IllegalArgumentException.class);
      TraceConfig.getDefault();
    }
  }
}
