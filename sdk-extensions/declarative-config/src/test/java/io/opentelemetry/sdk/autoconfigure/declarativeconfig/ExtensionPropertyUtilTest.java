/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordLimitsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LoggerProviderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ModelMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

class ExtensionPropertyUtilTest {

  @RegisterExtension
  LogCapturer logs =
      LogCapturer.create().captureForLogger(ExtensionPropertyUtil.class.getName(), Level.TRACE);

  @Test
  void handleAnySetter_knownExperimentalKey_convertsAndStores() {
    Map<String, Object> props = new LinkedHashMap<>();
    Map<String, Class<?>> experimentalTypes = new HashMap<>();
    experimentalTypes.put("composite/development", ExperimentalComposableSamplerModel.class);

    ExtensionPropertyUtil.handleAnySetter(
        "composite/development",
        new LinkedHashMap<>(),
        props,
        experimentalTypes,
        Collections.emptyMap(),
        false);

    assertThat(props.get("composite/development"))
        .isInstanceOf(ExperimentalComposableSamplerModel.class);
  }

  @Test
  void handleAnySetter_knownExperimentalKey_nullValue_storesNull() {
    Map<String, Object> props = new LinkedHashMap<>();
    Map<String, Class<?>> experimentalTypes = new HashMap<>();
    experimentalTypes.put("composite/development", ExperimentalComposableSamplerModel.class);

    ExtensionPropertyUtil.handleAnySetter(
        "composite/development", null, props, experimentalTypes, Collections.emptyMap(), false);

    assertThat(props).containsEntry("composite/development", null);
  }

  @Test
  void handleAnySetter_graduatedKey_storesUnderDevelopmentKey_andWarns() {
    Map<String, Object> props = new LinkedHashMap<>();
    Map<String, Class<?>> stableTypes = new HashMap<>();
    stableTypes.put("limits", LogRecordLimitsModel.class);

    ExtensionPropertyUtil.handleAnySetter(
        "limits/development",
        new LinkedHashMap<>(),
        props,
        Collections.emptyMap(),
        stableTypes,
        false);

    assertThat(props).doesNotContainKey("limits");
    assertThat(props.get("limits/development")).isInstanceOf(LogRecordLimitsModel.class);
    logs.assertContains("Property 'limits/development' has been stabilized. Use 'limits' instead.");
  }

  @Test
  void handleAnySetter_unknownKey_openType_stores() {
    Map<String, Object> props = new LinkedHashMap<>();

    ExtensionPropertyUtil.handleAnySetter(
        "custom_exporter",
        "config_value",
        props,
        Collections.emptyMap(),
        Collections.emptyMap(),
        true);

    assertThat(props).containsEntry("custom_exporter", "config_value");
  }

  @Test
  void handleAnySetter_unknownKey_closedType_doesNotStore_andWarns() {
    Map<String, Object> props = new LinkedHashMap<>();

    ExtensionPropertyUtil.handleAnySetter(
        "unknown_key", "value", props, Collections.emptyMap(), Collections.emptyMap(), false);

    assertThat(props).isEmpty();
    logs.assertContains("Unknown property 'unknown_key' is not recognized and will be ignored.");
  }

  @Test
  void get_absentKey_returnsNull() {
    assertThat(
            ExtensionPropertyUtil.get("missing", new LinkedHashMap<>(), LogRecordLimitsModel.class))
        .isNull();
  }

  @Test
  void get_presentKey_returnsTypedValue() {
    Map<String, Object> props = new LinkedHashMap<>();
    LogRecordLimitsModel limits = new LogRecordLimitsModel();
    props.put("limits", limits);

    assertThat(ExtensionPropertyUtil.get("limits", props, LogRecordLimitsModel.class))
        .isSameAs(limits);
  }

  @Test
  void stableGetter_returnsGraduatedValue_fromExtensionPropertiesFallback() {
    LoggerProviderModel model = new LoggerProviderModel();
    model.withExtensionProperty("limits/development", new LinkedHashMap<>());

    assertThat(model.getLimits()).isNotNull().isInstanceOf(LogRecordLimitsModel.class);
  }

  @Test
  void filterSerializable_noStableProperties_returnsInputUnchanged() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("anything/development", "x");
    assertThat(ExtensionPropertyUtil.filterSerializable(props, Collections.emptyMap()))
        .isSameAs(props);
  }

  @Test
  void filterSerializable_noMatchingGraduatedKeys_returnsInputUnchanged() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("composite/development", "x");
    Map<String, Class<?>> stableProperties = new HashMap<>();
    stableProperties.put("limits", LogRecordLimitsModel.class);

    assertThat(ExtensionPropertyUtil.filterSerializable(props, stableProperties)).isSameAs(props);
  }

  @Test
  void filterSerializable_dropsGraduatedKeys_preservesOthers() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("limits/development", "graduated");
    props.put("composite/development", "still-experimental");
    props.put("custom_key", "open-schema-extension");
    Map<String, Class<?>> stableProperties = new HashMap<>();
    stableProperties.put("limits", LogRecordLimitsModel.class);

    Map<String, Object> filtered =
        ExtensionPropertyUtil.filterSerializable(props, stableProperties);

    assertThat(filtered)
        .doesNotContainKey("limits/development")
        .containsEntry("composite/development", "still-experimental")
        .containsEntry("custom_key", "open-schema-extension");
    assertThat(props).containsKey("limits/development");
  }

  @Test
  void stableModel_roundTripsGraduatedKey_emitsStableKeyOnly() throws Exception {
    String json = "{\"limits/development\":{}}";
    LoggerProviderModel model = ModelMapper.MAPPER.readValue(json, LoggerProviderModel.class);

    assertThat(model.getLimits()).isNotNull().isInstanceOf(LogRecordLimitsModel.class);

    String reserialized = ModelMapper.MAPPER.writeValueAsString(model);
    assertThat(reserialized).contains("\"limits\":").doesNotContain("limits/development");
  }

  @Test
  void stableModel_roundTripsNonGraduatedDevelopmentKey_verbatim() throws Exception {
    String json = "{\"composite/development\":{}}";
    SamplerModel model = ModelMapper.MAPPER.readValue(json, SamplerModel.class);

    String reserialized = ModelMapper.MAPPER.writeValueAsString(model);
    assertThat(reserialized).contains("composite/development");
  }
}
