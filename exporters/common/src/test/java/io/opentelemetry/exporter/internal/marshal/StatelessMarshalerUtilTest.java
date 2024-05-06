/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil.getUtf8Size;
import static io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil.writeUtf8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StatelessMarshalerUtilTest {

  @ParameterizedTest
  @ValueSource(strings = {"true", "false"})
  @SuppressWarnings("AvoidEscapedUnicodeCharacters")
  void encodeUtf8(boolean useUnsafe) {
    assertThat(getUtf8Size("", useUnsafe)).isEqualTo(0);
    assertThat(testUtf8("", 0, useUnsafe)).isEqualTo("");

    assertThat(getUtf8Size("a", useUnsafe)).isEqualTo(1);
    assertThat(testUtf8("a", 1, useUnsafe)).isEqualTo("a");

    assertThat(getUtf8Size("©", useUnsafe)).isEqualTo(2);
    assertThat(testUtf8("©", 2, useUnsafe)).isEqualTo("©");

    assertThat(getUtf8Size("∆", useUnsafe)).isEqualTo(3);
    assertThat(testUtf8("∆", 3, useUnsafe)).isEqualTo("∆");

    assertThat(getUtf8Size("😀", useUnsafe)).isEqualTo(4);
    assertThat(testUtf8("😀", 4, useUnsafe)).isEqualTo("😀");

    // test that invalid characters are replaced with ?
    assertThat(getUtf8Size("\uD83D😀\uDE00", useUnsafe)).isEqualTo(6);
    assertThat(testUtf8("\uD83D😀\uDE00", 6, useUnsafe)).isEqualTo("?😀?");

    // the same invalid sequence as encoded by the jdk
    byte[] bytes = "\uD83D😀\uDE00".getBytes(StandardCharsets.UTF_8);
    assertThat(bytes.length).isEqualTo(6);
    assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("?😀?");
  }

  @RepeatedTest(1000)
  void testUtf8SizeLatin1() {
    Random random = new Random();
    byte[] bytes = new byte[15001];
    random.nextBytes(bytes);
    String string = new String(bytes, StandardCharsets.ISO_8859_1);
    int utf8Size = string.getBytes(StandardCharsets.UTF_8).length;
    assertThat(getUtf8Size(string, true)).isEqualTo(utf8Size);
  }

  private static String testUtf8(String string, int utf8Length, boolean useUnsafe) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
      writeUtf8(codedOutputStream, string, utf8Length, useUnsafe);
      codedOutputStream.flush();
      return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    } catch (Exception exception) {
      throw new IllegalArgumentException(exception);
    }
  }
}
