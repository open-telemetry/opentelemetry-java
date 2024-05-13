/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.spi.internal.ExtendedConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
          + "    service.name: !!str \"unknown_service\"\n"
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

  private ExtendedConfigProperties extendedConfigProps;

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
    ExtendedConfigProperties resourceProps = extendedConfigProps.getConfigProperties("resource");
    assertThat(resourceProps).isNotNull();
    ExtendedConfigProperties resourceAttributesProps =
        resourceProps.getConfigProperties("attributes");
    assertThat(resourceAttributesProps).isNotNull();
    assertThat(resourceAttributesProps.getString("service.name")).isEqualTo("unknown_service");
  }

  @Test
  void additionalProperties() {
    // Validate can read properties not part of configuration schema
    // .other
    ExtendedConfigProperties otherProps = extendedConfigProps.getConfigProperties("other");
    assertThat(otherProps).isNotNull();
    assertThat(otherProps.getString("str_key")).isEqualTo("str_value");
    assertThat(otherProps.getInt("int_key")).isEqualTo(1);
    assertThat(otherProps.getLong("int_key")).isEqualTo(1);
    assertThat(otherProps.getDouble("float_key")).isEqualTo(1.1);
    assertThat(otherProps.getDuration("int_key")).isEqualTo(Duration.ofMillis(1));
    assertThat(otherProps.getList("str_list_key")).isEqualTo(Arrays.asList("val1", "val2"));
    assertThat(otherProps.getList("int_list_key")).isEqualTo(Arrays.asList("1", "2"));
    assertThat(otherProps.getList("float_list_key")).isEqualTo(Arrays.asList("1.1", "2.2"));
    assertThat(otherProps.getList("bool_list_key")).isEqualTo(Arrays.asList("true", "false"));

    // .other.map_key
    ExtendedConfigProperties otherMapKeyProps = otherProps.getConfigProperties("map_key");
    assertThat(otherMapKeyProps).isNotNull();
    assertThat(otherMapKeyProps.getString("str_key1")).isEqualTo("str_value1");
    assertThat(otherMapKeyProps.getInt("int_key1")).isEqualTo(2);
    // other.map_key.map_key1
    ExtendedConfigProperties otherMapKeyMapKey1Props =
        otherMapKeyProps.getConfigProperties("map_key1");
    assertThat(otherMapKeyMapKey1Props).isNotNull();
    assertThat(otherMapKeyMapKey1Props.getString("str_key2")).isEqualTo("str_value2");
    assertThat(otherMapKeyMapKey1Props.getInt("int_key2")).isEqualTo(3);

    // .other.list_key
    List<ExtendedConfigProperties> listKey = otherProps.getListConfigProperties("list_key");
    assertThat(listKey).hasSize(2);
    ExtendedConfigProperties listKeyProps1 = listKey.get(0);
    assertThat(listKeyProps1.getString("str_key1")).isEqualTo("str_value1");
    assertThat(listKeyProps1.getInt("int_key1")).isEqualTo(2);
    // .other.list_key[0]
    ExtendedConfigProperties listKeyProps1MapKeyProps =
        listKeyProps1.getConfigProperties("map_key1");
    assertThat(listKeyProps1MapKeyProps).isNotNull();
    assertThat(listKeyProps1MapKeyProps.getString("str_key2")).isEqualTo("str_value2");
    assertThat(listKeyProps1MapKeyProps.getInt("int_key2")).isEqualTo(3);
    // .other.list_key[1]
    ExtendedConfigProperties listKeyProps2 = listKey.get(0);
    assertThat(listKeyProps2.getString("str_key1")).isEqualTo("str_value1");
    assertThat(listKeyProps2.getInt("int_key1")).isEqualTo(2);
  }

  @Test
  void dotNotation() {
    assertThat(extendedConfigProps.getString("other.str_key")).isEqualTo("str_value");
    assertThat(extendedConfigProps.getInt("other.int_key")).isEqualTo(1);
    assertThat(extendedConfigProps.getDouble("other.float_key")).isEqualTo(1.1);
    assertThat(extendedConfigProps.getBoolean("other.bool_key")).isEqualTo(true);
    assertThat(extendedConfigProps.getDuration("other.int_key")).isEqualTo(Duration.ofMillis(1));
    assertThat(extendedConfigProps.getList("other.str_list_key"))
        .isEqualTo(Arrays.asList("val1", "val2"));
    assertThat(extendedConfigProps.getList("other.int_list_key"))
        .isEqualTo(Arrays.asList("1", "2"));
    assertThat(extendedConfigProps.getList("other.float_list_key"))
        .isEqualTo(Arrays.asList("1.1", "2.2"));
    assertThat(extendedConfigProps.getList("other.bool_list_key"))
        .isEqualTo(Arrays.asList("true", "false"));
    assertThat(extendedConfigProps.getString("other.map_key.str_key1")).isEqualTo("str_value1");
    assertThat(extendedConfigProps.getInt("other.map_key.int_key1")).isEqualTo(2);
    assertThat(extendedConfigProps.getString("other.map_key.map_key1.str_key2"))
        .isEqualTo("str_value2");
    assertThat(extendedConfigProps.getInt("other.map_key.map_key1.int_key2")).isEqualTo(3);
    List<ExtendedConfigProperties> fileConfigProperties =
        extendedConfigProps.getListConfigProperties("other.list_key");
    assertThat(fileConfigProperties).isNotNull().hasSize(2);
    ExtendedConfigProperties otherMapKeyProps =
        extendedConfigProps.getConfigProperties("other.map_key");
    assertThat(otherMapKeyProps).isNotNull();
    assertThat(otherMapKeyProps.getString("str_key1")).isEqualTo("str_value1");
    assertThat(otherMapKeyProps.getInt("int_key1")).isEqualTo(2);
    assertThat(otherMapKeyProps.getConfigProperties("map_key1")).isNotNull();
  }

  @Test
  void defaults() {
    assertThat(extendedConfigProps.getString("foo", "bar")).isEqualTo("bar");
    assertThat(extendedConfigProps.getInt("foo", 1)).isEqualTo(1);
    assertThat(extendedConfigProps.getLong("foo", 1)).isEqualTo(1);
    assertThat(extendedConfigProps.getDouble("foo", 1.1)).isEqualTo(1.1);
    assertThat(extendedConfigProps.getBoolean("foo", true)).isTrue();
    assertThat(extendedConfigProps.getDuration("foo", Duration.ofMillis(1)))
        .isEqualTo(Duration.ofMillis(1));
    assertThat(extendedConfigProps.getList("foo", Collections.singletonList("bar")))
        .isEqualTo(Collections.singletonList("bar"));

    // Dot notation
    assertThat(extendedConfigProps.getString("foo.bar", "baz")).isEqualTo("baz");
    assertThat(extendedConfigProps.getInt("foo.bar", 1)).isEqualTo(1);
    assertThat(extendedConfigProps.getLong("foo.bar", 1)).isEqualTo(1);
    assertThat(extendedConfigProps.getDouble("foo.bar", 1.1)).isEqualTo(1.1);
    assertThat(extendedConfigProps.getBoolean("foo.bar", true)).isTrue();
    assertThat(extendedConfigProps.getDuration("foo.bar", Duration.ofMillis(1)))
        .isEqualTo(Duration.ofMillis(1));
    assertThat(extendedConfigProps.getList("foo.bar", Collections.singletonList("baz")))
        .isEqualTo(Collections.singletonList("baz"));
  }

  @Test
  void missingKeys() {
    assertThat(extendedConfigProps.getString("foo")).isNull();
    assertThat(extendedConfigProps.getInt("foo")).isNull();
    assertThat(extendedConfigProps.getLong("foo")).isNull();
    assertThat(extendedConfigProps.getDouble("foo")).isNull();
    assertThat(extendedConfigProps.getBoolean("foo")).isNull();
    assertThat(extendedConfigProps.getDuration("foo")).isNull();
    assertThat(extendedConfigProps.getList("foo")).isEmpty();
    assertThat(extendedConfigProps.getConfigProperties("foo")).isNull();
    assertThat(extendedConfigProps.getListConfigProperties("foo")).isNull();

    // Dot notation
    assertThat(extendedConfigProps.getString("other.missing_key")).isNull();
    assertThat(extendedConfigProps.getString("other.map_key.missing_key")).isNull();
  }

  @Test
  void wrongType() {
    ExtendedConfigProperties otherProps = extendedConfigProps.getConfigProperties("other");
    assertThat(otherProps).isNotNull();

    assertThat(otherProps.getString("int_key")).isNull();
    assertThat(otherProps.getInt("str_key")).isNull();
    assertThat(otherProps.getLong("str_key")).isNull();
    assertThat(otherProps.getDouble("str_key")).isNull();
    assertThat(otherProps.getBoolean("str_key")).isNull();
    assertThat(otherProps.getList("str_key")).isEmpty();
    assertThat(otherProps.getConfigProperties("str_key")).isNull();
    assertThat(otherProps.getListConfigProperties("str_key")).isNull();

    // Dot notation
    assertThat(extendedConfigProps.getString("other.int_key")).isNull();
    assertThat(extendedConfigProps.getInt("other.str_key")).isNull();
    assertThat(extendedConfigProps.getLong("other.str_key")).isNull();
    assertThat(extendedConfigProps.getDouble("other.str_key")).isNull();
    assertThat(extendedConfigProps.getBoolean("other.str_key")).isNull();
    assertThat(extendedConfigProps.getList("other.str_key")).isEmpty();
    assertThat(extendedConfigProps.getConfigProperties("other.str_key")).isNull();
    assertThat(extendedConfigProps.getListConfigProperties("other.str_key")).isNull();
  }

  @Test
  void unsupportedOperations() {
    assertThat(extendedConfigProps.getMap("foo")).isEqualTo(Collections.emptyMap());
    assertThat(extendedConfigProps.getMap("foo", Collections.emptyMap()))
        .isEqualTo(Collections.emptyMap());
  }
}
