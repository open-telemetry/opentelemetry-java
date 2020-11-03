/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
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
