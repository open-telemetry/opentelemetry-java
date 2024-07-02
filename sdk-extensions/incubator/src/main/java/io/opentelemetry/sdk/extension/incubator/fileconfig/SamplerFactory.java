/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.StructuredConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.NamedSpiManager;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.JaegerRemote;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ParentBased;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceIdRatioBased;
import io.opentelemetry.sdk.trace.samplers.ParentBasedSamplerBuilder;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

final class SamplerFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Sampler, Sampler> {

  private static final SamplerFactory INSTANCE = new SamplerFactory();

  private SamplerFactory() {}

  static SamplerFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Sampler create(
      @Nullable io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Sampler model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    if (model == null) {
      return Sampler.parentBased(Sampler.alwaysOn());
    }

    if (model.getAlwaysOn() != null) {
      return Sampler.alwaysOn();
    }
    if (model.getAlwaysOff() != null) {
      return Sampler.alwaysOff();
    }
    TraceIdRatioBased traceIdRatioBasedModel = model.getTraceIdRatioBased();
    if (traceIdRatioBasedModel != null) {
      Double ratio = traceIdRatioBasedModel.getRatio();
      if (ratio == null) {
        ratio = 1.0d;
      }
      return Sampler.traceIdRatioBased(ratio);
    }
    ParentBased parentBasedModel = model.getParentBased();
    if (parentBasedModel != null) {
      Sampler root =
          parentBasedModel.getRoot() == null
              ? Sampler.alwaysOn()
              : create(parentBasedModel.getRoot(), spiHelper, closeables);
      ParentBasedSamplerBuilder builder = Sampler.parentBasedBuilder(root);
      if (parentBasedModel.getRemoteParentSampled() != null) {
        builder.setRemoteParentSampled(
            create(parentBasedModel.getRemoteParentSampled(), spiHelper, closeables));
      }
      if (parentBasedModel.getRemoteParentNotSampled() != null) {
        builder.setRemoteParentNotSampled(
            create(parentBasedModel.getRemoteParentNotSampled(), spiHelper, closeables));
      }
      if (parentBasedModel.getLocalParentSampled() != null) {
        builder.setLocalParentSampled(
            create(parentBasedModel.getLocalParentSampled(), spiHelper, closeables));
      }
      if (parentBasedModel.getLocalParentNotSampled() != null) {
        builder.setLocalParentNotSampled(
            create(parentBasedModel.getLocalParentNotSampled(), spiHelper, closeables));
      }
      return builder.build();
    }

    JaegerRemote jaegerRemoteModel = model.getJaegerRemote();
    if (jaegerRemoteModel != null) {
      // Translate from file configuration scheme to environment variable scheme. This is ultimately
      // interpreted by JaegerRemoteSamplerProvider, but we want to avoid the dependency on
      // opentelemetry-sdk-extension-jaeger-remote-sampler
      Map<String, String> properties = new HashMap<>();
      if (jaegerRemoteModel.getEndpoint() != null) {
        properties.put("endpoint", jaegerRemoteModel.getEndpoint());
      }
      if (jaegerRemoteModel.getInterval() != null) {
        properties.put("pollingInterval", String.valueOf(jaegerRemoteModel.getInterval()));
      }
      // TODO(jack-berg): determine how to support initial sampler. This is first case where a
      // component configured via SPI has property that isn't available in the environment variable
      // scheme.
      String otelTraceSamplerArg =
          properties.entrySet().stream()
              .map(entry -> entry.getKey() + "=" + entry.getValue())
              .collect(joining(","));

      ConfigProperties configProperties =
          DefaultConfigProperties.createFromMap(
              Collections.singletonMap("otel.traces.sampler.arg", otelTraceSamplerArg));
      return FileConfigUtil.addAndReturn(
          closeables,
          FileConfigUtil.assertNotNull(
              samplerSpiManager(configProperties, spiHelper).getByName("jaeger_remote"),
              "jaeger remote sampler"));
    }

    // TODO(jack-berg): add support for generic SPI samplers
    if (!model.getAdditionalProperties().isEmpty()) {
      throw new StructuredConfigException(
          "Unrecognized sampler(s): "
              + model.getAdditionalProperties().keySet().stream().collect(joining(",", "[", "]")));
    }

    return Sampler.parentBased(Sampler.alwaysOn());
  }

  private static NamedSpiManager<Sampler> samplerSpiManager(
      ConfigProperties config, SpiHelper spiHelper) {
    return spiHelper.loadConfigurable(
        ConfigurableSamplerProvider.class,
        ConfigurableSamplerProvider::getName,
        ConfigurableSamplerProvider::createSampler,
        config);
  }
}
