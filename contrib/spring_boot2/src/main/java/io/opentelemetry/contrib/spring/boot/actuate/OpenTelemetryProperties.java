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

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Defines Spring Boot application properties for configurating the OpenTelementry SDK. */
@ConfigurationProperties(prefix = "management.opentelemetry")
public class OpenTelemetryProperties {

  public static final SamplerName DEFAULT_SAMPLER = SamplerName.ALWAYS_ON;
  public static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES = 32;
  public static final int DEFAULT_SPAN_MAX_NUM_EVENTS = 128;
  public static final int DEFAULT_SPAN_MAX_NUM_LINKS = 32;
  public static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT = 32;
  public static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK = 32;
  public static final boolean DEFAULT_EXPORT_SAMPLED_ONLY = true;
  public static final boolean DEFAULT_EXPORT_SPANS = false;

  private boolean enabled = true;
  private Tracer tracer = new Tracer();
  private Meter meter = new Meter();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Tracer getTracer() {
    return tracer;
  }

  public void setTracer(Tracer tracer) {
    this.tracer = tracer;
  }

  public Meter getMeter() {
    return meter;
  }

  public void setMeter(Meter meter) {
    this.meter = meter;
  }

  public static final class Tracer {

    private Sampler sampler = new Sampler();
    private int maxNumberOfAttributes = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES;
    private int maxNumberOfEvents = DEFAULT_SPAN_MAX_NUM_EVENTS;
    private int maxNumberOfLinks = DEFAULT_SPAN_MAX_NUM_LINKS;
    private int maxNumberOfAttributesPerEvent = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT;
    private int maxNumberOfAttributesPerLink = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK;
    private boolean exportSampledOnly = DEFAULT_EXPORT_SAMPLED_ONLY;
    private boolean logSpans = DEFAULT_EXPORT_SPANS;
    private boolean exportInmemory = DEFAULT_EXPORT_SPANS;

    public Sampler getSampler() {
      return sampler;
    }

    public void setSampler(Sampler sampler) {
      this.sampler = sampler;
    }

    public int getMaxNumberOfAttributes() {
      return maxNumberOfAttributes;
    }

    public void setMaxNumberOfAttributes(int maxNumberOfAttributes) {
      this.maxNumberOfAttributes = maxNumberOfAttributes;
    }

    public int getMaxNumberOfEvents() {
      return maxNumberOfEvents;
    }

    public void setMaxNumberOfEvents(int maxNumberOfEvents) {
      this.maxNumberOfEvents = maxNumberOfEvents;
    }

    public int getMaxNumberOfLinks() {
      return maxNumberOfLinks;
    }

    public void setMaxNumberOfLinks(int maxNumberOfLinks) {
      this.maxNumberOfLinks = maxNumberOfLinks;
    }

    public int getMaxNumberOfAttributesPerEvent() {
      return maxNumberOfAttributesPerEvent;
    }

    public void setMaxNumberOfAttributesPerEvent(int maxNumberOfAttributesPerEvent) {
      this.maxNumberOfAttributesPerEvent = maxNumberOfAttributesPerEvent;
    }

    public int getMaxNumberOfAttributesPerLink() {
      return maxNumberOfAttributesPerLink;
    }

    public void setMaxNumberOfAttributesPerLink(int maxNumberOfAttributesPerLink) {
      this.maxNumberOfAttributesPerLink = maxNumberOfAttributesPerLink;
    }

    public boolean isExportSampledOnly() {
      return exportSampledOnly;
    }

    public void setExportSampledOnly(boolean exportSampledOnly) {
      this.exportSampledOnly = exportSampledOnly;
    }

    public boolean isLogSpans() {
      return logSpans;
    }

    public void setLogSpans(boolean logSpans) {
      this.logSpans = logSpans;
    }

    public boolean isExportInmemory() {
      return exportInmemory;
    }

    public void setExportInmemory(boolean exportInmemory) {
      this.exportInmemory = exportInmemory;
    }
  }

  public static final class Sampler {

    private SamplerName name = DEFAULT_SAMPLER;
    private String implClass = "";
    private final Map<String, String> properties = new HashMap<>();

    public SamplerName getName() {
      return name;
    }

    public void setName(SamplerName name) {
      this.name = name;
    }

    public String getImplClass() {
      return implClass;
    }

    public void setImplClass(String implClass) {
      this.implClass = implClass;
    }

    public Map<String, String> getProperties() {
      return properties;
    }
  }

  public static enum SamplerName {
    ALWAYS_ON,
    ALWAYS_OFF,
    PROBABILITY,
    CUSTOM;
  }

  public static final class Meter {

    private boolean export = false;

    public boolean isExport() {
      return export;
    }

    public void setExport(boolean export) {
      this.export = export;
    }
  }
}
