/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class YamlStructuredConfigPropertiesTest {

  private static final String extendedSchema =
      "file_format: \"0.1\"\n"
          + "disabled: false\n"
          + "\n"
          + "resource:\n"
          + "  attributes:\n"
          + "    service.name: \"unknown_service\"\n"
          + "\n"
          + "other:\n"
          + "  str_key: str_value\n"
          + "  int_key: 1\n"
          + "  float_key: 1.1\n"
          + "  bool_key: true\n"
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

  private StructuredConfigProperties structuredConfigProps;

  @BeforeEach
  void setup() {
    OpenTelemetryConfiguration configuration =
        FileConfiguration.parse(
            new ByteArrayInputStream(extendedSchema.getBytes(StandardCharsets.UTF_8)));
    structuredConfigProps = FileConfiguration.toConfigProperties(configuration);
  }

  @Test
  void configurationSchema() {
    // Validate can read file configuration schema properties
    assertThat(structuredConfigProps.getString("file_format")).isEqualTo("0.1");
    StructuredConfigProperties resourceProps = structuredConfigProps.getStructured("resource");
    assertThat(resourceProps).isNotNull();
    StructuredConfigProperties resourceAttributesProps = resourceProps.getStructured("attributes");
    assertThat(resourceAttributesProps).isNotNull();
    assertThat(resourceAttributesProps.getString("service.name")).isEqualTo("unknown_service");
  }

  @Test
  void additionalProperties() {
    assertThat(structuredConfigProps.getPropertyKeys())
        .isEqualTo(ImmutableSet.of("file_format", "disabled", "resource", "other"));

    // Validate can read properties not part of configuration schema
    // .other
    StructuredConfigProperties otherProps = structuredConfigProps.getStructured("other");
    assertThat(otherProps).isNotNull();
    assertThat(otherProps.getPropertyKeys())
        .isEqualTo(
            ImmutableSet.of(
                "str_key",
                "int_key",
                "float_key",
                "bool_key",
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
    assertThat(otherProps.getBoolean("bool_key")).isTrue();
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
    StructuredConfigProperties otherMapKeyProps = otherProps.getStructured("map_key");
    assertThat(otherMapKeyProps).isNotNull();
    assertThat(otherMapKeyProps.getPropertyKeys())
        .isEqualTo(ImmutableSet.of("str_key1", "int_key1", "map_key1"));
    assertThat(otherMapKeyProps.getString("str_key1")).isEqualTo("str_value1");
    assertThat(otherMapKeyProps.getInt("int_key1")).isEqualTo(2);
    // other.map_key.map_key1
    StructuredConfigProperties otherMapKeyMapKey1Props = otherMapKeyProps.getStructured("map_key1");
    assertThat(otherMapKeyMapKey1Props).isNotNull();
    assertThat(otherMapKeyMapKey1Props.getPropertyKeys())
        .isEqualTo(ImmutableSet.of("str_key2", "int_key2"));
    assertThat(otherMapKeyMapKey1Props.getString("str_key2")).isEqualTo("str_value2");
    assertThat(otherMapKeyMapKey1Props.getInt("int_key2")).isEqualTo(3);

    // .other.list_key
    List<StructuredConfigProperties> listKey = otherProps.getStructuredList("list_key");
    assertThat(listKey).hasSize(2);
    StructuredConfigProperties listKeyProps1 = listKey.get(0);
    assertThat(listKeyProps1.getPropertyKeys())
        .isEqualTo(ImmutableSet.of("str_key1", "int_key1", "map_key1"));
    assertThat(listKeyProps1.getString("str_key1")).isEqualTo("str_value1");
    assertThat(listKeyProps1.getInt("int_key1")).isEqualTo(2);
    // .other.list_key[0]
    StructuredConfigProperties listKeyProps1MapKeyProps = listKeyProps1.getStructured("map_key1");
    assertThat(listKeyProps1MapKeyProps).isNotNull();
    assertThat(listKeyProps1MapKeyProps.getPropertyKeys())
        .isEqualTo(ImmutableSet.of("str_key2", "int_key2"));
    assertThat(listKeyProps1MapKeyProps.getString("str_key2")).isEqualTo("str_value2");
    assertThat(listKeyProps1MapKeyProps.getInt("int_key2")).isEqualTo(3);
    // .other.list_key[1]
    StructuredConfigProperties listKeyProps2 = listKey.get(1);
    assertThat(listKeyProps2.getPropertyKeys()).isEqualTo(ImmutableSet.of("str_key1", "int_key1"));
    assertThat(listKeyProps2.getString("str_key1")).isEqualTo("str_value1");
    assertThat(listKeyProps2.getInt("int_key1")).isEqualTo(2);
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
    StructuredConfigProperties otherProps = structuredConfigProps.getStructured("other");
    assertThat(otherProps).isNotNull();

    assertThat(otherProps.getString("int_key")).isNull();
    assertThat(otherProps.getInt("str_key")).isNull();
    assertThat(otherProps.getLong("str_key")).isNull();
    assertThat(otherProps.getDouble("str_key")).isNull();
    assertThat(otherProps.getBoolean("str_key")).isNull();
    assertThat(otherProps.getScalarList("str_key", String.class)).isNull();
    assertThat(otherProps.getStructured("str_key")).isNull();
    assertThat(otherProps.getStructuredList("str_key")).isNull();
  }
}
