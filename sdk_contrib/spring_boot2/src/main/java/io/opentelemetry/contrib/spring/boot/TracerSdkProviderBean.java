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

import static org.springframework.beans.BeanUtils.getPropertyDescriptor;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.util.ClassUtils.resolveClassName;

import io.opentelemetry.exporters.logging.LoggingExporter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdsGenerator;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.MultiSpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.TracerProvider;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/** Creates an OpenTelemetry {@link TracerProvider} from the default SDK the Spring way. */
public class TracerSdkProviderBean implements FactoryBean<TracerProvider>, InitializingBean {

  private static final String KEY_PROBABIITY = "probability";

  private OpenTelemetryProperties properties;
  private Sampler sampler;
  private Clock otelClock;
  private IdsGenerator otelIdsGenerator;
  private Resource otelResource;
  private TracerProvider tracerProvider;
  private final List<SpanProcessor> spanProcessors = new ArrayList<>();
  private final List<SpanExporter> spanExporters = new ArrayList<>();

  /**
   * Sets the Spring application properties used to configure the OpenTelemetry SDK.
   *
   * @param properties the properties
   */
  @Autowired
  public void setProperties(OpenTelemetryProperties properties) {
    this.properties = properties;
  }

  /**
   * Sets the Spring-managed OpenTelemetry tracing {@link Sampler} implementation.
   *
   * @param sampler the sampler
   */
  @Autowired(required = false)
  public void setSampler(Sampler sampler) {
    this.sampler = sampler;
  }

  /**
   * Sets the Spring-managed OpenTelemetry clock implementation for generating span timestamps.
   *
   * @param otelClock the clock
   */
  @Autowired
  public void setOtelClock(Clock otelClock) {
    this.otelClock = otelClock;
  }

  /**
   * Sets the Spring-managed OpenTelemetry tracing IDs generator.
   *
   * @param otelIdsGenerator the generator
   */
  @Autowired
  public void setOtelIdsGenerator(IdsGenerator otelIdsGenerator) {
    this.otelIdsGenerator = otelIdsGenerator;
  }

  /**
   * Sets the Spring-managed implementation of OpenTelemetry {@link Resource} which populates global
   * application attributes.
   *
   * @param otelResource the resource populator
   */
  @Autowired
  @Qualifier("otelResource")
  public void setOtelResource(Resource otelResource) {
    this.otelResource = otelResource;
  }

  /**
   * Sets all Spring-managed OpenTelemetry tracing span processors.
   *
   * @param spanProcessors the processors
   */
  @Autowired(required = false)
  public void setSpanProcessors(List<SpanProcessor> spanProcessors) {
    if (spanProcessors != null) {
      this.spanProcessors.clear();
      this.spanProcessors.addAll(spanProcessors);
    }
  }

  /**
   * Sets all Spring-managed OpenTelemetry tracing span exporters.
   *
   * @param spanExporters the exporters
   */
  @Autowired(required = false)
  public void setSpanExporters(List<SpanExporter> spanExporters) {
    if (spanExporters != null) {
      this.spanExporters.clear();
      this.spanExporters.addAll(spanExporters);
    }
  }

  @Override
  public void afterPropertiesSet() {
    tracerProvider = initializeTracerRegistry();
  }

  @Override
  public TracerProvider getObject() {
    if (tracerProvider == null) {
      tracerProvider = initializeTracerRegistry();
    }
    return tracerProvider;
  }

  @Override
  public Class<?> getObjectType() {
    return TracerProvider.class;
  }

  private TracerProvider initializeTracerRegistry() {
    TracerSdkProvider registry =
        TracerSdkProvider.builder()
            .setClock(otelClock)
            .setIdsGenerator(otelIdsGenerator)
            .setResource(otelResource)
            .build();
    TraceConfig traceConfig =
        TraceConfig.getDefault()
            .toBuilder()
            .setSampler(prepareSampler())
            .setMaxNumberOfAttributes(properties.getTracer().getMaxNumberOfAttributes())
            .setMaxNumberOfEvents(properties.getTracer().getMaxNumberOfEvents())
            .setMaxNumberOfLinks(properties.getTracer().getMaxNumberOfLinks())
            .setMaxNumberOfAttributesPerEvent(
                properties.getTracer().getMaxNumberOfAttributesPerEvent())
            .setMaxNumberOfAttributesPerLink(
                properties.getTracer().getMaxNumberOfAttributesPerLink())
            .build();
    registry.updateActiveTraceConfig(traceConfig);
    if (spanProcessors.isEmpty()) {
      registry.addSpanProcessor(constructSpanExportingProcessor());
    } else {
      for (SpanProcessor processor : spanProcessors) {
        registry.addSpanProcessor(processor);
      }
    }
    return registry;
  }

  private Sampler prepareSampler() {
    if (sampler != null) {
      return sampler;
    }
    Sampler result = Samplers.alwaysOff();
    switch (properties.getTracer().getSampler().getName()) {
      case ALWAYS_ON:
        result = Samplers.alwaysOn();
        break;
      case ALWAYS_OFF:
        result = Samplers.alwaysOff();
        break;
      case PROBABILITY:
        result = constructDefaultProbabilitySampler();
        break;
      case CUSTOM:
        result = constructCustomSampler();
        break;
    }
    return result;
  }

  private Sampler constructDefaultProbabilitySampler() {
    Sampler result;
    double probability = 0.0;
    if (properties.getTracer().getSampler().getProperties().containsKey(KEY_PROBABIITY)) {
      probability =
          Double.parseDouble(
              properties.getTracer().getSampler().getProperties().get(KEY_PROBABIITY));
    }
    result = Samplers.probability(probability);
    return result;
  }

  private Sampler constructCustomSampler() {
    Class<?> implClass = resolveClassName(properties.getTracer().getSampler().getImplClass(), null);
    Sampler customSampler = instantiateClass(implClass, Sampler.class);
    for (Map.Entry<String, String> entry :
        properties.getTracer().getSampler().getProperties().entrySet()) {
      PropertyDescriptor descriptor = getPropertyDescriptor(implClass, entry.getKey());
      try {
        Class<?> propType = descriptor.getPropertyType();
        Object value = entry.getValue();
        if (Boolean.class.isAssignableFrom(propType) || Boolean.TYPE.isAssignableFrom(propType)) {
          value = Boolean.valueOf(entry.getValue());
        } else if (Integer.class.isAssignableFrom(propType)
            || Integer.TYPE.isAssignableFrom(propType)) {
          value = Integer.valueOf(entry.getValue());
        } else if (Long.class.isAssignableFrom(propType) || Long.TYPE.isAssignableFrom(propType)) {
          value = Long.valueOf(entry.getValue());
        } else if (Double.class.isAssignableFrom(propType)
            || Double.TYPE.isAssignableFrom(propType)) {
          value = Double.valueOf(entry.getValue());
        } else if (BigDecimal.class.isAssignableFrom(propType)) {
          value = new BigDecimal(entry.getValue());
        }
        descriptor.getWriteMethod().invoke(customSampler, value);
      } catch (IllegalAccessException | InvocationTargetException cause) {
        throw new BeanCreationException(
            "tracerSampler", "unable to set " + entry.getKey() + " to " + entry.getValue(), cause);
      }
    }
    return customSampler;
  }

  private SpanProcessor constructSpanExportingProcessor() {
    SpanExporter spanExporter;
    if (spanExporters.isEmpty()) {
      spanExporter = new LoggingExporter();
    } else if (spanExporters.size() == 1) {
      spanExporter = spanExporters.get(0);
    } else {
      spanExporter = MultiSpanExporter.create(spanExporters);
    }
    return SimpleSpansProcessor.newBuilder(spanExporter)
        .reportOnlySampled(properties.getTracer().isExportSampledOnly())
        .build();
  }
}
