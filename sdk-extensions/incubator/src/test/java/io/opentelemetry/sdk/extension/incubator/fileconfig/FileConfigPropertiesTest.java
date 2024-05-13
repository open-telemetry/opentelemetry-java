/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileConfigPropertiesTest {

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
          + "  long_key: 1\n"
          + "  float_key: 1.1\n"
          + "  bool_key: true\n"
          + "  str_list_key: [val1, val2]\n"
          + "  int_list_key: [1, 2]\n"
          + "  float_list_key: [1.1, 2.2]\n"
          + "  bool_list_key: [true, false]\n"
          + "  map_key:\n"
          + "    str_key1: str_value1\n"
          + "    long_key1: 2\n"
          + "    map_key1:\n"
          + "      str_key2: str_value2\n"
          + "      long_key2: 3\n"
          + "  list_key:\n"
          + "    - str_key1: str_value1\n"
          + "      long_key1: 2\n"
          + "      map_key1:\n"
          + "        str_key2: str_value2\n"
          + "        long_key2: 3\n"
          + "    - str_key1: str_value1\n"
          + "      long_key1: 2";

  private StructuredConfigProperties extendedConfigProps;

  @BeforeEach
  void setup() {
    OpenTelemetryConfiguration configuration =
        FileConfiguration.parse(
            new ByteArrayInputStream(extendedSchema.getBytes(StandardCharsets.UTF_8)));
    extendedConfigProps = FileConfiguration.toConfigProperties(configuration);
  }

  @Test
  void configurationSchema() {
    // Validate can read file configuration schema properties
    assertThat(extendedConfigProps.getString("file_format")).isEqualTo("0.1");
    StructuredConfigProperties resourceProps = extendedConfigProps.getStructured("resource");
    assertThat(resourceProps).isNotNull();
    StructuredConfigProperties resourceAttributesProps = resourceProps.getStructured("attributes");
    assertThat(resourceAttributesProps).isNotNull();
    assertThat(resourceAttributesProps.getString("service.name")).isEqualTo("unknown_service");
  }

  @Test
  void additionalProperties() {
    // Validate can read properties not part of configuration schema
    // .other
    StructuredConfigProperties otherProps = extendedConfigProps.getStructured("other");
    assertThat(otherProps).isNotNull();
    assertThat(otherProps.getString("str_key")).isEqualTo("str_value");
    assertThat(otherProps.getLong("long_key")).isEqualTo(1);
    assertThat(otherProps.getDouble("float_key")).isEqualTo(1.1);
    assertThat(otherProps.getPrimitiveList("str_list_key"))
        .isEqualTo(Arrays.asList("val1", "val2"));
    assertThat(otherProps.getPrimitiveList("int_list_key")).isEqualTo(Arrays.asList("1", "2"));
    assertThat(otherProps.getPrimitiveList("float_list_key"))
        .isEqualTo(Arrays.asList("1.1", "2.2"));
    assertThat(otherProps.getPrimitiveList("bool_list_key"))
        .isEqualTo(Arrays.asList("true", "false"));

    // .other.map_key
    StructuredConfigProperties otherMapKeyProps = otherProps.getStructured("map_key");
    assertThat(otherMapKeyProps).isNotNull();
    assertThat(otherMapKeyProps.getString("str_key1")).isEqualTo("str_value1");
    // other.map_key.map_key1
    StructuredConfigProperties otherMapKeyMapKey1Props = otherMapKeyProps.getStructured("map_key1");
    assertThat(otherMapKeyMapKey1Props).isNotNull();
    assertThat(otherMapKeyMapKey1Props.getString("str_key2")).isEqualTo("str_value2");

    // .other.list_key
    List<StructuredConfigProperties> listKey = otherProps.getStructuredList("list_key");
    assertThat(listKey).hasSize(2);
    StructuredConfigProperties listKeyProps1 = listKey.get(0);
    assertThat(listKeyProps1.getString("str_key1")).isEqualTo("str_value1");
    // .other.list_key[0]
    StructuredConfigProperties listKeyProps1MapKeyProps = listKeyProps1.getStructured("map_key1");
    assertThat(listKeyProps1MapKeyProps).isNotNull();
    assertThat(listKeyProps1MapKeyProps.getString("str_key2")).isEqualTo("str_value2");
    // .other.list_key[1]
    StructuredConfigProperties listKeyProps2 = listKey.get(0);
    assertThat(listKeyProps2.getString("str_key1")).isEqualTo("str_value1");
  }

  @Test
  void defaults() {
    assertThat(extendedConfigProps.getString("foo", "bar")).isEqualTo("bar");
    assertThat(extendedConfigProps.getLong("foo", 1)).isEqualTo(1);
    assertThat(extendedConfigProps.getDouble("foo", 1.1)).isEqualTo(1.1);
    assertThat(extendedConfigProps.getBoolean("foo", true)).isTrue();
    assertThat(extendedConfigProps.getPrimitiveList("foo", Collections.singletonList("bar")))
        .isEqualTo(Collections.singletonList("bar"));

    // Dot notation
    assertThat(extendedConfigProps.getString("foo.bar", "baz")).isEqualTo("baz");
    assertThat(extendedConfigProps.getLong("foo.bar", 1)).isEqualTo(1);
    assertThat(extendedConfigProps.getDouble("foo.bar", 1.1)).isEqualTo(1.1);
    assertThat(extendedConfigProps.getBoolean("foo.bar", true)).isTrue();
    assertThat(extendedConfigProps.getPrimitiveList("foo.bar", Collections.singletonList("baz")))
        .isEqualTo(Collections.singletonList("baz"));
  }

  @Test
  void missingKeys() {
    assertThat(extendedConfigProps.getString("foo")).isNull();
    assertThat(extendedConfigProps.getLong("foo")).isNull();
    assertThat(extendedConfigProps.getDouble("foo")).isNull();
    assertThat(extendedConfigProps.getBoolean("foo")).isNull();
    assertThat(extendedConfigProps.getPrimitiveList("foo")).isEmpty();
    assertThat(extendedConfigProps.getStructured("foo")).isNull();
    assertThat(extendedConfigProps.getStructuredList("foo")).isNull();

    // Dot notation
    assertThat(extendedConfigProps.getString("other.missing_key")).isNull();
    assertThat(extendedConfigProps.getString("other.map_key.missing_key")).isNull();
  }

  @Test
  void wrongType() {
    StructuredConfigProperties otherProps = extendedConfigProps.getStructured("other");
    assertThat(otherProps).isNotNull();

    assertThat(otherProps.getString("long_key")).isNull();
    assertThat(otherProps.getLong("str_key")).isNull();
    assertThat(otherProps.getDouble("str_key")).isNull();
    assertThat(otherProps.getBoolean("str_key")).isNull();
    assertThat(otherProps.getPrimitiveList("str_key")).isEmpty();
    assertThat(otherProps.getStructured("str_key")).isNull();
    assertThat(otherProps.getStructuredList("str_key")).isNull();

    // Dot notation
    assertThat(extendedConfigProps.getString("other.long_key")).isNull();
    assertThat(extendedConfigProps.getLong("other.str_key")).isNull();
    assertThat(extendedConfigProps.getDouble("other.str_key")).isNull();
    assertThat(extendedConfigProps.getBoolean("other.str_key")).isNull();
    assertThat(extendedConfigProps.getPrimitiveList("other.str_key")).isEmpty();
    assertThat(extendedConfigProps.getStructured("other.str_key")).isNull();
    assertThat(extendedConfigProps.getStructuredList("other.str_key")).isNull();
  }
}
