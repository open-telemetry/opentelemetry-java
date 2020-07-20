/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace;

import static com.google.common.truth.Truth.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import org.junit.Test;

public class StringBuilderPrintWriterTest {

  private static final char[] CHARS = "hello".toCharArray();
  private static final String STRING = "world";
  private static final Object OBJECT = new Object();

  @Test
  public void matchesJdk() {
    StringWriter jdkString = new StringWriter();
    fill(new PrintWriter(jdkString));

    StringBuilder stringBuilder = new StringBuilder();
    fill(new StringBuilderPrintWriter(stringBuilder));

    assertThat(stringBuilder.toString()).isEqualTo(jdkString.toString());
  }

  private static void fill(PrintWriter writer) {
    writer.write(10);
    writer.write(CHARS, 1, 2);
    writer.write(CHARS);
    writer.write(STRING, 2, 3);
    writer.write(STRING);
    writer.print(true);
    writer.print('c');
    writer.print(21);
    writer.print(30L);
    writer.print(1.0F);
    writer.print(2.0D);
    writer.print(CHARS);
    writer.print(STRING);
    writer.print(OBJECT);
    writer.println();
    writer.println(false);
    writer.println('d');
    writer.println(30);
    writer.println(40L);
    writer.println(3.0F);
    writer.println(4.0D);
    writer.println(CHARS);
    writer.println(STRING);
    writer.println(OBJECT);
    writer.printf("%d %s", 10, "foo");
    writer.printf(Locale.FRANCE, "e = %+10.4f", Math.E);
    writer.format("%d %s", 20, "bar");
    writer.format(Locale.FRANCE, "e = %+10.4f", Math.E);
    writer.append(STRING);
    writer.append(STRING, 2, 3);
    writer.append('e');

    writer.checkError();
    writer.flush();
    writer.close();
  }
}
