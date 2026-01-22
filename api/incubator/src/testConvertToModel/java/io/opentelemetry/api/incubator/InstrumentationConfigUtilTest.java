/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.incubator.config.InstrumentationConfigUtil;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLanguageSpecificInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLanguageSpecificInstrumentationPropertyModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.internal.all.SdkConfigProvider;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class InstrumentationConfigUtilTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void toMap_RoundTrip() throws JsonProcessingException {
    Map<String, Object> map = new HashMap<>();
    map.put("string", "val");
    map.put("boolean", true);
    map.put("long", 1L);
    map.put("double", 1.1);
    map.put("null", null);
    map.put("stringList", Arrays.asList("val1", "val2"));
    map.put("boolList", Arrays.asList(true, false));
    map.put("longList", Arrays.asList(1L, 2L));
    map.put("doubleList", Arrays.asList(1.1d, 2.2d));
    map.put(
        "structuredList", Collections.singletonList(Collections.singletonMap("childKey", "val")));
    map.put("emptyList", Collections.emptyList());
    map.put("structured", Collections.singletonMap("childKey", "val"));
    map.put("emptyStructured", Collections.emptyMap());

    String mapJson = MAPPER.writeValueAsString(map);
    DeclarativeConfigProperties properties =
        DeclarativeConfiguration.toConfigProperties(
            new ByteArrayInputStream(mapJson.getBytes(StandardCharsets.UTF_8)));

    assertThat(DeclarativeConfigProperties.toMap(properties)).isEqualTo(map);
  }

  @Test
  void getInstrumentationConfigModel_UnsetConfig() {
    ConfigProvider configProvider = DeclarativeConfigProperties::empty;

    assertThat(
            InstrumentationConfigUtil.getInstrumentationConfigModel(
                configProvider, "my_instrumentation_library", MAPPER, Model.class))
        .isNull();
  }

  @Test
  void getInstrumentationConfigModel_EmptyConfig() {
    ConfigProvider configProvider =
        withInstrumentationConfig(
            "my_instrumentation_library",
            new ExperimentalLanguageSpecificInstrumentationPropertyModel());

    assertThat(
            InstrumentationConfigUtil.getInstrumentationConfigModel(
                configProvider, "my_instrumentation_library", MAPPER, Model.class))
        .isEqualTo(new Model());
  }

  @Test
  void getInstrumentationConfigModel_KitchenSink() {
    ConfigProvider configProvider =
        withInstrumentationConfig(
            "my_instrumentation_library",
            new ExperimentalLanguageSpecificInstrumentationPropertyModel()
                .withAdditionalProperty("string_property", "value")
                .withAdditionalProperty("boolean_property", true)
                .withAdditionalProperty("long_property", 1L)
                .withAdditionalProperty("double_property", 1.1d)
                .withAdditionalProperty("string_list_property", Arrays.asList("val1", "val2"))
                .withAdditionalProperty("boolean_list_property", Arrays.asList(true, false))
                .withAdditionalProperty("long_list_property", Arrays.asList(1L, 2L))
                .withAdditionalProperty("double_list_property", Arrays.asList(1.1d, 2.2d))
                .withAdditionalProperty("map_property", Collections.singletonMap("childKey", "val"))
                .withAdditionalProperty(
                    "structured_list_property",
                    Collections.singletonList(
                        ImmutableMap.of("key", "the_key", "value", "the_value"))));

    Model expected = new Model();
    expected.stringProperty = "value";
    expected.booleanProperty = true;
    expected.longProperty = 1L;
    expected.doubleProperty = 1.1d;
    expected.stringListProperty = Arrays.asList("val1", "val2");
    expected.booleanListProperty = Arrays.asList(true, false);
    expected.longListProperty = Arrays.asList(1L, 2L);
    expected.doubleListProperty = Arrays.asList(1.1d, 2.2d);
    expected.mapProperty = Collections.singletonMap("childKey", "val");
    ListEntryModel listEntryModel = new ListEntryModel();
    listEntryModel.key = "the_key";
    listEntryModel.value = "the_value";
    expected.structuredListProperty = Collections.singletonList(listEntryModel);

    assertThat(
            InstrumentationConfigUtil.getInstrumentationConfigModel(
                configProvider, "my_instrumentation_library", MAPPER, Model.class))
        .isEqualTo(expected);
  }

  private static ConfigProvider withInstrumentationConfig(
      String instrumentationName,
      ExperimentalLanguageSpecificInstrumentationPropertyModel instrumentationConfig) {
    ExperimentalLanguageSpecificInstrumentationModel javaConfig =
        new ExperimentalLanguageSpecificInstrumentationModel();
    javaConfig.setAdditionalProperty(instrumentationName, instrumentationConfig);
    DeclarativeConfigProperties modelProperties =
        DeclarativeConfiguration.toConfigProperties(
            new OpenTelemetryConfigurationModel()
                .withInstrumentationDevelopment(
                    new ExperimentalInstrumentationModel().withJava(javaConfig)));

    return SdkConfigProvider.create(modelProperties);
  }

  private static class Model {
    @JsonProperty("string_property")
    private String stringProperty;

    @JsonProperty("boolean_property")
    private Boolean booleanProperty;

    @JsonProperty("long_property")
    private Long longProperty;

    @JsonProperty("double_property")
    private Double doubleProperty;

    @JsonProperty("string_list_property")
    private List<String> stringListProperty;

    @JsonProperty("boolean_list_property")
    private List<Boolean> booleanListProperty;

    @JsonProperty("long_list_property")
    private List<Long> longListProperty;

    @JsonProperty("double_list_property")
    private List<Double> doubleListProperty;

    ;

    @JsonProperty("map_property")
    private Map<String, Object> mapProperty;

    @JsonProperty("structured_list_property")
    private List<ListEntryModel> structuredListProperty;

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Model)) {
        return false;
      }
      Model model = (Model) o;
      return Objects.equals(stringProperty, model.stringProperty)
          && Objects.equals(booleanProperty, model.booleanProperty)
          && Objects.equals(longProperty, model.longProperty)
          && Objects.equals(doubleProperty, model.doubleProperty)
          && Objects.equals(stringListProperty, model.stringListProperty)
          && Objects.equals(booleanListProperty, model.booleanListProperty)
          && Objects.equals(longListProperty, model.longListProperty)
          && Objects.equals(doubleListProperty, model.doubleListProperty)
          && Objects.equals(mapProperty, model.mapProperty)
          && Objects.equals(structuredListProperty, model.structuredListProperty);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          stringProperty,
          booleanProperty,
          longProperty,
          doubleProperty,
          stringListProperty,
          booleanListProperty,
          longListProperty,
          doubleListProperty,
          mapProperty,
          structuredListProperty);
    }

    @Override
    public String toString() {
      return "Model{"
          + "stringProperty='"
          + stringProperty
          + '\''
          + ", booleanProperty='"
          + booleanProperty
          + '\''
          + ", longProperty='"
          + longProperty
          + '\''
          + ", doubleProperty='"
          + doubleProperty
          + '\''
          + ", stringListProperty="
          + stringListProperty
          + ", booleanListProperty="
          + booleanListProperty
          + ", longListProperty="
          + longListProperty
          + ", doubleListProperty="
          + doubleListProperty
          + ", mapProperty="
          + mapProperty
          + ", structuredListProperty="
          + structuredListProperty
          + '}';
    }
  }

  private static final class ListEntryModel {
    @JsonProperty("key")
    private String key;

    @JsonProperty("value")
    private String value;

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ListEntryModel that = (ListEntryModel) o;
      return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(key, value);
    }

    @Override
    public String toString() {
      return "ListEntryModel{" + "key='" + key + '\'' + ", value='" + value + '\'' + '}';
    }
  }
}
