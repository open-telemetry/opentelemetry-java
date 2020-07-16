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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class IndexZPageHandler extends ZPageHandler {
  private static final String INDEX_URL = "/";
  private static final String INDEX_NAME = "Index";
  private static final String INDEX_DESCRITION = "Index page of zPages";
  private static final Logger logger = Logger.getLogger(IndexZPageHandler.class.getName());
  @Nullable private final List<ZPageHandler> availableHandlers;

  IndexZPageHandler(@Nullable List<ZPageHandler> availableHandlers) {
    this.availableHandlers = availableHandlers;
  }

  @Override
  public String getUrlPath() {
    return INDEX_URL;
  }

  @Override
  public String getPageName() {
    return INDEX_NAME;
  }

  @Override
  public String getPageDescription() {
    return INDEX_DESCRITION;
  }

  private static void emitPageLinkAndInfo(PrintStream out, ZPageHandler handler) {
    out.print("<a href=\"" + handler.getUrlPath() + "\">");
    out.print("<h2 style=\"text-algin: left;\">" + handler.getPageName() + "</h2>");
    out.print("</a>");
    out.print("<p>" + handler.getPageDescription() + "</p>");
  }

  @Override
  public void emitHtml(Map<String, String> queryMap, OutputStream outputStream) {
    // PrintStream for emiting HTML contents
    try (PrintStream out = new PrintStream(outputStream, /* autoFlush= */ false, "UTF-8")) {
      out.print("<!DOCTYPE html>");
      out.print("<html lang=\"en\">");
      out.print("<head>");
      out.print("<meta charset=\"UTF-8\">");
      out.print(
          "<link rel=\"shortcut icon\" href=\"data:image/png;base64,"
              + ZPageLogo.getFaviconBase64()
              + "\" type=\"image/png\">");
      out.print(
          "<link href=\"https://fonts.googleapis.com/css?family=Open+Sans:300\""
              + "rel=\"stylesheet\">");
      out.print(
          "<link href=\"https://fonts.googleapis.com/css?family=Roboto\" rel=\"stylesheet\">");
      out.print("<title>zPages</title>");
      out.print("<style>");
      out.print(ZPageStyle.style);
      out.print("</style>");
      out.print("</head>");
      out.print("<body>");
      out.print(
          "<a href=\"/\"><img style=\"height: 90px;\" src=\"data:image/png;base64,"
              + ZPageLogo.getLogoBase64()
              + "\" /></a>");
      out.print("<h1 style=\"text-align: left;\">zPages</h1>");
      out.print(
          "<p>OpenTelemetry provides in-process web pages that display collected data from"
              + " the process that they are attached to. These are called \"zPages\"."
              + " They are useful for in-process diagnostics without having to depend on"
              + " any backend to examine traces or metrics.</p>");

      out.print(
          "<p>zPages can be useful during the development time or "
              + "when the process to be inspected is known in production.</p>");
      if (this.availableHandlers != null) {
        for (ZPageHandler handler : this.availableHandlers) {
          emitPageLinkAndInfo(out, handler);
        }
      }
      out.print("</body>");
      out.print("</html>");
    } catch (Throwable t) {
      logger.log(Level.WARNING, "error while generating HTML", t);
    }
  }
}
