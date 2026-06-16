/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AggregationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeLimitsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeNameValueModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ResourceModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TraceIdRatioBasedSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TracerProviderModel;
import java.io.ByteArrayInputStream;
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
  void parse_nullValuesParsedToEmptyObjects() {
    String objectPlaceholderString =
        "file_format: \"1.0\"\n"
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
        "file_format: \"1.0\"\n"
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
        Arguments.argumentSet("octal with o", "key1: 0o123\n", mapOf(entry("key1", 83))),
        Arguments.argumentSet("octal without o", "key1: 0123\n", mapOf(entry("key1", 123))),
        Arguments.argumentSet(
            "unquoted hex string", "key1: 0xdeadbeef\n", mapOf(entry("key1", 3735928559L))),
        Arguments.argumentSet(
            "quoted hex string", "key1: \"0xdeadbeef\"\n", mapOf(entry("key1", "0xdeadbeef"))));
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
        Arguments.argumentSet("STR_1 string", "key1: ${STR_1}\n", mapOf(entry("key1", "value1"))),
        Arguments.argumentSet("BOOL boolean", "key1: ${BOOL}\n", mapOf(entry("key1", true))),
        Arguments.argumentSet("INT integer", "key1: ${INT}\n", mapOf(entry("key1", 1))),
        Arguments.argumentSet("FLOAT float", "key1: ${FLOAT}\n", mapOf(entry("key1", 1.1))),
        Arguments.argumentSet("HEX hex", "key1: ${HEX}\n", mapOf(entry("key1", 3735928559L))),
        Arguments.argumentSet(
            "STR_1 with literal key2",
            "key1: ${STR_1}\n" + "key2: value2\n",
            mapOf(entry("key1", "value1"), entry("key2", "value2"))),
        Arguments.argumentSet(
            "STR_1 concatenated with literal",
            "key1: ${STR_1} value1\n" + "key2: value2\n",
            mapOf(entry("key1", "value1 value1"), entry("key2", "value2"))),
        Arguments.argumentSet(
            "default string", "key1: ${NOT_SET:-value1}\n", mapOf(entry("key1", "value1"))),
        Arguments.argumentSet(
            "default boolean", "key1: ${NOT_SET:-true}\n", mapOf(entry("key1", true))),
        Arguments.argumentSet("default integer", "key1: ${NOT_SET:-1}\n", mapOf(entry("key1", 1))),
        Arguments.argumentSet(
            "default float", "key1: ${NOT_SET:-1.1}\n", mapOf(entry("key1", 1.1))),
        Arguments.argumentSet(
            "default hex", "key1: ${NOT_SET:-0xdeadbeef}\n", mapOf(entry("key1", 3735928559L))),
        Arguments.argumentSet(
            "default concatenated with literal",
            "key1: ${NOT_SET:-value1} value2\n" + "key2: value2\n",
            mapOf(entry("key1", "value1 value2"), entry("key2", "value2"))),
        Arguments.argumentSet(
            "two vars concatenated",
            "key1: ${STR_1}${STR_2}\n",
            mapOf(entry("key1", "value1value2"))),
        Arguments.argumentSet(
            "two vars with space",
            "key1: ${STR_1} ${STR_2}\n",
            mapOf(entry("key1", "value1 value2"))),
        Arguments.argumentSet(
            "two vars with default",
            "key1: ${STR_1} ${NOT_SET:-default} ${STR_2}\n",
            mapOf(entry("key1", "value1 default value2"))),
        Arguments.argumentSet("empty var null", "key1: ${EMPTY_STR}\n", mapOf(entry("key1", null))),
        Arguments.argumentSet("undefined var null", "key1: ${STR_3}\n", mapOf(entry("key1", null))),
        Arguments.argumentSet(
            "set and undefined vars", "key1: ${STR_1} ${STR_3}\n", mapOf(entry("key1", "value1 "))),
        // Environment variable keys must match pattern: [a-zA-Z_]+[a-zA-Z0-9_]*
        Arguments.argumentSet(
            "invalid var syntax literal", "key1: ${VAR&}\n", mapOf(entry("key1", "${VAR&}"))),
        // Environment variable substitution only takes place in scalar values of maps
        Arguments.argumentSet(
            "var in map key not substituted",
            "${STR_1}: value1\n",
            mapOf(entry("${STR_1}", "value1"))),
        Arguments.argumentSet(
            "var in nested map key not substituted",
            "key1:\n  ${STR_1}: value1\n",
            mapOf(entry("key1", mapOf(entry("${STR_1}", "value1"))))),
        Arguments.argumentSet(
            "var in list not substituted",
            "key1:\n - ${STR_1}\n",
            mapOf(entry("key1", Collections.singletonList("${STR_1}")))),
        Arguments.argumentSet(
            "double-quoted hex string", "key1: \"${HEX}\"\n", mapOf(entry("key1", "0xdeadbeef"))),
        Arguments.argumentSet(
            "double-quoted string", "key1: \"${STR_1}\"\n", mapOf(entry("key1", "value1"))),
        Arguments.argumentSet(
            "double-quoted empty string", "key1: \"${EMPTY_STR}\"\n", mapOf(entry("key1", ""))),
        Arguments.argumentSet(
            "double-quoted boolean string", "key1: \"${BOOL}\"\n", mapOf(entry("key1", "true"))),
        Arguments.argumentSet(
            "double-quoted integer string", "key1: \"${INT}\"\n", mapOf(entry("key1", "1"))),
        Arguments.argumentSet(
            "double-quoted float string", "key1: \"${FLOAT}\"\n", mapOf(entry("key1", "1.1"))),
        Arguments.argumentSet(
            "double-quoted multiple vars",
            "key1: \"${HEX} ${BOOL} ${INT}\"\n",
            mapOf(entry("key1", "0xdeadbeef true 1"))),
        Arguments.argumentSet(
            "single-quoted hex string", "key1: '${HEX}'\n", mapOf(entry("key1", "0xdeadbeef"))),
        Arguments.argumentSet(
            "single-quoted string", "key1: '${STR_1}'\n", mapOf(entry("key1", "value1"))),
        Arguments.argumentSet(
            "single-quoted empty string", "key1: '${EMPTY_STR}'\n", mapOf(entry("key1", ""))),
        Arguments.argumentSet(
            "single-quoted boolean string", "key1: '${BOOL}'\n", mapOf(entry("key1", "true"))),
        Arguments.argumentSet(
            "single-quoted integer string", "key1: '${INT}'\n", mapOf(entry("key1", "1"))),
        Arguments.argumentSet(
            "single-quoted float string", "key1: '${FLOAT}'\n", mapOf(entry("key1", "1.1"))),
        Arguments.argumentSet(
            "single-quoted multiple vars",
            "key1: '${HEX} ${BOOL} ${INT}'\n",
            mapOf(entry("key1", "0xdeadbeef true 1"))),
        Arguments.argumentSet("FOO var", "key1: ${FOO}\n", mapOf(entry("key1", "BAR"))),
        Arguments.argumentSet(
            "double-dollar escape", "key1: $${FOO}\n", mapOf(entry("key1", "${FOO}"))),
        Arguments.argumentSet("triple-dollar", "key1: $$${FOO}\n", mapOf(entry("key1", "$BAR"))),
        Arguments.argumentSet(
            "quad-dollar escape", "key1: $$$${FOO}\n", mapOf(entry("key1", "$${FOO}"))),
        Arguments.argumentSet(
            "double-dollar in middle", "key1: a $$ b\n", mapOf(entry("key1", "a $ b"))),
        Arguments.argumentSet("double-dollar prefix", "key1: $$ b\n", mapOf(entry("key1", "$ b"))),
        Arguments.argumentSet("double-dollar suffix", "key1: a $$\n", mapOf(entry("key1", "a $"))),
        Arguments.argumentSet(
            "single dollar literal", "key1: a $ b\n", mapOf(entry("key1", "a $ b"))),
        Arguments.argumentSet("escape var", "key1: $${STR_1}\n", mapOf(entry("key1", "${STR_1}"))),
        Arguments.argumentSet(
            "escape two vars",
            "key1: $${STR_1}$${STR_1}\n",
            mapOf(entry("key1", "${STR_1}${STR_1}"))),
        Arguments.argumentSet(
            "escape var with dollar", "key1: $${STR_1}$$\n", mapOf(entry("key1", "${STR_1}$"))),
        Arguments.argumentSet(
            "triple-dollar var", "key1: $$${STR_1}\n", mapOf(entry("key1", "$value1"))),
        Arguments.argumentSet(
            "double-quoted escape var", "key1: \"$${STR_1}\"\n", mapOf(entry("key1", "${STR_1}"))),
        Arguments.argumentSet(
            "escape var then real var",
            "key1: $${STR_1} ${STR_2}\n",
            mapOf(entry("key1", "${STR_1} value2"))),
        Arguments.argumentSet(
            "two escape vars",
            "key1: $${STR_1} $${STR_2}\n",
            mapOf(entry("key1", "${STR_1} ${STR_2}"))),
        Arguments.argumentSet(
            "escape default",
            "key1: $${NOT_SET:-value1}\n",
            mapOf(entry("key1", "${NOT_SET:-value1}"))),
        Arguments.argumentSet(
            "escape var with fallback",
            "key1: $${STR_1:-fallback}\n",
            mapOf(entry("key1", "${STR_1:-fallback}"))),
        Arguments.argumentSet(
            "escape var with nested default",
            "key1: $${STR_1:-${STR_1}}\n",
            mapOf(entry("key1", "${STR_1:-value1}"))),
        Arguments.argumentSet(
            "nested var in default",
            "key1: ${NOT_SET:-${FALLBACK}}\n",
            mapOf(entry("key1", "${FALLBACK}"))),
        Arguments.argumentSet(
            "escape in default",
            "key1: ${NOT_SET:-$${FALLBACK}}\n",
            mapOf(entry("key1", "${NOT_SET:-${FALLBACK}}"))),
        Arguments.argumentSet(
            "value with escape", "key1: ${VALUE_WITH_ESCAPE}\n", mapOf(entry("key1", "value$$"))));
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
        Arguments.argumentSet(
            "sys str.1 string", "key1: ${sys:str.1}\n", mapOf(entry("key1", "value1"))),
        Arguments.argumentSet(
            "sys bool.prop boolean", "key1: ${sys:bool.prop}\n", mapOf(entry("key1", true))),
        Arguments.argumentSet(
            "sys int.prop integer", "key1: ${sys:int.prop}\n", mapOf(entry("key1", 1))),
        Arguments.argumentSet(
            "sys float.prop float", "key1: ${sys:float.prop}\n", mapOf(entry("key1", 1.1))),
        Arguments.argumentSet(
            "sys hex.prop hex", "key1: ${sys:hex.prop}\n", mapOf(entry("key1", 3735928559L))),
        Arguments.argumentSet(
            "sys default string", "key1: ${sys:not.set:-value1}\n", mapOf(entry("key1", "value1"))),
        Arguments.argumentSet(
            "sys default boolean", "key1: ${sys:not.set:-true}\n", mapOf(entry("key1", true))),
        Arguments.argumentSet(
            "sys default integer", "key1: ${sys:not.set:-1}\n", mapOf(entry("key1", 1))),
        Arguments.argumentSet(
            "sys two props concatenated",
            "key1: ${sys:str.1}${sys:str.2}\n",
            mapOf(entry("key1", "value1value2"))),
        Arguments.argumentSet(
            "sys two props with space",
            "key1: ${sys:str.1} ${sys:str.2}\n",
            mapOf(entry("key1", "value1 value2"))),
        Arguments.argumentSet(
            "sys two props with default",
            "key1: ${sys:str.1} ${sys:not.set:-default} ${sys:str.2}\n",
            mapOf(entry("key1", "value1 default value2"))),
        Arguments.argumentSet(
            "sys empty.str null", "key1: ${sys:empty.str}\n", mapOf(entry("key1", null))),
        Arguments.argumentSet(
            "sys undefined null", "key1: ${sys:str.3}\n", mapOf(entry("key1", null))),
        Arguments.argumentSet(
            "sys set and undefined",
            "key1: ${sys:str.1} ${sys:str.3}\n",
            mapOf(entry("key1", "value1 "))),
        Arguments.argumentSet(
            "sys double-quoted hex string",
            "key1: \"${sys:hex.prop}\"\n",
            mapOf(entry("key1", "0xdeadbeef"))),
        Arguments.argumentSet(
            "sys double-quoted string", "key1: \"${sys:str.1}\"\n", mapOf(entry("key1", "value1"))),
        Arguments.argumentSet(
            "sys single-quoted string", "key1: '${sys:str.1}'\n", mapOf(entry("key1", "value1"))),
        Arguments.argumentSet("sys foo.bar", "key1: ${sys:foo.bar}\n", mapOf(entry("key1", "BAR"))),
        Arguments.argumentSet(
            "sys double-dollar escape",
            "key1: $${sys:foo.bar}\n",
            mapOf(entry("key1", "${sys:foo.bar}"))),
        Arguments.argumentSet(
            "sys triple-dollar", "key1: $$${sys:foo.bar}\n", mapOf(entry("key1", "$BAR"))),
        Arguments.argumentSet(
            "sys quad-dollar escape",
            "key1: $$$${sys:foo.bar}\n",
            mapOf(entry("key1", "$${sys:foo.bar}"))),
        Arguments.argumentSet(
            "sys value with escape",
            "key1: ${sys:value.with.escape}\n",
            mapOf(entry("key1", "value$$"))));
  }

  @Test
  void read_WithEnvironmentVariables() {
    String yaml =
        "file_format: \"1.0\"\n"
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
                .withFileFormat("1.0")
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
        "file_format: \"1.0\"\n"
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
                .withFileFormat("1.0")
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
        "file_format: \"1.0\"\n"
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
                .withFileFormat("1.0")
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
