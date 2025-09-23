/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.api.incubator.config.DeclarativeConfigProperties.empty;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class YamlDeclarativeConfigPropertiesTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(YamlDeclarativeConfigProperties.class);

  private static final String extendedSchema =
      "file_format: \"1.0-rc.1\"\n"
          + "disabled: false\n"
          + "\n"
          + "resource:\n"
          + "  attributes:\n"
          + "    - name: service.name\n"
          + "      value: \"unknown_service\"\n"
          + "\n"
          + "other:\n"
          + "  str_key: str_value\n"
          + "  int_key: 1\n"
          + "  float_key: 1.1\n"
          + "  bool_key: true\n"
          + "  null_key:\n"
          + "  str_list_key: [val1, val2]\n"
          + "  int_list_key: [1, 2]\n"
          + "  float_list_key: [1.1, 2.2]\n"
          + "  bool_list_key: [true, false]\n"
          + "  mixed_list_key: [val1, 1, 1.1, true]\n"
          + "  map_key:\n"
          + "    str_key1: str_value1\n"
          + "    int_key1: 2\n"
          + "    map_key1:\n"
          + "      str_key2: str_value2\n"
          + "      int_key2: 3\n"
          + "  list_key:\n"
          + "    - str_key1: str_value1\n"
          + "      int_key1: 2\n"
          + "      map_key1:\n"
          + "        str_key2: str_value2\n"
          + "        int_key2: 3\n"
          + "    - str_key1: str_value1\n"
          + "      int_key1: 2";

  private DeclarativeConfigProperties structuredConfigProps;

  @BeforeEach
  void setup() {
    OpenTelemetryConfigurationModel configuration =
        DeclarativeConfiguration.parse(
            new ByteArrayInputStream(extendedSchema.getBytes(StandardCharsets.UTF_8)));
    structuredConfigProps = DeclarativeConfiguration.toConfigProperties(configuration);
  }

  @Test
  void configurationSchema() {
    // Validate can read declarative configuration schema properties
    assertThat(structuredConfigProps.getString("file_format")).isEqualTo("1.0-rc.1");
    DeclarativeConfigProperties resourceProps = structuredConfigProps.getStructured("resource");
    assertThat(resourceProps).isNotNull();
    List<DeclarativeConfigProperties> resourceAttributesList =
        resourceProps.getStructuredList("attributes");
    assertThat(resourceAttributesList)
        .isNotNull()
        .satisfiesExactly(
            attributeEntry -> {
              assertThat(attributeEntry.getString("name")).isEqualTo("service.name");
              assertThat(attributeEntry.getString("value")).isEqualTo("unknown_service");
            });
  }

  @Test
  void additionalProperties() {
    assertThat(structuredConfigProps.getPropertyKeys())
        .isEqualTo(ImmutableSet.of("file_format", "disabled", "resource", "other"));

    // Validate can read properties not part of configuration schema
    // .other
    DeclarativeConfigProperties otherProps = structuredConfigProps.getStructured("other");
    assertThat(otherProps).isNotNull();
    assertThat(otherProps.getPropertyKeys())
        .isEqualTo(
            ImmutableSet.of(
                "str_key",
                "int_key",
                "float_key",
                "bool_key",
                "null_key",
                "str_list_key",
                "int_list_key",
                "float_list_key",
                "bool_list_key",
                "mixed_list_key",
                "map_key",
                "list_key"));
    assertThat(otherProps.getString("str_key")).isEqualTo("str_value");
    assertThat(otherProps.getInt("int_key")).isEqualTo(1);
    assertThat(otherProps.getLong("int_key")).isEqualTo(1);
    assertThat(otherProps.getDouble("float_key")).isEqualTo(1.1);
    assertThat(otherProps.getString("null_key")).isNull();
    assertThat(otherProps.getInt("null_key")).isNull();
    assertThat(otherProps.getLong("null_key")).isNull();
    assertThat(otherProps.getBoolean("null_key")).isNull();
    assertThat(otherProps.getScalarList("str_list_key", String.class))
        .isEqualTo(Arrays.asList("val1", "val2"));
    assertThat(otherProps.getScalarList("int_list_key", Long.class))
        .isEqualTo(Arrays.asList(1L, 2L));
    assertThat(otherProps.getScalarList("float_list_key", Double.class))
        .isEqualTo(Arrays.asList(1.1d, 2.2d));
    assertThat(otherProps.getScalarList("bool_list_key", Boolean.class))
        .isEqualTo(Arrays.asList(true, false));
    // If reading a scalar list which is mixed, entries which are not aligned with the requested
    // type are filtered out
    assertThat(otherProps.getScalarList("mixed_list_key", String.class))
        .isEqualTo(Collections.singletonList("val1"));
    assertThat(otherProps.getScalarList("mixed_list_key", Long.class))
        .isEqualTo(Collections.singletonList(1L));
    assertThat(otherProps.getScalarList("mixed_list_key", Double.class))
        .isEqualTo(Collections.singletonList(1.1d));
    assertThat(otherProps.getScalarList("mixed_list_key", Boolean.class))
        .isEqualTo(Collections.singletonList(true));

    // .other.map_key
    DeclarativeConfigProperties otherMapKeyProps = otherProps.getStructured("map_key");
    assertThat(otherMapKeyProps).isNotNull();
    assertThat(otherMapKeyProps.getPropertyKeys())
        .isEqualTo(ImmutableSet.of("str_key1", "int_key1", "map_key1"));
    assertThat(otherMapKeyProps.getString("str_key1")).isEqualTo("str_value1");
    assertThat(otherMapKeyProps.getInt("int_key1")).isEqualTo(2);
    // other.map_key.map_key1
    DeclarativeConfigProperties otherMapKeyMapKey1Props =
        otherMapKeyProps.getStructured("map_key1");
    assertThat(otherMapKeyMapKey1Props).isNotNull();
    assertThat(otherMapKeyMapKey1Props.getPropertyKeys())
        .isEqualTo(ImmutableSet.of("str_key2", "int_key2"));
    assertThat(otherMapKeyMapKey1Props.getString("str_key2")).isEqualTo("str_value2");
    assertThat(otherMapKeyMapKey1Props.getInt("int_key2")).isEqualTo(3);

    // .other.list_key
    List<DeclarativeConfigProperties> listKey = otherProps.getStructuredList("list_key");
    assertThat(listKey).hasSize(2);
    DeclarativeConfigProperties listKeyProps1 = listKey.get(0);
    assertThat(listKeyProps1.getPropertyKeys())
        .isEqualTo(ImmutableSet.of("str_key1", "int_key1", "map_key1"));
    assertThat(listKeyProps1.getString("str_key1")).isEqualTo("str_value1");
    assertThat(listKeyProps1.getInt("int_key1")).isEqualTo(2);
    // .other.list_key[0]
    DeclarativeConfigProperties listKeyProps1MapKeyProps = listKeyProps1.getStructured("map_key1");
    assertThat(listKeyProps1MapKeyProps).isNotNull();
    assertThat(listKeyProps1MapKeyProps.getPropertyKeys())
        .isEqualTo(ImmutableSet.of("str_key2", "int_key2"));
    assertThat(listKeyProps1MapKeyProps.getString("str_key2")).isEqualTo("str_value2");
    assertThat(listKeyProps1MapKeyProps.getInt("int_key2")).isEqualTo(3);
    // .other.list_key[1]
    DeclarativeConfigProperties listKeyProps2 = listKey.get(1);
    assertThat(listKeyProps2.getPropertyKeys()).isEqualTo(ImmutableSet.of("str_key1", "int_key1"));
    assertThat(listKeyProps2.getString("str_key1")).isEqualTo("str_value1");
    assertThat(listKeyProps2.getInt("int_key1")).isEqualTo(2);
  }

  @Test
  void treeWalking() {
    // Validate common pattern of walking down tree path which is not defined
    // Access string at .foo.bar.baz without null checking and without exception.
    assertThat(
            structuredConfigProps
                .getStructured("foo", empty())
                .getStructured("bar", empty())
                .getString("baz"))
        .isNull();
  }

  @Test
  void defaults() {
    assertThat(structuredConfigProps.getString("foo", "bar")).isEqualTo("bar");
    assertThat(structuredConfigProps.getInt("foo", 1)).isEqualTo(1);
    assertThat(structuredConfigProps.getLong("foo", 1)).isEqualTo(1);
    assertThat(structuredConfigProps.getDouble("foo", 1.1)).isEqualTo(1.1);
    assertThat(structuredConfigProps.getBoolean("foo", true)).isTrue();
    assertThat(
            structuredConfigProps.getScalarList(
                "foo", String.class, Collections.singletonList("bar")))
        .isEqualTo(Collections.singletonList("bar"));
    assertThat(structuredConfigProps.getStructured("foo", empty())).isEqualTo(empty());
    assertThat(structuredConfigProps.getStructuredList("foo", Collections.emptyList()))
        .isEqualTo(Collections.emptyList());
  }

  @Test
  void missingKeys() {
    assertThat(structuredConfigProps.getString("foo")).isNull();
    assertThat(structuredConfigProps.getInt("foo")).isNull();
    assertThat(structuredConfigProps.getLong("foo")).isNull();
    assertThat(structuredConfigProps.getDouble("foo")).isNull();
    assertThat(structuredConfigProps.getBoolean("foo")).isNull();
    assertThat(structuredConfigProps.getScalarList("foo", String.class)).isNull();
    assertThat(structuredConfigProps.getStructured("foo")).isNull();
    assertThat(structuredConfigProps.getStructuredList("foo")).isNull();
  }

  @Test
  void wrongType() {
    DeclarativeConfigProperties otherProps = structuredConfigProps.getStructured("other");
    assertThat(otherProps).isNotNull();

    assertThat(otherProps.getString("int_key")).isNull();
    assertThat(otherProps.getInt("str_key")).isNull();
    assertThat(otherProps.getLong("str_key")).isNull();
    assertThat(otherProps.getDouble("str_key")).isNull();
    assertThat(otherProps.getBoolean("str_key")).isNull();
    assertThat(otherProps.getScalarList("str_key", String.class)).isNull();
    assertThat(otherProps.getStructured("str_key")).isNull();
    assertThat(otherProps.getStructuredList("str_key")).isNull();

    assertWarning("Ignoring value for key [int_key] because it is Integer instead of String: 1");
    assertWarning(
        "Ignoring value for key [str_key] because it is String instead of Long: str_value");
    assertWarning(
        "Ignoring value for key [str_key] because it is String instead of Double: str_value");
    assertWarning(
        "Ignoring value for key [str_key] because it is String instead of Boolean: str_value");
  }

  private void assertWarning(String message) {
    logs.assertContains(
        e ->
            String.format(e.getMessage().replaceAll("\\{\\d}", "%s"), e.getArgumentArray())
                .contains(message),
        message);
  }

  @Test
  void emptyProperties() {
    assertThat(empty().getString("foo")).isNull();
    assertThat(empty().getInt("foo")).isNull();
    assertThat(empty().getLong("foo")).isNull();
    assertThat(empty().getDouble("foo")).isNull();
    assertThat(empty().getBoolean("foo")).isNull();
    assertThat(empty().getScalarList("foo", String.class)).isNull();
    assertThat(empty().getStructured("foo")).isNull();
    assertThat(empty().getStructuredList("foo")).isNull();
    assertThat(empty().getString("foo", "bar")).isEqualTo("bar");
    assertThat(empty().getInt("foo", 1)).isEqualTo(1);
    assertThat(empty().getLong("foo", 1)).isEqualTo(1);
    assertThat(empty().getDouble("foo", 1.1)).isEqualTo(1.1);
    assertThat(empty().getBoolean("foo", true)).isTrue();
    assertThat(empty().getScalarList("foo", String.class, Collections.singletonList("bar")))
        .isEqualTo(Collections.singletonList("bar"));
    assertThat(empty().getStructured("foo", empty())).isEqualTo(empty());
    assertThat(empty().getStructuredList("foo", Collections.emptyList()))
        .isEqualTo(Collections.emptyList());
  }
}
