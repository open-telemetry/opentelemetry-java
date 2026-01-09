/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.JRE;

class StringEncoderTest {

  private static final StringEncoder fallbackStringEncoder =
      StringEncoderHolder.createFallbackEncoder();
  private static final StringEncoder unsafeStringEncoder =
      StringEncoderHolder.createUnsafeEncoder();
  private static final StringEncoder varHandleStringEncoder =
      StringEncoderHolder.createVarHandleEncoder();

  @Test
  void testUtf8Encoding_Fallback() {
    testUtf8Encoding(fallbackStringEncoder);
  }

  @Test
  void testUtf8SizeLatin1_Fallback() {
    testUtf8SizeLatin1(fallbackStringEncoder);
  }

  @Test
  @DisabledOnJre(JRE.JAVA_8)
  void testUtf8Encoding_Unsafe() {
    testUtf8Encoding(unsafeStringEncoder);
  }

  @Test
  @DisabledOnJre(JRE.JAVA_8)
  void testUtf8SizeLatin1_Unsafe() {
    testUtf8SizeLatin1(unsafeStringEncoder);
  }

  @Test
  @DisabledOnJre(JRE.JAVA_8)
  void testUtf8Encoding_VarHandle() {
    testUtf8Encoding(varHandleStringEncoder);
  }

  @Test
  @DisabledOnJre(JRE.JAVA_8)
  void testUtf8SizeLatin1_VarHandle() {
    testUtf8SizeLatin1(varHandleStringEncoder);
  }

  @SuppressWarnings("AvoidEscapedUnicodeCharacters")
  private static void testUtf8Encoding(StringEncoder stringEncoder) {
    assertThat(stringEncoder).isNotNull();

    assertThat(stringEncoder.getUtf8Size("")).isEqualTo(0);
    assertThat(testUtf8("", 0, stringEncoder)).isEqualTo("");

    assertThat(stringEncoder.getUtf8Size("a")).isEqualTo(1);
    assertThat(testUtf8("a", 1, stringEncoder)).isEqualTo("a");

    assertThat(stringEncoder.getUtf8Size("©")).isEqualTo(2);
    assertThat(testUtf8("©", 2, stringEncoder)).isEqualTo("©");

    assertThat(stringEncoder.getUtf8Size("∆")).isEqualTo(3);
    assertThat(testUtf8("∆", 3, stringEncoder)).isEqualTo("∆");

    assertThat(stringEncoder.getUtf8Size("😀")).isEqualTo(4);
    assertThat(testUtf8("😀", 4, stringEncoder)).isEqualTo("😀");

    // test that invalid characters are replaced with ?
    assertThat(stringEncoder.getUtf8Size("\uD83D😀\uDE00")).isEqualTo(6);
    assertThat(testUtf8("\uD83D😀\uDE00", 6, stringEncoder)).isEqualTo("?😀?");

    // the same invalid sequence as encoded by the jdk
    byte[] bytes = "\uD83D😀\uDE00".getBytes(StandardCharsets.UTF_8);
    assertThat(bytes.length).isEqualTo(6);
    assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("?😀?");
  }

  private static void testUtf8SizeLatin1(StringEncoder stringEncoder) {
    // Run repeated test logic for each encoder
    Random random = new Random();
    for (int i = 0; i < 1000; i++) {
      byte[] bytes = new byte[15001];
      random.nextBytes(bytes);
      String string = new String(bytes, StandardCharsets.ISO_8859_1);
      int utf8Size = string.getBytes(StandardCharsets.UTF_8).length;
      assertThat(stringEncoder.getUtf8Size(string)).isEqualTo(utf8Size);
    }
  }

  static String testUtf8(String string, int utf8Length) {
    return testUtf8(string, utf8Length, StringEncoder.getInstance());
  }

  static String testUtf8(String string, int utf8Length, StringEncoder stringEncoder) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
      stringEncoder.writeUtf8(codedOutputStream, string, utf8Length);
      codedOutputStream.flush();
      return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    } catch (Exception exception) {
      throw new IllegalArgumentException(exception);
    }
  }
}
