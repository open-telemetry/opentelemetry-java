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
import org.junit.jupiter.api.Test;

class StatelessMarshalerUtilTest {

  @Test
  @SuppressWarnings("AvoidEscapedUnicodeCharacters")
  void encodeUtf8() {
    assertThat(getUtf8Size("")).isEqualTo(0);
    assertThat(testUtf8("", 0)).isEqualTo("");

    assertThat(getUtf8Size("a")).isEqualTo(1);
    assertThat(testUtf8("a", 1)).isEqualTo("a");

    assertThat(getUtf8Size("©")).isEqualTo(2);
    assertThat(testUtf8("©", 2)).isEqualTo("©");

    assertThat(getUtf8Size("∆")).isEqualTo(3);
    assertThat(testUtf8("∆", 3)).isEqualTo("∆");

    assertThat(getUtf8Size("😀")).isEqualTo(4);
    assertThat(testUtf8("😀", 4)).isEqualTo("😀");

    // test that invalid characters are replaced with ?
    assertThat(getUtf8Size("\uD83D😀\uDE00")).isEqualTo(6);
    assertThat(testUtf8("\uD83D😀\uDE00", 6)).isEqualTo("?😀?");

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
    assertThat(getUtf8Size(string)).isEqualTo(utf8Size);
  }

  private static String testUtf8(String string, int utf8Length) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
      writeUtf8(codedOutputStream, string, utf8Length);
      codedOutputStream.flush();
      return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    } catch (Exception exception) {
      throw new IllegalArgumentException(exception);
    }
  }
}
