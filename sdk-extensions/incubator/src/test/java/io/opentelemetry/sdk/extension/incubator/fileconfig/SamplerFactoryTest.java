/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.incubator.config.StructuredConfigException;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOff;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOn;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.JaegerRemote;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ParentBased;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Sampler;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceIdRatioBased;
import io.opentelemetry.sdk.extension.trace.jaeger.sampler.JaegerRemoteSampler;
import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
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
class SamplerFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final SpiHelper spiHelper = SpiHelper.create(SamplerFactoryTest.class.getClassLoader());

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(
      @Nullable Sampler model, io.opentelemetry.sdk.trace.samplers.Sampler expectedSampler) {
    // Some samplers like JaegerRemoteSampler are Closeable - ensure these get cleaned up
    if (expectedSampler instanceof Closeable) {
      cleanup.addCloseable((Closeable) expectedSampler);
    }

    List<Closeable> closeables = new ArrayList<>();
    io.opentelemetry.sdk.trace.samplers.Sampler sampler =
        SamplerFactory.getInstance().create(model, spiHelper, closeables);
    cleanup.addCloseables(closeables);

    assertThat(sampler.toString()).isEqualTo(expectedSampler.toString());
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.of(
            new Sampler().withAlwaysOn(new AlwaysOn()),
            io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOn()),
        Arguments.of(
            new Sampler().withAlwaysOff(new AlwaysOff()),
            io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOff()),
        Arguments.of(
            new Sampler().withTraceIdRatioBased(new TraceIdRatioBased()),
            io.opentelemetry.sdk.trace.samplers.Sampler.traceIdRatioBased(1.0d)),
        Arguments.of(
            new Sampler().withTraceIdRatioBased(new TraceIdRatioBased().withRatio(0.5d)),
            io.opentelemetry.sdk.trace.samplers.Sampler.traceIdRatioBased(0.5)),
        Arguments.of(
            new Sampler().withParentBased(new ParentBased()),
            io.opentelemetry.sdk.trace.samplers.Sampler.parentBased(
                io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOn())),
        Arguments.of(
            new Sampler()
                .withParentBased(
                    new ParentBased()
                        .withRoot(
                            new Sampler()
                                .withTraceIdRatioBased(new TraceIdRatioBased().withRatio(0.1d)))
                        .withRemoteParentSampled(
                            new Sampler()
                                .withTraceIdRatioBased(new TraceIdRatioBased().withRatio(0.2d)))
                        .withRemoteParentNotSampled(
                            new Sampler()
                                .withTraceIdRatioBased(new TraceIdRatioBased().withRatio(0.3d)))
                        .withLocalParentSampled(
                            new Sampler()
                                .withTraceIdRatioBased(new TraceIdRatioBased().withRatio(0.4d)))
                        .withLocalParentNotSampled(
                            new Sampler()
                                .withTraceIdRatioBased(new TraceIdRatioBased().withRatio(0.5d)))),
            io.opentelemetry.sdk.trace.samplers.Sampler.parentBasedBuilder(
                    io.opentelemetry.sdk.trace.samplers.Sampler.traceIdRatioBased(0.1d))
                .setRemoteParentSampled(
                    io.opentelemetry.sdk.trace.samplers.Sampler.traceIdRatioBased(0.2d))
                .setRemoteParentNotSampled(
                    io.opentelemetry.sdk.trace.samplers.Sampler.traceIdRatioBased(0.3d))
                .setLocalParentSampled(
                    io.opentelemetry.sdk.trace.samplers.Sampler.traceIdRatioBased(0.4d))
                .setLocalParentNotSampled(
                    io.opentelemetry.sdk.trace.samplers.Sampler.traceIdRatioBased(0.5d))
                .build()),
        Arguments.of(
            new Sampler()
                .withJaegerRemote(
                    new JaegerRemote()
                        .withEndpoint("http://jaeger-remote-endpoint")
                        .withInterval(10_000)
                        .withInitialSampler(new Sampler().withAlwaysOff(new AlwaysOff()))),
            JaegerRemoteSampler.builder()
                .setEndpoint("http://jaeger-remote-endpoint")
                .setPollingInterval(Duration.ofSeconds(10))
                .build()));
  }

  @Test
  void create_SpiExporter() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                SamplerFactory.getInstance()
                    .create(
                        new Sampler()
                            .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                        spiHelper,
                        new ArrayList<>()))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("Unrecognized sampler(s): [test]");
    cleanup.addCloseables(closeables);
  }
}
