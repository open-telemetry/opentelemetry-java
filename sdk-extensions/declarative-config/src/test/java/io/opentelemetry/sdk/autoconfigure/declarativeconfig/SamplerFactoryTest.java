/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.component.SamplerComponentProvider;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AlwaysOffSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AlwaysOnSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ParentBasedSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TraceIdRatioBasedSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableAlwaysOffSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableAlwaysOnSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableParentThresholdSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableProbabilitySamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableRuleBasedSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalJaegerRemoteSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.SamplerModelAccessor;
import io.opentelemetry.sdk.extension.incubator.trace.samplers.ComposableSampler;
import io.opentelemetry.sdk.extension.incubator.trace.samplers.CompositeSampler;
import io.opentelemetry.sdk.extension.trace.jaeger.sampler.JaegerRemoteSampler;
import io.opentelemetry.sdk.trace.samplers.ParentBasedSamplerBuilder;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// Suppress logs from JaegerRemoteSampler
@SuppressLogger(
    loggerName = "io.opentelemetry.sdk.extension.trace.jaeger.sampler.OkHttpGrpcService")
@SuppressLogger(ParentBasedSamplerBuilder.class)
@SuppressLogger(JaegerRemoteSampler.class)
class SamplerFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(ComponentLoader.forClassLoader(getClass().getClassLoader()));

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(@Nullable SamplerModel model, Sampler expectedSampler) {
    // // All samplers are Closeable - ensure these get cleaned up
    cleanup.addCloseable(expectedSampler);

    List<Closeable> closeables = new ArrayList<>();
    Sampler sampler = SamplerFactory.getInstance().create(model, context);
    cleanup.addCloseables(closeables);

    assertThat(sampler.toString()).isEqualTo(expectedSampler.toString());
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.argumentSet(
            "always_on",
            new SamplerModel().setAlwaysOn(new AlwaysOnSamplerModel()),
            Sampler.alwaysOn()),
        Arguments.argumentSet(
            "always_off",
            new SamplerModel().setAlwaysOff(new AlwaysOffSamplerModel()),
            Sampler.alwaysOff()),
        Arguments.argumentSet(
            "trace_id_ratio_based default",
            new SamplerModel().setTraceIdRatioBased(new TraceIdRatioBasedSamplerModel()),
            Sampler.traceIdRatioBased(1.0d)),
        Arguments.argumentSet(
            "trace_id_ratio_based with ratio",
            new SamplerModel()
                .setTraceIdRatioBased(new TraceIdRatioBasedSamplerModel().setRatio(0.5d)),
            Sampler.traceIdRatioBased(0.5)),
        Arguments.argumentSet(
            "parent_based default",
            new SamplerModel().setParentBased(new ParentBasedSamplerModel()),
            Sampler.parentBased(Sampler.alwaysOn())),
        Arguments.argumentSet(
            "parent_based with options",
            new SamplerModel()
                .setParentBased(
                    new ParentBasedSamplerModel()
                        .setRoot(
                            new SamplerModel()
                                .setTraceIdRatioBased(
                                    new TraceIdRatioBasedSamplerModel().setRatio(0.1d)))
                        .setRemoteParentSampled(
                            new SamplerModel()
                                .setTraceIdRatioBased(
                                    new TraceIdRatioBasedSamplerModel().setRatio(0.2d)))
                        .setRemoteParentNotSampled(
                            new SamplerModel()
                                .setTraceIdRatioBased(
                                    new TraceIdRatioBasedSamplerModel().setRatio(0.3d)))
                        .setLocalParentSampled(
                            new SamplerModel()
                                .setTraceIdRatioBased(
                                    new TraceIdRatioBasedSamplerModel().setRatio(0.4d)))
                        .setLocalParentNotSampled(
                            new SamplerModel()
                                .setTraceIdRatioBased(
                                    new TraceIdRatioBasedSamplerModel().setRatio(0.5d)))),
            Sampler.parentBasedBuilder(Sampler.traceIdRatioBased(0.1d))
                .setRemoteParentSampled(Sampler.traceIdRatioBased(0.2d))
                .setRemoteParentNotSampled(Sampler.traceIdRatioBased(0.3d))
                .setLocalParentSampled(Sampler.traceIdRatioBased(0.4d))
                .setLocalParentNotSampled(Sampler.traceIdRatioBased(0.5d))
                .build()),
        Arguments.argumentSet(
            "jaeger_remote",
            SamplerModelAccessor.setJaegerRemote(
                new SamplerModel(),
                new ExperimentalJaegerRemoteSamplerModel()
                    .setEndpoint("http://jaeger-remote-endpoint")
                    .setInterval(10_000)
                    .setInitialSampler(
                        new SamplerModel().setAlwaysOff(new AlwaysOffSamplerModel()))),
            JaegerRemoteSampler.builder()
                .setEndpoint("http://jaeger-remote-endpoint")
                .setPollingInterval(Duration.ofSeconds(10))
                .setInitialSampler(Sampler.alwaysOff())
                .build()),
        Arguments.argumentSet(
            "composite/development always_on",
            SamplerModelAccessor.setComposite(
                new SamplerModel(),
                new ExperimentalComposableSamplerModel()
                    .setAlwaysOn(new ExperimentalComposableAlwaysOnSamplerModel())),
            CompositeSampler.wrap(ComposableSampler.alwaysOn())),
        Arguments.argumentSet(
            "composite/development always_off",
            SamplerModelAccessor.setComposite(
                new SamplerModel(),
                new ExperimentalComposableSamplerModel()
                    .setAlwaysOff(new ExperimentalComposableAlwaysOffSamplerModel())),
            CompositeSampler.wrap(ComposableSampler.alwaysOff())),
        Arguments.argumentSet(
            "composite/development probability",
            SamplerModelAccessor.setComposite(
                new SamplerModel(),
                new ExperimentalComposableSamplerModel()
                    .setProbability(
                        new ExperimentalComposableProbabilitySamplerModel().setRatio(0.5))),
            CompositeSampler.wrap(ComposableSampler.probability(0.5))),
        Arguments.argumentSet(
            "composite/development rule_based",
            SamplerModelAccessor.setComposite(
                new SamplerModel(),
                new ExperimentalComposableSamplerModel()
                    .setRuleBased(new ExperimentalComposableRuleBasedSamplerModel())),
            CompositeSampler.wrap(ComposableSampler.ruleBasedBuilder().build())),
        Arguments.argumentSet(
            "composite/development parent_threshold",
            SamplerModelAccessor.setComposite(
                new SamplerModel(),
                new ExperimentalComposableSamplerModel()
                    .setParentThreshold(
                        new ExperimentalComposableParentThresholdSamplerModel()
                            .setRoot(
                                new ExperimentalComposableSamplerModel()
                                    .setAlwaysOn(
                                        new ExperimentalComposableAlwaysOnSamplerModel())))),
            CompositeSampler.wrap(
                ComposableSampler.parentThreshold(ComposableSampler.alwaysOn()))));
  }

  @ParameterizedTest
  @MethodSource("createInvalidArguments")
  void createInvalid(SamplerModel model, String expectedMessage) {
    assertThatThrownBy(() -> SamplerFactory.getInstance().create(model, context))
        .isInstanceOf(DeclarativeConfigException.class)
        .extracting(throwable -> throwable.getCause() == null ? throwable : throwable.getCause())
        .extracting(Throwable::getMessage)
        .isEqualTo(expectedMessage);
  }

  private static Stream<Arguments> createInvalidArguments() {
    return Stream.of(
        Arguments.argumentSet(
            "jaeger_remote missing endpoint",
            SamplerModelAccessor.setJaegerRemote(
                new SamplerModel(), new ExperimentalJaegerRemoteSamplerModel()),
            "jaeger remote sampler endpoint is required"),
        Arguments.argumentSet(
            "jaeger_remote missing initialSampler",
            SamplerModelAccessor.setJaegerRemote(
                new SamplerModel(),
                new ExperimentalJaegerRemoteSamplerModel()
                    .setEndpoint("http://jaeger-remote-endpoint")),
            "jaeger remote sampler initial_sampler is required"),
        Arguments.argumentSet(
            "parent_threshold missing root",
            SamplerModelAccessor.setComposite(
                new SamplerModel(),
                new ExperimentalComposableSamplerModel()
                    .setParentThreshold(new ExperimentalComposableParentThresholdSamplerModel())),
            "parent threshold sampler root is required but is null"),
        Arguments.argumentSet(
            "unknown component provider",
            new SamplerModel()
                .setExtensionProperty("unknown_key", Collections.singletonMap("key1", "value1")),
            "No component provider detected for io.opentelemetry.sdk.trace.samplers.Sampler with name \"unknown_key\"."));
  }

  @Test
  void create_SpiExporter_Valid() {
    Sampler sampler =
        SamplerFactory.getInstance()
            .create(
                new SamplerModel()
                    .setExtensionProperty("test", Collections.singletonMap("key1", "value1")),
                context);
    assertThat(sampler).isInstanceOf(SamplerComponentProvider.TestSampler.class);
    assertThat(((SamplerComponentProvider.TestSampler) sampler).config.getString("key1"))
        .isEqualTo("value1");
  }
}
