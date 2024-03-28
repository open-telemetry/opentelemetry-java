/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static io.opentelemetry.exporter.internal.marshal.MarshalerUtil.getUtf8Size;
import static io.opentelemetry.exporter.internal.marshal.MarshalerUtil.writeUtf8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MarshalerUtilTest {

  @Test
  @SuppressWarnings("AvoidEscapedUnicodeCharacters")
  void encodeUtf8() {
    assertThat(getUtf8Size("")).isEqualTo(0);
    assertThat(testUtf8("")).isEqualTo("");

    assertThat(getUtf8Size("a")).isEqualTo(1);
    assertThat(testUtf8("a")).isEqualTo("a");

    assertThat(getUtf8Size("©")).isEqualTo(2);
    assertThat(testUtf8("©")).isEqualTo("©");

    assertThat(getUtf8Size("∆")).isEqualTo(3);
    assertThat(testUtf8("∆")).isEqualTo("∆");

    assertThat(getUtf8Size("😀")).isEqualTo(4);
    assertThat(testUtf8("😀")).isEqualTo("😀");

    // test that invalid characters are replaced with ?
    assertThat(getUtf8Size("\uD83D😀\uDE00")).isEqualTo(6);
    assertThat(testUtf8("\uD83D😀\uDE00")).isEqualTo("?😀?");
  }

  private static String testUtf8(String string) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
      writeUtf8(codedOutputStream, string);
      codedOutputStream.flush();
      return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    } catch (Exception exception) {
      throw new IllegalArgumentException(exception);
    }
  }
}
