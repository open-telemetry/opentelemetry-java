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
  public static final String DEFAULT_CLOCK = "io.opentelemetry.sdk.internal.MillisClock";
  public static final String DEFAULT_IDS_GENERATOR =
      "io.opentelemetry.sdk.trace.RandomIdsGenerator";
  public static final String DEFAULT_RESOURCE = "io.opentelemetry.sdk.resources.EnvVarResource";
  public static final boolean DEFAULT_EXPORT_SAMPLED_ONLY = true;
  public static final boolean DEFAULT_LOG_SPANS = false;

  private boolean enabled = true;
  private Tracer tracer = new Tracer();
  private Meter meter = new Meter();
  private Otelcol otelcol = new Otelcol();

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

  public Otelcol getOtelcol() {
    return otelcol;
  }

  public void setOtelcol(Otelcol otelcol) {
    this.otelcol = otelcol;
  }

  public static final class Tracer {

    private Sampler sampler = new Sampler();
    private int maxNumberOfAttributes = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES;
    private int maxNumberOfEvents = DEFAULT_SPAN_MAX_NUM_EVENTS;
    private int maxNumberOfLinks = DEFAULT_SPAN_MAX_NUM_LINKS;
    private int maxNumberOfAttributesPerEvent = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT;
    private int maxNumberOfAttributesPerLink = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK;
    private String clockImpl = DEFAULT_CLOCK;
    private String idsGeneratorImpl = DEFAULT_IDS_GENERATOR;
    private String resourceImpl = DEFAULT_RESOURCE;
    private boolean exportSampledOnly = DEFAULT_EXPORT_SAMPLED_ONLY;
    private boolean logSpans = DEFAULT_LOG_SPANS;

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

    public String getClockImpl() {
      return clockImpl;
    }

    public void setClockImpl(String clockImpl) {
      this.clockImpl = clockImpl;
    }

    public String getIdsGeneratorImpl() {
      return idsGeneratorImpl;
    }

    public void setIdsGeneratorImpl(String idsGeneratorImpl) {
      this.idsGeneratorImpl = idsGeneratorImpl;
    }

    public String getResourceImpl() {
      return resourceImpl;
    }

    public void setResourceImpl(String resourceImpl) {
      this.resourceImpl = resourceImpl;
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

  public static final class Otelcol {

    private String serviceName = "OpenTelemetry";
    private String endpoint = "localhost:55678";
    private boolean enableConfig = false;
    private boolean useInsecure = true;
    private int retryInterval = 300;
    private int deadline = 10;
    private int interval = 60;

    public String getServiceName() {
      return serviceName;
    }

    public void setServiceName(String serviceName) {
      this.serviceName = serviceName;
    }

    public String getEndpoint() {
      return endpoint;
    }

    public void setEndpoint(String endpoint) {
      this.endpoint = endpoint;
    }

    public boolean isEnableConfig() {
      return enableConfig;
    }

    public void setEnableConfig(boolean enableConfig) {
      this.enableConfig = enableConfig;
    }

    public boolean isUseInsecure() {
      return useInsecure;
    }

    public void setUseInsecure(boolean useInsecure) {
      this.useInsecure = useInsecure;
    }

    public int getRetryInterval() {
      return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
      this.retryInterval = retryInterval;
    }

    public int getDeadline() {
      return deadline;
    }

    public void setDeadline(int deadline) {
      this.deadline = deadline;
    }

    public int getInterval() {
      return interval;
    }

    public void setInterval(int interval) {
      this.interval = interval;
    }
  }
}
