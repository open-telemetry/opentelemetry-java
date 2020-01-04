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

import static org.springframework.beans.BeanUtils.getPropertyDescriptor;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.util.ClassUtils.resolveClassName;

import io.opentelemetry.exporters.logging.LoggingExporter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.EnvVarResource;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdsGenerator;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.sdk.trace.TracerSdkFactoryProvider;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TracerFactory;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Creates an OpenTelemetry {@link TracerFactory} from the default SDK the Spring way. */
@Component
public class TracerSdkFactoryBean implements FactoryBean<TracerFactory>, InitializingBean {

  private static final String KEY_PROBABIITY = "probability";

  private OpenTelemetryProperties properties;
  private Sampler sampler;
  private Clock clock;
  private IdsGenerator idsGenerator;
  private Resource resource;
  private TracerFactory tracerFactory;
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
   * @param clock the clock
   */
  @Autowired(required = false)
  public void setClock(Clock clock) {
    this.clock = clock;
  }

  /**
   * Sets the Spring-managed OpenTelemetry tracing IDs generator.
   *
   * @param idsGenerator the generator
   */
  @Autowired(required = false)
  public void setIdsGenerator(IdsGenerator idsGenerator) {
    this.idsGenerator = idsGenerator;
  }

  /**
   * Sets the Spring-managed implementation of OpenTelemetry {@link Resource} which populates global
   * application attributes.
   *
   * @param resource the resource populator
   */
  @Autowired(required = false)
  public void setResource(Resource resource) {
    this.resource = resource;
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
    tracerFactory = initializeTracerFactory();
  }

  @Override
  public TracerFactory getObject() {
    if (tracerFactory == null) {
      tracerFactory = initializeTracerFactory();
    }
    return tracerFactory;
  }

  @Override
  public Class<?> getObjectType() {
    return TracerFactory.class;
  }

  private TracerFactory initializeTracerFactory() {
    TracerSdkFactoryProvider provider = new TracerSdkFactoryProvider();
    TracerSdkFactory factory = (TracerSdkFactory) provider.create();
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
    factory.updateActiveTraceConfig(traceConfig);
    constructTracerSharedState();
    if (spanProcessors.isEmpty()) {
      factory.addSpanProcessor(constructSpanExportingProcessor());
    } else {
      for (SpanProcessor processor : spanProcessors) {
        factory.addSpanProcessor(processor);
      }
    }
    return factory;
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
    Sampler sampler = instantiateClass(implClass, Sampler.class);
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
        descriptor.getWriteMethod().invoke(sampler, value);
      } catch (IllegalAccessException | InvocationTargetException cause) {
        throw new BeanCreationException(
            "tracerSampler", "unable to set " + entry.getKey() + " to " + entry.getValue(), cause);
      }
    }
    return sampler;
  }

  private Object constructTracerSharedState() {
    Map<String, Object> state = new HashMap<>();
    state.put("clock", prepareClock());
    state.put("idsGenerator", prepareIdsGenerator());
    state.put("resource", prepareResource());
    return state;
  }

  private Clock prepareClock() {
    if (clock != null) {
      return clock;
    }
    return MillisClock.getInstance();
  }

  private IdsGenerator prepareIdsGenerator() {
    if (idsGenerator != null) {
      return idsGenerator;
    }
    return new DummyIdsGenerator();
  }

  private Resource prepareResource() {
    if (resource != null) {
      return resource;
    }
    return EnvVarResource.getResource();
  }

  private SpanProcessor constructSpanExportingProcessor() {
    SpanExporter spanExporter;
    if (spanExporters.isEmpty()) {
      spanExporter = new LoggingExporter();
    } else {
      spanExporter = spanExporters.get(0);
    }
    return SimpleSpansProcessor.newBuilder(spanExporter)
        .reportOnlySampled(properties.getTracer().isExportSampledOnly())
        .build();
  }

  private static class DummyIdsGenerator implements IdsGenerator {

    @Override
    public SpanId generateSpanId() {
      return SpanId.getInvalid();
    }

    @Override
    public TraceId generateTraceId() {
      return TraceId.getInvalid();
    }
  }
}
