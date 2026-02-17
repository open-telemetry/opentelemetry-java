/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOffSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOnSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.B3MultiPropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.B3PropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BaggagePropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.CardinalityLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ConsoleExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ConsoleMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.DistributionModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.DistributionPropertyModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableAlwaysOffSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableAlwaysOnSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableProbabilitySamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableRuleBasedSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableRuleBasedSamplerRuleModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalContainerResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalGeneralInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalHostResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalHttpClientInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalHttpInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalHttpServerInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLanguageSpecificInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLanguageSpecificInstrumentationPropertyModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerConfiguratorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLoggerMatcherAndConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalMeterConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalMeterConfiguratorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalMeterMatcherAndConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalOtlpFileExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalOtlpFileMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalPeerInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalPeerServiceMappingModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalProbabilitySamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalProcessResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalPrometheusMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectionModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalServiceResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalSpanParent;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalTracerConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalTracerConfiguratorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalTracerMatcherAndConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogramAggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.GrpcTlsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.HttpTlsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.IncludeExcludeModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.JaegerPropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricProducerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.NameStringValuePairModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenCensusMetricProducerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTracingPropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpGrpcExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpGrpcMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ParentBasedSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanKind;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TextMapPropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceContextPropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceIdRatioBasedSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewSelectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewStreamModel;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DeclarativeConfigurationParseTest {

  @Test
  void parse_BadInputStream() {
    assertThatThrownBy(
            () ->
                DeclarativeConfiguration.parseAndCreate(
                    new ByteArrayInputStream("foo".getBytes(StandardCharsets.UTF_8))))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("Unable to parse configuration input stream");
  }

  @Test
  void parse_KitchenSinkExampleFile() throws IOException {
    OpenTelemetryConfigurationModel expected = new OpenTelemetryConfigurationModel();

    expected.withFileFormat("1.0-rc.3");
    expected.withDisabled(false);
    expected.withLogLevel(OpenTelemetryConfigurationModel.SeverityNumber.INFO);

    // General config
    ResourceModel resource =
        new ResourceModel()
            .withAttributes(
                Arrays.asList(
                    new AttributeNameValueModel()
                        .withName("service.name")
                        .withValue("unknown_service"),
                    new AttributeNameValueModel()
                        .withName("string_key")
                        .withValue("value")
                        .withType(AttributeNameValueModel.AttributeType.STRING),
                    new AttributeNameValueModel()
                        .withName("bool_key")
                        .withValue(true)
                        .withType(AttributeNameValueModel.AttributeType.BOOL),
                    new AttributeNameValueModel()
                        .withName("int_key")
                        .withValue(1)
                        .withType(AttributeNameValueModel.AttributeType.INT),
                    new AttributeNameValueModel()
                        .withName("double_key")
                        .withValue(1.1)
                        .withType(AttributeNameValueModel.AttributeType.DOUBLE),
                    new AttributeNameValueModel()
                        .withName("string_array_key")
                        .withValue(Arrays.asList("value1", "value2"))
                        .withType(AttributeNameValueModel.AttributeType.STRING_ARRAY),
                    new AttributeNameValueModel()
                        .withName("bool_array_key")
                        .withValue(Arrays.asList(true, false))
                        .withType(AttributeNameValueModel.AttributeType.BOOL_ARRAY),
                    new AttributeNameValueModel()
                        .withName("int_array_key")
                        .withValue(Arrays.asList(1, 2))
                        .withType(AttributeNameValueModel.AttributeType.INT_ARRAY),
                    new AttributeNameValueModel()
                        .withName("double_array_key")
                        .withValue(Arrays.asList(1.1, 2.2))
                        .withType(AttributeNameValueModel.AttributeType.DOUBLE_ARRAY)))
            .withAttributesList("service.namespace=my-namespace,service.version=1.0.0")
            .withDetectionDevelopment(
                new ExperimentalResourceDetectionModel()
                    .withAttributes(
                        new IncludeExcludeModel()
                            .withIncluded(Collections.singletonList("process.*"))
                            .withExcluded(Collections.singletonList("process.command_args")))
                    .withDetectors(
                        Arrays.asList(
                            new ExperimentalResourceDetectorModel()
                                .withContainer(new ExperimentalContainerResourceDetectorModel()),
                            new ExperimentalResourceDetectorModel()
                                .withHost(new ExperimentalHostResourceDetectorModel()),
                            new ExperimentalResourceDetectorModel()
                                .withProcess(new ExperimentalProcessResourceDetectorModel()),
                            new ExperimentalResourceDetectorModel()
                                .withService(new ExperimentalServiceResourceDetectorModel()))))
            .withSchemaUrl("https://opentelemetry.io/schemas/1.16.0");
    expected.withResource(resource);

    AttributeLimitsModel attributeLimits =
        new AttributeLimitsModel().withAttributeValueLengthLimit(4096).withAttributeCountLimit(128);
    expected.withAttributeLimits(attributeLimits);

    PropagatorModel propagator =
        new PropagatorModel()
            .withComposite(
                Arrays.asList(
                    new TextMapPropagatorModel()
                        .withTracecontext(new TraceContextPropagatorModel()),
                    new TextMapPropagatorModel().withBaggage(new BaggagePropagatorModel()),
                    new TextMapPropagatorModel().withB3(new B3PropagatorModel()),
                    new TextMapPropagatorModel().withB3multi(new B3MultiPropagatorModel()),
                    new TextMapPropagatorModel().withJaeger(new JaegerPropagatorModel()),
                    new TextMapPropagatorModel().withOttrace(new OpenTracingPropagatorModel())))
            .withCompositeList("tracecontext,baggage,b3,b3multi,jaeger,ottrace,xray");
    expected.withPropagator(propagator);

    // TracerProvider config
    TracerProviderModel tracerProvider = new TracerProviderModel();

    SpanLimitsModel spanLimits =
        new SpanLimitsModel()
            .withAttributeValueLengthLimit(4096)
            .withAttributeCountLimit(128)
            .withEventCountLimit(128)
            .withLinkCountLimit(128)
            .withEventAttributeCountLimit(128)
            .withLinkAttributeCountLimit(128);
    tracerProvider.withLimits(spanLimits);

    SamplerModel sampler =
        new SamplerModel()
            .withParentBased(
                new ParentBasedSamplerModel()
                    .withRoot(
                        new SamplerModel()
                            .withTraceIdRatioBased(
                                new TraceIdRatioBasedSamplerModel().withRatio(0.0001)))
                    .withRemoteParentSampled(
                        new SamplerModel().withAlwaysOn(new AlwaysOnSamplerModel()))
                    .withRemoteParentNotSampled(
                        new SamplerModel()
                            .withProbabilityDevelopment(
                                new ExperimentalProbabilitySamplerModel().withRatio(0.01)))
                    .withLocalParentSampled(
                        new SamplerModel()
                            .withCompositeDevelopment(
                                new ExperimentalComposableSamplerModel()
                                    .withRuleBased(
                                        new ExperimentalComposableRuleBasedSamplerModel()
                                            .withRules(
                                                Arrays.asList(
                                                    new ExperimentalComposableRuleBasedSamplerRuleModel()
                                                        .withAttributeValues(
                                                            new ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel()
                                                                .withKey("http.route")
                                                                .withValues(
                                                                    Arrays.asList(
                                                                        "/healthz", "/livez")))
                                                        .withSampler(
                                                            new ExperimentalComposableSamplerModel()
                                                                .withAlwaysOff(
                                                                    new ExperimentalComposableAlwaysOffSamplerModel())),
                                                    new ExperimentalComposableRuleBasedSamplerRuleModel()
                                                        .withAttributePatterns(
                                                            new ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel()
                                                                .withKey("http.path")
                                                                .withIncluded(
                                                                    Collections.singletonList(
                                                                        "/internal/*"))
                                                                .withExcluded(
                                                                    Collections.singletonList(
                                                                        "/internal/special/*")))
                                                        .withSampler(
                                                            new ExperimentalComposableSamplerModel()
                                                                .withAlwaysOn(
                                                                    new ExperimentalComposableAlwaysOnSamplerModel())),
                                                    new ExperimentalComposableRuleBasedSamplerRuleModel()
                                                        .withParent(
                                                            Collections.singletonList(
                                                                ExperimentalSpanParent.NONE))
                                                        .withSpanKinds(
                                                            Collections.singletonList(
                                                                SpanKind.CLIENT))
                                                        .withSampler(
                                                            new ExperimentalComposableSamplerModel()
                                                                .withProbability(
                                                                    new ExperimentalComposableProbabilitySamplerModel()
                                                                        .withRatio(0.05))),
                                                    new ExperimentalComposableRuleBasedSamplerRuleModel()
                                                        .withSampler(
                                                            new ExperimentalComposableSamplerModel()
                                                                .withProbability(
                                                                    new ExperimentalComposableProbabilitySamplerModel()
                                                                        .withRatio(0.001))))))))
                    .withLocalParentNotSampled(
                        new SamplerModel().withAlwaysOff(new AlwaysOffSamplerModel())));
    tracerProvider.withSampler(sampler);

    ExperimentalTracerConfiguratorModel tracerConfigurator =
        new ExperimentalTracerConfiguratorModel()
            .withDefaultConfig(new ExperimentalTracerConfigModel().withDisabled(true))
            .withTracers(
                Collections.singletonList(
                    new ExperimentalTracerMatcherAndConfigModel()
                        .withName("io.opentelemetry.contrib.*")
                        .withConfig(new ExperimentalTracerConfigModel().withDisabled(false))));
    tracerProvider.withTracerConfiguratorDevelopment(tracerConfigurator);

    SpanProcessorModel spanProcessor1 =
        new SpanProcessorModel()
            .withBatch(
                new BatchSpanProcessorModel()
                    .withScheduleDelay(5_000)
                    .withExportTimeout(30_000)
                    .withMaxQueueSize(2048)
                    .withMaxExportBatchSize(512)
                    .withExporter(
                        new SpanExporterModel()
                            .withOtlpHttp(
                                new OtlpHttpExporterModel()
                                    .withEndpoint("http://localhost:4318/v1/traces")
                                    .withTls(
                                        new HttpTlsModel()
                                            .withCaFile("/app/cert.pem")
                                            .withKeyFile("/app/cert.pem")
                                            .withCertFile("/app/cert.pem"))
                                    .withHeaders(
                                        Collections.singletonList(
                                            new NameStringValuePairModel()
                                                .withName("api-key")
                                                .withValue("1234")))
                                    .withHeadersList("api-key=1234")
                                    .withCompression("gzip")
                                    .withTimeout(10_000)
                                    .withEncoding(
                                        OtlpHttpExporterModel.OtlpHttpEncoding.PROTOBUF))));
    SpanProcessorModel spanProcessor2 =
        new SpanProcessorModel()
            .withBatch(
                new BatchSpanProcessorModel()
                    .withExporter(
                        new SpanExporterModel()
                            .withOtlpGrpc(
                                new OtlpGrpcExporterModel()
                                    .withEndpoint("http://localhost:4317")
                                    .withTls(
                                        new GrpcTlsModel()
                                            .withCaFile("/app/cert.pem")
                                            .withKeyFile("/app/cert.pem")
                                            .withCertFile("/app/cert.pem")
                                            .withInsecure(false))
                                    .withHeaders(
                                        Collections.singletonList(
                                            new NameStringValuePairModel()
                                                .withName("api-key")
                                                .withValue("1234")))
                                    .withHeadersList("api-key=1234")
                                    .withCompression("gzip")
                                    .withTimeout(10_000))));
    SpanProcessorModel spanProcessor3 =
        new SpanProcessorModel()
            .withBatch(
                new BatchSpanProcessorModel()
                    .withExporter(
                        new SpanExporterModel()
                            .withOtlpFileDevelopment(
                                new ExperimentalOtlpFileExporterModel()
                                    .withOutputStream("file:///var/log/traces.jsonl"))));
    SpanProcessorModel spanProcessor4 =
        new SpanProcessorModel()
            .withBatch(
                new BatchSpanProcessorModel()
                    .withExporter(
                        new SpanExporterModel()
                            .withOtlpFileDevelopment(
                                new ExperimentalOtlpFileExporterModel()
                                    .withOutputStream("stdout"))));
    SpanProcessorModel spanProcessor5 =
        new SpanProcessorModel()
            .withSimple(
                new SimpleSpanProcessorModel()
                    .withExporter(new SpanExporterModel().withConsole(new ConsoleExporterModel())));
    tracerProvider.withProcessors(
        Arrays.asList(
            spanProcessor1, spanProcessor2, spanProcessor3, spanProcessor4, spanProcessor5));

    expected.withTracerProvider(tracerProvider);
    // end TracerProvider config

    // LoggerProvider config
    LoggerProviderModel loggerProvider = new LoggerProviderModel();

    LogRecordLimitsModel logRecordLimits =
        new LogRecordLimitsModel().withAttributeValueLengthLimit(4096).withAttributeCountLimit(128);
    loggerProvider.withLimits(logRecordLimits);

    ExperimentalLoggerConfiguratorModel loggerConfigurator =
        new ExperimentalLoggerConfiguratorModel()
            .withDefaultConfig(new ExperimentalLoggerConfigModel().withDisabled(true))
            .withLoggers(
                Collections.singletonList(
                    new ExperimentalLoggerMatcherAndConfigModel()
                        .withName("io.opentelemetry.contrib.*")
                        .withConfig(
                            new ExperimentalLoggerConfigModel()
                                .withDisabled(false)
                                .withMinimumSeverity(
                                    // TODO: SeverityNumber should not be nested
                                    OpenTelemetryConfigurationModel.SeverityNumber.INFO)
                                .withTraceBased(true))));
    loggerProvider.withLoggerConfiguratorDevelopment(loggerConfigurator);

    LogRecordProcessorModel logRecordProcessor1 =
        new LogRecordProcessorModel()
            .withBatch(
                new BatchLogRecordProcessorModel()
                    .withScheduleDelay(5_000)
                    .withExportTimeout(30_000)
                    .withMaxQueueSize(2048)
                    .withMaxExportBatchSize(512)
                    .withExporter(
                        new LogRecordExporterModel()
                            .withOtlpHttp(
                                new OtlpHttpExporterModel()
                                    .withEndpoint("http://localhost:4318/v1/logs")
                                    .withTls(
                                        new HttpTlsModel()
                                            .withCaFile("/app/cert.pem")
                                            .withKeyFile("/app/cert.pem")
                                            .withCertFile("/app/cert.pem"))
                                    .withHeaders(
                                        Collections.singletonList(
                                            new NameStringValuePairModel()
                                                .withName("api-key")
                                                .withValue("1234")))
                                    .withHeadersList("api-key=1234")
                                    .withCompression("gzip")
                                    .withTimeout(10_000)
                                    .withEncoding(
                                        OtlpHttpExporterModel.OtlpHttpEncoding.PROTOBUF))));
    LogRecordProcessorModel logRecordProcessor2 =
        new LogRecordProcessorModel()
            .withBatch(
                new BatchLogRecordProcessorModel()
                    .withExporter(
                        new LogRecordExporterModel()
                            .withOtlpGrpc(
                                new OtlpGrpcExporterModel()
                                    .withEndpoint("http://localhost:4317")
                                    .withTls(
                                        new GrpcTlsModel()
                                            .withCaFile("/app/cert.pem")
                                            .withKeyFile("/app/cert.pem")
                                            .withCertFile("/app/cert.pem")
                                            .withInsecure(false))
                                    .withHeaders(
                                        Collections.singletonList(
                                            new NameStringValuePairModel()
                                                .withName("api-key")
                                                .withValue("1234")))
                                    .withHeadersList("api-key=1234")
                                    .withCompression("gzip")
                                    .withTimeout(10_000))));
    LogRecordProcessorModel logRecordProcessor3 =
        new LogRecordProcessorModel()
            .withBatch(
                new BatchLogRecordProcessorModel()
                    .withExporter(
                        new LogRecordExporterModel()
                            .withOtlpFileDevelopment(
                                new ExperimentalOtlpFileExporterModel()
                                    .withOutputStream("file:///var/log/logs.jsonl"))));
    LogRecordProcessorModel logRecordProcessor4 =
        new LogRecordProcessorModel()
            .withBatch(
                new BatchLogRecordProcessorModel()
                    .withExporter(
                        new LogRecordExporterModel()
                            .withOtlpFileDevelopment(
                                new ExperimentalOtlpFileExporterModel()
                                    .withOutputStream("stdout"))));
    LogRecordProcessorModel logRecordProcessor5 =
        new LogRecordProcessorModel()
            .withSimple(
                new SimpleLogRecordProcessorModel()
                    .withExporter(
                        new LogRecordExporterModel().withConsole(new ConsoleExporterModel())));
    loggerProvider.withProcessors(
        Arrays.asList(
            logRecordProcessor1,
            logRecordProcessor2,
            logRecordProcessor3,
            logRecordProcessor4,
            logRecordProcessor5));

    expected.withLoggerProvider(loggerProvider);
    // end LoggerProvider config

    // MeterProvider config
    MeterProviderModel meterProvider = new MeterProviderModel();

    MetricReaderModel metricReader1 =
        new MetricReaderModel()
            .withPull(
                new PullMetricReaderModel()
                    .withExporter(
                        new PullMetricExporterModel()
                            .withPrometheusDevelopment(
                                new ExperimentalPrometheusMetricExporterModel()
                                    .withHost("localhost")
                                    .withPort(9464)
                                    .withWithoutScopeInfo(false)
                                    .withWithoutTargetInfo(false)
                                    .withWithResourceConstantLabels(
                                        new IncludeExcludeModel()
                                            .withIncluded(Collections.singletonList("service*"))
                                            .withExcluded(
                                                Collections.singletonList("service.attr1")))
                                    .withTranslationStrategy(
                                        ExperimentalPrometheusMetricExporterModel
                                            .ExperimentalPrometheusTranslationStrategy
                                            .UNDERSCORE_ESCAPING_WITH_SUFFIXES)))
                    .withProducers(
                        Collections.singletonList(
                            new MetricProducerModel()
                                .withOpencensus(new OpenCensusMetricProducerModel())))
                    .withCardinalityLimits(
                        new CardinalityLimitsModel()
                            .withDefault(2000)
                            .withCounter(2000)
                            .withGauge(2000)
                            .withHistogram(2000)
                            .withObservableCounter(2000)
                            .withObservableGauge(2000)
                            .withObservableUpDownCounter(2000)
                            .withUpDownCounter(2000)));
    MetricReaderModel metricReader2 =
        new MetricReaderModel()
            .withPeriodic(
                new PeriodicMetricReaderModel()
                    .withInterval(60_000)
                    .withTimeout(30_000)
                    .withExporter(
                        new PushMetricExporterModel()
                            .withOtlpHttp(
                                new OtlpHttpMetricExporterModel()
                                    .withEndpoint("http://localhost:4318/v1/metrics")
                                    .withTls(
                                        new HttpTlsModel()
                                            .withCaFile("/app/cert.pem")
                                            .withKeyFile("/app/cert.pem")
                                            .withCertFile("/app/cert.pem"))
                                    .withHeaders(
                                        Collections.singletonList(
                                            new NameStringValuePairModel()
                                                .withName("api-key")
                                                .withValue("1234")))
                                    .withHeadersList("api-key=1234")
                                    .withCompression("gzip")
                                    .withTimeout(10_000)
                                    .withEncoding(OtlpHttpExporterModel.OtlpHttpEncoding.PROTOBUF)
                                    .withTemporalityPreference(
                                        OtlpHttpMetricExporterModel.ExporterTemporalityPreference
                                            .DELTA)
                                    .withDefaultHistogramAggregation(
                                        OtlpHttpMetricExporterModel
                                            .ExporterDefaultHistogramAggregation
                                            .BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM)))
                    .withProducers(
                        Collections.singletonList(
                            new MetricProducerModel()
                                .withOpencensus(new OpenCensusMetricProducerModel())))
                    .withCardinalityLimits(
                        new CardinalityLimitsModel()
                            .withDefault(2000)
                            .withCounter(2000)
                            .withGauge(2000)
                            .withHistogram(2000)
                            .withObservableCounter(2000)
                            .withObservableGauge(2000)
                            .withObservableUpDownCounter(2000)
                            .withUpDownCounter(2000)));
    MetricReaderModel metricReader3 =
        new MetricReaderModel()
            .withPeriodic(
                new PeriodicMetricReaderModel()
                    .withExporter(
                        new PushMetricExporterModel()
                            .withOtlpGrpc(
                                new OtlpGrpcMetricExporterModel()
                                    .withEndpoint("http://localhost:4317")
                                    .withTls(
                                        new GrpcTlsModel()
                                            .withCaFile("/app/cert.pem")
                                            .withKeyFile("/app/cert.pem")
                                            .withCertFile("/app/cert.pem")
                                            .withInsecure(false))
                                    .withHeaders(
                                        Collections.singletonList(
                                            new NameStringValuePairModel()
                                                .withName("api-key")
                                                .withValue("1234")))
                                    .withHeadersList("api-key=1234")
                                    .withCompression("gzip")
                                    .withTimeout(10_000)
                                    .withTemporalityPreference(
                                        OtlpHttpMetricExporterModel.ExporterTemporalityPreference
                                            .DELTA)
                                    .withDefaultHistogramAggregation(
                                        OtlpHttpMetricExporterModel
                                            .ExporterDefaultHistogramAggregation
                                            .BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM))));
    MetricReaderModel metricReader4 =
        new MetricReaderModel()
            .withPeriodic(
                new PeriodicMetricReaderModel()
                    .withExporter(
                        new PushMetricExporterModel()
                            .withOtlpFileDevelopment(
                                new ExperimentalOtlpFileMetricExporterModel()
                                    .withOutputStream("file:///var/log/metrics.jsonl")
                                    .withTemporalityPreference(
                                        OtlpHttpMetricExporterModel.ExporterTemporalityPreference
                                            .DELTA)
                                    .withDefaultHistogramAggregation(
                                        OtlpHttpMetricExporterModel
                                            .ExporterDefaultHistogramAggregation
                                            .BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM))));
    MetricReaderModel metricReader5 =
        new MetricReaderModel()
            .withPeriodic(
                new PeriodicMetricReaderModel()
                    .withExporter(
                        new PushMetricExporterModel()
                            .withOtlpFileDevelopment(
                                new ExperimentalOtlpFileMetricExporterModel()
                                    .withOutputStream("stdout")
                                    .withTemporalityPreference(
                                        OtlpHttpMetricExporterModel.ExporterTemporalityPreference
                                            .DELTA)
                                    .withDefaultHistogramAggregation(
                                        OtlpHttpMetricExporterModel
                                            .ExporterDefaultHistogramAggregation
                                            .BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM))));
    MetricReaderModel metricReader6 =
        new MetricReaderModel()
            .withPeriodic(
                new PeriodicMetricReaderModel()
                    .withExporter(
                        new PushMetricExporterModel()
                            .withConsole(
                                new ConsoleMetricExporterModel()
                                    .withTemporalityPreference(
                                        OtlpHttpMetricExporterModel.ExporterTemporalityPreference
                                            .DELTA)
                                    .withDefaultHistogramAggregation(
                                        OtlpHttpMetricExporterModel
                                            .ExporterDefaultHistogramAggregation
                                            .BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM))));
    meterProvider.withReaders(
        Arrays.asList(
            metricReader1,
            metricReader2,
            metricReader3,
            metricReader4,
            metricReader5,
            metricReader6));

    ViewModel view =
        new ViewModel()
            .withSelector(
                new ViewSelectorModel()
                    .withInstrumentName("my-instrument")
                    .withInstrumentType(ViewSelectorModel.InstrumentType.HISTOGRAM)
                    .withUnit("ms")
                    .withMeterName("my-meter")
                    .withMeterVersion("1.0.0")
                    .withMeterSchemaUrl("https://opentelemetry.io/schemas/1.16.0"))
            .withStream(
                new ViewStreamModel()
                    .withName("new_instrument_name")
                    .withDescription("new_description")
                    .withAggregation(
                        new AggregationModel()
                            .withExplicitBucketHistogram(
                                new ExplicitBucketHistogramAggregationModel()
                                    .withBoundaries(
                                        Arrays.asList(
                                            0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0,
                                            750.0, 1000.0, 2500.0, 5000.0, 7500.0, 10000.0))
                                    .withRecordMinMax(true)))
                    .withAggregationCardinalityLimit(2000)
                    .withAttributeKeys(
                        new IncludeExcludeModel()
                            .withIncluded(Arrays.asList("key1", "key2"))
                            .withExcluded(Collections.singletonList("key3"))));
    meterProvider.withViews(Collections.singletonList(view));
    meterProvider.withExemplarFilter(MeterProviderModel.ExemplarFilter.TRACE_BASED);

    ExperimentalMeterConfiguratorModel meterConfigurator =
        new ExperimentalMeterConfiguratorModel()
            .withDefaultConfig(new ExperimentalMeterConfigModel().withDisabled(true))
            .withMeters(
                Collections.singletonList(
                    new ExperimentalMeterMatcherAndConfigModel()
                        .withName("io.opentelemetry.contrib.*")
                        .withConfig(new ExperimentalMeterConfigModel().withDisabled(false))));
    meterProvider.withMeterConfiguratorDevelopment(meterConfigurator);

    expected.withMeterProvider(meterProvider);
    // end MeterProvider config

    // start instrumentation config
    ExperimentalInstrumentationModel instrumentation =
        new ExperimentalInstrumentationModel()
            .withGeneral(
                new ExperimentalGeneralInstrumentationModel()
                    .withPeer(
                        new ExperimentalPeerInstrumentationModel()
                            .withServiceMapping(
                                Arrays.asList(
                                    new ExperimentalPeerServiceMappingModel()
                                        .withPeer("1.2.3.4")
                                        .withService("FooService"),
                                    new ExperimentalPeerServiceMappingModel()
                                        .withPeer("2.3.4.5")
                                        .withService("BarService"))))
                    .withHttp(
                        new ExperimentalHttpInstrumentationModel()
                            .withClient(
                                new ExperimentalHttpClientInstrumentationModel()
                                    .withRequestCapturedHeaders(
                                        Arrays.asList("Content-Type", "Accept"))
                                    .withResponseCapturedHeaders(
                                        Arrays.asList("Content-Type", "Content-Encoding")))
                            .withServer(
                                new ExperimentalHttpServerInstrumentationModel()
                                    .withRequestCapturedHeaders(
                                        Arrays.asList("Content-Type", "Accept"))
                                    .withResponseCapturedHeaders(
                                        Arrays.asList("Content-Type", "Content-Encoding")))))
            .withCpp(
                new ExperimentalLanguageSpecificInstrumentationModel()
                    .withAdditionalProperty(
                        "example",
                        new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                            .withAdditionalProperty("property", "value")))
            .withDotnet(
                new ExperimentalLanguageSpecificInstrumentationModel()
                    .withAdditionalProperty(
                        "example",
                        new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                            .withAdditionalProperty("property", "value")))
            .withErlang(
                new ExperimentalLanguageSpecificInstrumentationModel()
                    .withAdditionalProperty(
                        "example",
                        new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                            .withAdditionalProperty("property", "value")))
            .withGo(
                new ExperimentalLanguageSpecificInstrumentationModel()
                    .withAdditionalProperty(
                        "example",
                        new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                            .withAdditionalProperty("property", "value")))
            .withJava(
                new ExperimentalLanguageSpecificInstrumentationModel()
                    .withAdditionalProperty(
                        "example",
                        new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                            .withAdditionalProperty("property", "value")))
            .withJs(
                new ExperimentalLanguageSpecificInstrumentationModel()
                    .withAdditionalProperty(
                        "example",
                        new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                            .withAdditionalProperty("property", "value")))
            .withPhp(
                new ExperimentalLanguageSpecificInstrumentationModel()
                    .withAdditionalProperty(
                        "example",
                        new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                            .withAdditionalProperty("property", "value")))
            .withPython(
                new ExperimentalLanguageSpecificInstrumentationModel()
                    .withAdditionalProperty(
                        "example",
                        new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                            .withAdditionalProperty("property", "value")))
            .withRuby(
                new ExperimentalLanguageSpecificInstrumentationModel()
                    .withAdditionalProperty(
                        "example",
                        new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                            .withAdditionalProperty("property", "value")))
            .withRust(
                new ExperimentalLanguageSpecificInstrumentationModel()
                    .withAdditionalProperty(
                        "example",
                        new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                            .withAdditionalProperty("property", "value")))
            .withSwift(
                new ExperimentalLanguageSpecificInstrumentationModel()
                    .withAdditionalProperty(
                        "example",
                        new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                            .withAdditionalProperty("property", "value")));
    expected.withInstrumentationDevelopment(instrumentation);
    // end instrumentation config

    DistributionModel distribution =
        new DistributionModel()
            .withAdditionalProperty(
                "example",
                new DistributionPropertyModel().withAdditionalProperty("property", "value"));
    expected.withDistribution(distribution);

    try (FileInputStream configExampleFile =
        new FileInputStream(System.getenv("CONFIG_EXAMPLE_DIR") + "/kitchen-sink.yaml")) {
      OpenTelemetryConfigurationModel config = DeclarativeConfiguration.parse(configExampleFile);

      // General config
      assertThat(config.getFileFormat()).isEqualTo("1.0-rc.3");
      assertThat(config.getResource()).isEqualTo(resource);
      assertThat(config.getAttributeLimits()).isEqualTo(attributeLimits);
      assertThat(config.getPropagator()).isEqualTo(propagator);

      // TracerProvider config
      TracerProviderModel configTracerProvider = config.getTracerProvider();
      assertThat(configTracerProvider.getLimits()).isEqualTo(spanLimits);
      assertThat(configTracerProvider.getSampler()).isEqualTo(sampler);
      assertThat(configTracerProvider.getTracerConfiguratorDevelopment())
          .isEqualTo(tracerConfigurator);
      assertThat(configTracerProvider.getProcessors())
          .isEqualTo(
              Arrays.asList(
                  spanProcessor1, spanProcessor2, spanProcessor3, spanProcessor4, spanProcessor5));
      assertThat(configTracerProvider).isEqualTo(tracerProvider);

      // LoggerProvider config
      LoggerProviderModel configLoggerProvider = config.getLoggerProvider();
      assertThat(configLoggerProvider.getLimits()).isEqualTo(logRecordLimits);
      assertThat(configLoggerProvider.getLoggerConfiguratorDevelopment())
          .isEqualTo(loggerConfigurator);
      assertThat(configLoggerProvider.getProcessors())
          .isEqualTo(
              Arrays.asList(
                  logRecordProcessor1,
                  logRecordProcessor2,
                  logRecordProcessor3,
                  logRecordProcessor4,
                  logRecordProcessor5));
      assertThat(configLoggerProvider).isEqualTo(loggerProvider);

      // MeterProvider config
      MeterProviderModel configMeterProvider = config.getMeterProvider();
      assertThat(configMeterProvider.getReaders())
          .isEqualTo(
              Arrays.asList(
                  metricReader1,
                  metricReader2,
                  metricReader3,
                  metricReader4,
                  metricReader5,
                  metricReader6));
      assertThat(configMeterProvider.getViews()).isEqualTo(Collections.singletonList(view));
      assertThat(configMeterProvider.getMeterConfiguratorDevelopment())
          .isEqualTo(meterConfigurator);
      assertThat(configMeterProvider).isEqualTo(meterProvider);

      // Instrumentation config
      ExperimentalInstrumentationModel configInstrumentation =
          config.getInstrumentationDevelopment();
      assertThat(configInstrumentation).isEqualTo(instrumentation);

      // Distribution config
      DistributionModel configDistribution = config.getDistribution();
      assertThat(configDistribution).isEqualTo(distribution);

      // All configuration
      assertThat(config).isEqualTo(expected);
    }
  }

  @Test
  void parse_nullValuesParsedToEmptyObjects() {
    String objectPlaceholderString =
        "file_format: \"1.0-rc.1\"\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          console: {}\n"
            + "meter_provider:\n"
            + "  views:\n"
            + "    - selector:\n"
            + "        instrument_type: histogram\n"
            + "      stream:\n"
            + "        aggregation:\n"
            + "          drop: {}\n";
    OpenTelemetryConfigurationModel objectPlaceholderModel =
        DeclarativeConfiguration.parse(
            new ByteArrayInputStream(objectPlaceholderString.getBytes(StandardCharsets.UTF_8)));

    String noOjbectPlaceholderString =
        "file_format: \"1.0-rc.1\"\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          console:\n"
            + "meter_provider:\n"
            + "  views:\n"
            + "    - selector:\n"
            + "        instrument_type: histogram\n"
            + "      stream:\n"
            + "        aggregation:\n"
            + "          drop:\n";
    OpenTelemetryConfigurationModel noObjectPlaceholderModel =
        DeclarativeConfiguration.parse(
            new ByteArrayInputStream(noOjbectPlaceholderString.getBytes(StandardCharsets.UTF_8)));

    SpanExporterModel exporter =
        noObjectPlaceholderModel
            .getTracerProvider()
            .getProcessors()
            .get(0)
            .getBatch()
            .getExporter();
    assertThat(exporter.getConsole()).isNotNull();
    assertThat(exporter.getOtlpHttp()).isNull();

    AggregationModel aggregation =
        noObjectPlaceholderModel.getMeterProvider().getViews().get(0).getStream().getAggregation();
    assertThat(aggregation.getDrop()).isNotNull();
    assertThat(aggregation.getSum()).isNull();

    assertThat(objectPlaceholderModel).isEqualTo(noObjectPlaceholderModel);
  }

  @Test
  void parse_nullBoxedPrimitivesParsedToNull() {
    String yaml =
        "file_format:\n" // String
            + "disabled:\n" // Boolean
            + "attribute_limits:\n"
            + "  attribute_value_length_limit:\n" // Integer
            + "tracer_provider:\n"
            + "  sampler:\n"
            + "    trace_id_ratio_based:\n"
            + "      ratio:\n"; // Double

    OpenTelemetryConfigurationModel model =
        DeclarativeConfiguration.parse(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

    assertThat(model.getFileFormat()).isNull();
    assertThat(model.getDisabled()).isNull();
    assertThat(model.getAttributeLimits().getAttributeValueLengthLimit()).isNull();
    assertThat(model.getTracerProvider().getSampler().getTraceIdRatioBased().getRatio()).isNull();

    assertThat(model)
        .isEqualTo(
            new OpenTelemetryConfigurationModel()
                .withAttributeLimits(new AttributeLimitsModel())
                .withTracerProvider(
                    new TracerProviderModel()
                        .withSampler(
                            new SamplerModel()
                                .withTraceIdRatioBased(new TraceIdRatioBasedSamplerModel()))));
  }

  @Test
  void parse_quotedInput() {
    String yaml =
        "resource:\n"
            + "  attributes:\n"
            + "    - name: single_quote\n"
            + "      value: '\"single\"'\n"
            + "    - name: double_quote\n"
            + "      value: \"\\\"double\\\"\"";

    OpenTelemetryConfigurationModel model =
        DeclarativeConfiguration.parse(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

    Assertions.assertNotNull(model.getResource());
    assertThat(model.getResource().getAttributes())
        .containsExactly(
            new AttributeNameValueModel().withName("single_quote").withValue("\"single\""),
            new AttributeNameValueModel().withName("double_quote").withValue("\"double\""));
  }

  @ParameterizedTest
  @MethodSource("coreSchemaValuesArgs")
  void coreSchemaValues(String rawYaml, Object expectedYamlResult) {
    Object yaml =
        DeclarativeConfiguration.loadYaml(
            new ByteArrayInputStream(rawYaml.getBytes(StandardCharsets.UTF_8)),
            Collections.emptyMap(),
            Collections.emptyMap());
    assertThat(yaml).isEqualTo(expectedYamlResult);
  }

  @SuppressWarnings("unchecked")
  private static Stream<Arguments> coreSchemaValuesArgs() {
    return Stream.of(
        Arguments.of("key1: 0o123\n", mapOf(entry("key1", 83))),
        Arguments.of("key1: 0123\n", mapOf(entry("key1", 123))),
        Arguments.of("key1: 0xdeadbeef\n", mapOf(entry("key1", 3735928559L))),
        Arguments.of("key1: \"0xdeadbeef\"\n", mapOf(entry("key1", "0xdeadbeef"))));
  }

  @ParameterizedTest
  @MethodSource("envVarSubstitutionArgs")
  void envSubstituteAndLoadYaml(String rawYaml, Object expectedYamlResult) {
    Map<String, String> environmentVariables = new HashMap<>();
    environmentVariables.put("FOO", "BAR");
    environmentVariables.put("STR_1", "value1");
    environmentVariables.put("STR_2", "value2");
    environmentVariables.put("VALUE_WITH_ESCAPE", "value$$");
    environmentVariables.put("EMPTY_STR", "");
    environmentVariables.put("BOOL", "true");
    environmentVariables.put("INT", "1");
    environmentVariables.put("FLOAT", "1.1");
    environmentVariables.put("HEX", "0xdeadbeef");

    Object yaml =
        DeclarativeConfiguration.loadYaml(
            new ByteArrayInputStream(rawYaml.getBytes(StandardCharsets.UTF_8)),
            environmentVariables,
            Collections.emptyMap());
    assertThat(yaml).isEqualTo(expectedYamlResult);
  }

  @SuppressWarnings("unchecked")
  private static Stream<Arguments> envVarSubstitutionArgs() {
    return Stream.of(
        // Simple cases
        Arguments.of("key1: ${STR_1}\n", mapOf(entry("key1", "value1"))),
        Arguments.of("key1: ${BOOL}\n", mapOf(entry("key1", true))),
        Arguments.of("key1: ${INT}\n", mapOf(entry("key1", 1))),
        Arguments.of("key1: ${FLOAT}\n", mapOf(entry("key1", 1.1))),
        Arguments.of("key1: ${HEX}\n", mapOf(entry("key1", 3735928559L))),
        Arguments.of(
            "key1: ${STR_1}\n" + "key2: value2\n",
            mapOf(entry("key1", "value1"), entry("key2", "value2"))),
        Arguments.of(
            "key1: ${STR_1} value1\n" + "key2: value2\n",
            mapOf(entry("key1", "value1 value1"), entry("key2", "value2"))),
        // Default cases
        Arguments.of("key1: ${NOT_SET:-value1}\n", mapOf(entry("key1", "value1"))),
        Arguments.of("key1: ${NOT_SET:-true}\n", mapOf(entry("key1", true))),
        Arguments.of("key1: ${NOT_SET:-1}\n", mapOf(entry("key1", 1))),
        Arguments.of("key1: ${NOT_SET:-1.1}\n", mapOf(entry("key1", 1.1))),
        Arguments.of("key1: ${NOT_SET:-0xdeadbeef}\n", mapOf(entry("key1", 3735928559L))),
        Arguments.of(
            "key1: ${NOT_SET:-value1} value2\n" + "key2: value2\n",
            mapOf(entry("key1", "value1 value2"), entry("key2", "value2"))),
        // Multiple environment variables referenced
        Arguments.of("key1: ${STR_1}${STR_2}\n", mapOf(entry("key1", "value1value2"))),
        Arguments.of("key1: ${STR_1} ${STR_2}\n", mapOf(entry("key1", "value1 value2"))),
        Arguments.of(
            "key1: ${STR_1} ${NOT_SET:-default} ${STR_2}\n",
            mapOf(entry("key1", "value1 default value2"))),
        // Undefined / empty environment variable
        Arguments.of("key1: ${EMPTY_STR}\n", mapOf(entry("key1", null))),
        Arguments.of("key1: ${STR_3}\n", mapOf(entry("key1", null))),
        Arguments.of("key1: ${STR_1} ${STR_3}\n", mapOf(entry("key1", "value1 "))),
        // Environment variable keys must match pattern: [a-zA-Z_]+[a-zA-Z0-9_]*
        Arguments.of("key1: ${VAR&}\n", mapOf(entry("key1", "${VAR&}"))),
        // Environment variable substitution only takes place in scalar values of maps
        Arguments.of("${STR_1}: value1\n", mapOf(entry("${STR_1}", "value1"))),
        Arguments.of(
            "key1:\n  ${STR_1}: value1\n",
            mapOf(entry("key1", mapOf(entry("${STR_1}", "value1"))))),
        Arguments.of(
            "key1:\n - ${STR_1}\n", mapOf(entry("key1", Collections.singletonList("${STR_1}")))),
        // Double-quoted environment variables
        Arguments.of("key1: \"${HEX}\"\n", mapOf(entry("key1", "0xdeadbeef"))),
        Arguments.of("key1: \"${STR_1}\"\n", mapOf(entry("key1", "value1"))),
        Arguments.of("key1: \"${EMPTY_STR}\"\n", mapOf(entry("key1", ""))),
        Arguments.of("key1: \"${BOOL}\"\n", mapOf(entry("key1", "true"))),
        Arguments.of("key1: \"${INT}\"\n", mapOf(entry("key1", "1"))),
        Arguments.of("key1: \"${FLOAT}\"\n", mapOf(entry("key1", "1.1"))),
        Arguments.of(
            "key1: \"${HEX} ${BOOL} ${INT}\"\n", mapOf(entry("key1", "0xdeadbeef true 1"))),
        // Single-quoted environment variables
        Arguments.of("key1: '${HEX}'\n", mapOf(entry("key1", "0xdeadbeef"))),
        Arguments.of("key1: '${STR_1}'\n", mapOf(entry("key1", "value1"))),
        Arguments.of("key1: '${EMPTY_STR}'\n", mapOf(entry("key1", ""))),
        Arguments.of("key1: '${BOOL}'\n", mapOf(entry("key1", "true"))),
        Arguments.of("key1: '${INT}'\n", mapOf(entry("key1", "1"))),
        Arguments.of("key1: '${FLOAT}'\n", mapOf(entry("key1", "1.1"))),
        Arguments.of("key1: '${HEX} ${BOOL} ${INT}'\n", mapOf(entry("key1", "0xdeadbeef true 1"))),
        // Escaped
        Arguments.of("key1: ${FOO}\n", mapOf(entry("key1", "BAR"))),
        Arguments.of("key1: $${FOO}\n", mapOf(entry("key1", "${FOO}"))),
        Arguments.of("key1: $$${FOO}\n", mapOf(entry("key1", "$BAR"))),
        Arguments.of("key1: $$$${FOO}\n", mapOf(entry("key1", "$${FOO}"))),
        Arguments.of("key1: a $$ b\n", mapOf(entry("key1", "a $ b"))),
        Arguments.of("key1: $$ b\n", mapOf(entry("key1", "$ b"))),
        Arguments.of("key1: a $$\n", mapOf(entry("key1", "a $"))),
        Arguments.of("key1: a $ b\n", mapOf(entry("key1", "a $ b"))),
        Arguments.of("key1: $${STR_1}\n", mapOf(entry("key1", "${STR_1}"))),
        Arguments.of("key1: $${STR_1}$${STR_1}\n", mapOf(entry("key1", "${STR_1}${STR_1}"))),
        Arguments.of("key1: $${STR_1}$$\n", mapOf(entry("key1", "${STR_1}$"))),
        Arguments.of("key1: $$${STR_1}\n", mapOf(entry("key1", "$value1"))),
        Arguments.of("key1: \"$${STR_1}\"\n", mapOf(entry("key1", "${STR_1}"))),
        Arguments.of("key1: $${STR_1} ${STR_2}\n", mapOf(entry("key1", "${STR_1} value2"))),
        Arguments.of("key1: $${STR_1} $${STR_2}\n", mapOf(entry("key1", "${STR_1} ${STR_2}"))),
        Arguments.of("key1: $${NOT_SET:-value1}\n", mapOf(entry("key1", "${NOT_SET:-value1}"))),
        Arguments.of("key1: $${STR_1:-fallback}\n", mapOf(entry("key1", "${STR_1:-fallback}"))),
        Arguments.of("key1: $${STR_1:-${STR_1}}\n", mapOf(entry("key1", "${STR_1:-value1}"))),
        Arguments.of("key1: ${NOT_SET:-${FALLBACK}}\n", mapOf(entry("key1", "${FALLBACK}"))),
        Arguments.of(
            "key1: ${NOT_SET:-$${FALLBACK}}\n", mapOf(entry("key1", "${NOT_SET:-${FALLBACK}}"))),
        Arguments.of("key1: ${VALUE_WITH_ESCAPE}\n", mapOf(entry("key1", "value$$"))));
  }

  private static <K, V> Map.Entry<K, V> entry(K key, @Nullable V value) {
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> mapOf(Map.Entry<String, ?>... entries) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, ?> entry : entries) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  @ParameterizedTest
  @MethodSource("sysPropertySubstitutionArgs")
  void sysPropertySubstituteAndLoadYaml(String rawYaml, Object expectedYamlResult) {
    Map<Object, Object> systemProperties = new HashMap<>();
    systemProperties.put("foo.bar", "BAR");
    systemProperties.put("str.1", "value1");
    systemProperties.put("str.2", "value2");
    systemProperties.put("value.with.escape", "value$$");
    systemProperties.put("empty.str", "");
    systemProperties.put("bool.prop", "true");
    systemProperties.put("int.prop", "1");
    systemProperties.put("float.prop", "1.1");
    systemProperties.put("hex.prop", "0xdeadbeef");

    Object yaml =
        DeclarativeConfiguration.loadYaml(
            new ByteArrayInputStream(rawYaml.getBytes(StandardCharsets.UTF_8)),
            Collections.emptyMap(),
            systemProperties);
    assertThat(yaml).isEqualTo(expectedYamlResult);
  }

  @SuppressWarnings("unchecked")
  private static Stream<Arguments> sysPropertySubstitutionArgs() {
    return Stream.of(
        // Simple cases with sys: prefix
        Arguments.of("key1: ${sys:str.1}\n", mapOf(entry("key1", "value1"))),
        Arguments.of("key1: ${sys:bool.prop}\n", mapOf(entry("key1", true))),
        Arguments.of("key1: ${sys:int.prop}\n", mapOf(entry("key1", 1))),
        Arguments.of("key1: ${sys:float.prop}\n", mapOf(entry("key1", 1.1))),
        Arguments.of("key1: ${sys:hex.prop}\n", mapOf(entry("key1", 3735928559L))),
        // Default values
        Arguments.of("key1: ${sys:not.set:-value1}\n", mapOf(entry("key1", "value1"))),
        Arguments.of("key1: ${sys:not.set:-true}\n", mapOf(entry("key1", true))),
        Arguments.of("key1: ${sys:not.set:-1}\n", mapOf(entry("key1", 1))),
        // Multiple property references
        Arguments.of("key1: ${sys:str.1}${sys:str.2}\n", mapOf(entry("key1", "value1value2"))),
        Arguments.of("key1: ${sys:str.1} ${sys:str.2}\n", mapOf(entry("key1", "value1 value2"))),
        Arguments.of(
            "key1: ${sys:str.1} ${sys:not.set:-default} ${sys:str.2}\n",
            mapOf(entry("key1", "value1 default value2"))),
        // Undefined / empty system property
        Arguments.of("key1: ${sys:empty.str}\n", mapOf(entry("key1", null))),
        Arguments.of("key1: ${sys:str.3}\n", mapOf(entry("key1", null))),
        Arguments.of("key1: ${sys:str.1} ${sys:str.3}\n", mapOf(entry("key1", "value1 "))),
        // Quoted system properties
        Arguments.of("key1: \"${sys:hex.prop}\"\n", mapOf(entry("key1", "0xdeadbeef"))),
        Arguments.of("key1: \"${sys:str.1}\"\n", mapOf(entry("key1", "value1"))),
        Arguments.of("key1: '${sys:str.1}'\n", mapOf(entry("key1", "value1"))),
        // Escaped
        Arguments.of("key1: ${sys:foo.bar}\n", mapOf(entry("key1", "BAR"))),
        Arguments.of("key1: $${sys:foo.bar}\n", mapOf(entry("key1", "${sys:foo.bar}"))),
        Arguments.of("key1: $$${sys:foo.bar}\n", mapOf(entry("key1", "$BAR"))),
        Arguments.of("key1: $$$${sys:foo.bar}\n", mapOf(entry("key1", "$${sys:foo.bar}"))),
        // Mixed env and sys
        Arguments.of("key1: ${sys:value.with.escape}\n", mapOf(entry("key1", "value$$"))));
  }

  @Test
  void read_WithEnvironmentVariables() {
    String yaml =
        "file_format: \"1.0-rc.1\"\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          otlp_http:\n"
            + "            endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT}\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          otlp_http:\n"
            + "            endpoint: ${UNSET_ENV_VAR}\n";
    Map<String, String> envVars = new HashMap<>();
    envVars.put("OTEL_EXPORTER_OTLP_ENDPOINT", "http://collector:4317");
    OpenTelemetryConfigurationModel model =
        DeclarativeConfiguration.parse(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)),
            envVars,
            Collections.emptyMap());
    assertThat(model)
        .isEqualTo(
            new OpenTelemetryConfigurationModel()
                .withFileFormat("1.0-rc.1")
                .withTracerProvider(
                    new TracerProviderModel()
                        .withProcessors(
                            Arrays.asList(
                                new SpanProcessorModel()
                                    .withBatch(
                                        new BatchSpanProcessorModel()
                                            .withExporter(
                                                new SpanExporterModel()
                                                    .withOtlpHttp(
                                                        new OtlpHttpExporterModel()
                                                            .withEndpoint(
                                                                "http://collector:4317")))),
                                new SpanProcessorModel()
                                    .withBatch(
                                        new BatchSpanProcessorModel()
                                            .withExporter(
                                                new SpanExporterModel()
                                                    .withOtlpHttp(
                                                        new OtlpHttpExporterModel())))))));
  }

  @Test
  void read_WithSystemProperties() {
    String yaml =
        "file_format: \"1.0-rc.1\"\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          otlp_http:\n"
            + "            endpoint: ${sys:otel.exporter.otlp.endpoint}\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          otlp_http:\n"
            + "            endpoint: ${sys:unset.sys.prop}\n";
    Map<Object, Object> sysProps = new HashMap<>();
    sysProps.put("otel.exporter.otlp.endpoint", "http://collector:4318");
    OpenTelemetryConfigurationModel model =
        DeclarativeConfiguration.parse(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)),
            Collections.emptyMap(),
            sysProps);
    assertThat(model)
        .isEqualTo(
            new OpenTelemetryConfigurationModel()
                .withFileFormat("1.0-rc.1")
                .withTracerProvider(
                    new TracerProviderModel()
                        .withProcessors(
                            Arrays.asList(
                                new SpanProcessorModel()
                                    .withBatch(
                                        new BatchSpanProcessorModel()
                                            .withExporter(
                                                new SpanExporterModel()
                                                    .withOtlpHttp(
                                                        new OtlpHttpExporterModel()
                                                            .withEndpoint(
                                                                "http://collector:4318")))),
                                new SpanProcessorModel()
                                    .withBatch(
                                        new BatchSpanProcessorModel()
                                            .withExporter(
                                                new SpanExporterModel()
                                                    .withOtlpHttp(
                                                        new OtlpHttpExporterModel())))))));
  }

  @Test
  void read_WithMixedEnvVarsAndSystemProperties() {
    String yaml =
        "file_format: \"1.0-rc.1\"\n"
            + "resource:\n"
            + "  attributes:\n"
            + "    - name: service.name\n"
            + "      value: ${SERVICE_NAME}\n"
            + "    - name: service.version\n"
            + "      value: ${sys:app.version}\n"
            + "    - name: deployment.environment\n"
            + "      value: ${env:DEPLOYMENT_ENV:-production}\n";
    Map<String, String> envVars = new HashMap<>();
    envVars.put("SERVICE_NAME", "my-service");
    Map<Object, Object> sysProps = new HashMap<>();
    sysProps.put("app.version", "1.2.3");
    OpenTelemetryConfigurationModel model =
        DeclarativeConfiguration.parse(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)), envVars, sysProps);
    assertThat(model)
        .isEqualTo(
            new OpenTelemetryConfigurationModel()
                .withFileFormat("1.0-rc.1")
                .withResource(
                    new ResourceModel()
                        .withAttributes(
                            Arrays.asList(
                                new AttributeNameValueModel()
                                    .withName("service.name")
                                    .withValue("my-service"),
                                new AttributeNameValueModel()
                                    .withName("service.version")
                                    .withValue("1.2.3"),
                                new AttributeNameValueModel()
                                    .withName("deployment.environment")
                                    .withValue("production")))));
  }
}
