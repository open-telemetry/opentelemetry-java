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

package io.opentelemetry.sdk.extensions.zpages;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceConfigzChangeTableRow}. */
@RunWith(JUnit4.class)
public final class TraceConfigzChangeTableRowTest {
  @Test
  public void emitsHtmlCorrectly() {
    OutputStream output = new ByteArrayOutputStream();
    String rowName = "TestRow";
    String paramName = "testparam";
    String defaultValue = "1.0";
    String zebraStripeColor = "#363636";
    TraceConfigzChangeTableRow.builder()
        .setPrintStream(new PrintStream(output))
        .setRowName(rowName)
        .setParamName(paramName)
        .setInputPlaceHolder("")
        .setParamDefaultValue(defaultValue)
        .setZebraStripeColor(zebraStripeColor)
        .setZebraStripe(true)
        .build()
        .emitHtml();

    assertThat(output.toString()).contains(">" + rowName + "<");
    assertThat(output.toString()).contains("name=" + paramName);
    assertThat(output.toString()).contains(">(" + defaultValue + ")<");
    assertThat(output.toString()).contains("background-color: " + zebraStripeColor);
  }
}
