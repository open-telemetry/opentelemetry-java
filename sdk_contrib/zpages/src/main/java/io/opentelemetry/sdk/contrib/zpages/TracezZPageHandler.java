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

package io.opentelemetry.sdk.contrib.zpages;

import com.google.common.base.Charsets;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

final class TracezZPageHandler extends ZPageHandler {
  private static final String TRACEZ_URL = "/tracez";

  private TracezZPageHandler() {}

  /**
   * Constructs a new {@code TracezZPageHandler}.
   *
   * @return a new {@code TracezZPageHandler}.
   */
  static TracezZPageHandler create() {
    return new TracezZPageHandler();
  }

  @Override
  public String getUrlPath() {
    return TRACEZ_URL;
  }

  /**
   * Emits CSS Styles to the {@link PrintWriter} {@code out}. Content emited by this function should
   * be enclosed by <head></head> tag.
   *
   * @param out The {@link PrintWriter} {@code out}.
   */
  private static void emitHtmlStyle(PrintWriter out) {
    out.write("<style>");
    out.write(ZPageStyle.style);
    out.write("</style");
  }

  /**
   * Emits HTML body content to the {@link PrintWriter} {@code out}. Content emited by this function
   * should be enclosed by <body></body> tag.
   *
   * @param out The {@link PrintWriter} {@code out}.
   */
  private static void emitHtmlBody(PrintWriter out) {
    out.write("body");
  }

  @Override
  public void emitHtml(Map<String, String> queryMap, OutputStream outputStream) {
    // PrintWriter for emiting HTML contents
    PrintWriter out =
        new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, Charsets.UTF_8)));
    out.write("<!DOCTYPE html>");
    out.write("<html lang=\"en\">");
    out.write("<head>");
    out.write("<meta charset=\"UTF-8\">");
    out.write(
        "<link rel=\"shortcut icon\" href=\"https://opentelemetry.io/favicon.png\""
            + "type=\"image/png\">");
    out.write(
        "<link href=\"https://fonts.googleapis.com/css?family=Open+Sans:300\""
            + "rel=\"stylesheet\">");
    out.write(
        "<link href=\"https://fonts.googleapis.com/css?family=Roboto\"" + "rel=\"stylesheet\">");
    out.write("<title>TraceZ</title>");
    emitHtmlStyle(out);
    out.write("</head>");
    out.write("<body>");
    emitHtmlBody(out);
    out.write("</body>");
    out.write("</html>");
    out.close();
  }
}
