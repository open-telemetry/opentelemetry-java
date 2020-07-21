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

/** This class contains the unified CSS styles for all zPages. */
final class ZPageStyle {
  private ZPageStyle() {}

  /** Style here will be applied to the generated HTML pages for all zPages. */
  static String style =
      "body{font-family: \"Roboto\", sans-serif; font-size: 16px;"
          + "background-color: #fff;}"
          + "h1{padding: 0 20px; color: #363636; text-align: center; margin-bottom: 20px;}"
          + "h2{padding: 0 20px; color: #363636; text-align: center; margin-bottom: 20px;}"
          + "p{padding: 0 20px; color: #363636;}"
          + "tr.bg-color{background-color: #4b5fab;}"
          + "table{margin: 0 auto;}"
          + "th{padding: 0 1em; line-height: 2.0}"
          + "td{padding: 0 1em; line-height: 2.0}"
          + ".border-right-white{border-right: 1px solid #fff;}"
          + ".border-left-white{border-left: 1px solid #fff;}"
          + ".border-left-dark{border-left: 1px solid #363636;}"
          + "th.header-text{color: #fff; line-height: 3.0;}"
          + ".align-center{text-align: center;}"
          + ".align-right{text-align: right;}"
          + "pre.no-margin{margin: 0;}"
          + "pre.wrap-text{white-space:pre-wrap;}"
          + "td.bg-white{background-color: #fff;}"
          + "button.button{background-color: #fff; margin-top: 15px;}"
          + "form.form-flex{display: flex; flex-direction: column; align-items: center;}";
}
