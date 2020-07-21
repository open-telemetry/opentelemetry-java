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

import java.io.PrintStream;

/** Builder pattern class for emiting a single row of the change parameter table. */
final class TraceConfigzChangeTableRow {
  private final PrintStream out;
  private final String rowName;
  private final String paramName;
  private final String inputPlaceholder;
  private final String defaultValue;
  private final String zebraStripeColor;
  private final boolean zebraStripe;

  private TraceConfigzChangeTableRow(Builder builder) {
    out = builder.out;
    rowName = builder.rowName;
    paramName = builder.paramName;
    inputPlaceholder = builder.inputPlaceholder;
    defaultValue = builder.defaultValue;
    zebraStripeColor = builder.zebraStripeColor;
    zebraStripe = builder.zebraStripe;
  }

  public static class Builder {
    private PrintStream out;
    private String rowName;
    private String paramName;
    private String inputPlaceholder = "";
    private String defaultValue;
    private String zebraStripeColor;
    private boolean zebraStripe;

    public TraceConfigzChangeTableRow build() {
      return new TraceConfigzChangeTableRow(this);
    }

    /**
     * Set the print stream to emit HTML contents.
     *
     * @param out the {@link PrintStream} {@code out}.
     * @return the {@link Builder}.
     */
    public Builder setPrintStream(PrintStream out) {
      this.out = out;
      return this;
    }

    /**
     * Set the display name of the parameter the row corresponds to.
     *
     * @param rowName the display name of the parameter the row corresponds to.
     * @return the {@link Builder}.
     */
    public Builder setRowName(String rowName) {
      this.rowName = rowName;
      return this;
    }

    /**
     * Set the parameter name the row corresponds to.
     *
     * @param paramName the parameter name the row corresponds to (this will be used as URL query
     *     parameter, e.g. /traceconfigz?maxnumofattributes=30).
     * @return the {@link Builder}.
     */
    public Builder setParamName(String paramName) {
      this.paramName = paramName;
      return this;
    }

    /**
     * Set the placeholder of the input element.
     *
     * @param inputPlaceholder the value of the placeholder.
     * @return the {@link Builder}.
     */
    public Builder setInputPlaceHolder(String inputPlaceholder) {
      this.inputPlaceholder = inputPlaceholder;
      return this;
    }

    /**
     * Set the default value of the parameter the row corresponds to.
     *
     * @param defaultValue the default value of the corresponding parameter.
     * @return the {@link Builder}.
     */
    public Builder setParamDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    /**
     * Set the background color for zebraStriping.
     *
     * @param zebraStripeColor the background color for zebraStriping.
     * @return the {@link Builder}.
     */
    public Builder setZebraStripeColor(String zebraStripeColor) {
      this.zebraStripeColor = zebraStripeColor;
      return this;
    }

    /**
     * Set the boolean for zebraStriping the row.
     *
     * @param zebraStripe the boolean for zebraStriping the row.
     * @return the {@link Builder}.
     */
    public Builder setZebraStripe(boolean zebraStripe) {
      this.zebraStripe = zebraStripe;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Emit HTML content to the PrintStream. */
  public void emitHtml() {
    if (zebraStripe) {
      out.print("<tr style=\"background-color: " + zebraStripeColor + ";\">");
    } else {
      out.print("<tr>");
    }
    out.print("<td>" + rowName + "</td>");
    out.print(
        "<td class=\"border-left-dark\"><input type=text size=15 name="
            + paramName
            + " value=\"\" placeholder=\""
            + inputPlaceholder
            + "\" /></td>");
    out.print("<td class=\"border-left-dark\">(" + defaultValue + ")</td>");
    out.print("</tr>");
  }
}
