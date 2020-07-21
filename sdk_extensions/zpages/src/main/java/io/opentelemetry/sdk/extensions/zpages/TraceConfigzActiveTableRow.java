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

final class TraceConfigzActiveTableRow {
  private final PrintStream out;
  private final String paramName;
  private final String paramValue;
  private final String zebraStripeColor;
  private final boolean zebraStripe;

  private TraceConfigzActiveTableRow(Builder builder) {
    out = builder.out;
    paramName = builder.paramName;
    paramValue = builder.paramValue;
    zebraStripeColor = builder.zebraStripeColor;
    zebraStripe = builder.zebraStripe;
  }

  public static class Builder {
    private PrintStream out;
    private String paramName;
    private String paramValue;
    private String zebraStripeColor;
    private boolean zebraStripe;

    public TraceConfigzActiveTableRow build() {
      return new TraceConfigzActiveTableRow(this);
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
     * Set the parameter name the row corresponds to.
     *
     * @param paramName the parameter name the row corresponds to.
     * @return the {@link Builder}.
     */
    public Builder setParamName(String paramName) {
      this.paramName = paramName;
      return this;
    }

    /**
     * Set the parameter value the row corresponds to.
     *
     * @param paramValue the parameter value the row corresponds to.
     * @return the {@link Builder}.
     */
    public Builder setParamValue(String paramValue) {
      this.paramValue = paramValue;
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
    out.print("<td>" + paramName + "</td>");
    out.print("<td class=\"border-left-dark\">" + paramValue + "</td>");
    out.print("</tr>");
  }
}
