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

import com.google.auto.value.AutoValue;
import java.io.PrintStream;

/** Builder pattern class for emiting a single row of the change parameter table. */
@AutoValue
abstract class TraceConfigzActiveTableRow {
  abstract PrintStream printStream();

  abstract String paramName();

  abstract String paramValue();

  abstract String zebraStripeColor();

  abstract boolean zebraStripe();

  /** Emit HTML content to the PrintStream. */
  public void emitHtml() {
    if (this.zebraStripe()) {
      this.printStream().print("<tr style=\"background-color: " + this.zebraStripeColor() + ";\">");
    } else {
      this.printStream().print("<tr>");
    }
    this.printStream().print("<td>" + this.paramName() + "</td>");
    this.printStream().print("<td class=\"border-left-dark\">" + this.paramValue() + "</td>");
    this.printStream().print("</tr>");
  }

  static Builder builder() {
    return new AutoValue_TraceConfigzActiveTableRow.Builder();
  }

  @AutoValue.Builder
  interface Builder {
    TraceConfigzActiveTableRow build();

    /**
     * Set the print stream to emit HTML contents.
     *
     * @param out the {@link PrintStream} {@code out}.
     * @return the {@link Builder}.
     */
    Builder setPrintStream(PrintStream out);

    /**
     * Set the parameter name the row corresponds to.
     *
     * @param paramName the parameter name the row corresponds to.
     * @return the {@link Builder}.
     */
    Builder setParamName(String paramName);

    /**
     * Set the parameter value the row corresponds to.
     *
     * @param paramValue the parameter value the row corresponds to.
     * @return the {@link Builder}.
     */
    Builder setParamValue(String paramValue);

    /**
     * Set the background color for zebraStriping.
     *
     * @param zebraStripeColor the background color for zebraStriping.
     * @return the {@link Builder}.
     */
    Builder setZebraStripeColor(String zebraStripeColor);

    /**
     * Set the boolean for zebraStriping the row.
     *
     * @param zebraStripe the boolean for zebraStriping the row.
     * @return the {@link Builder}.
     */
    Builder setZebraStripe(boolean zebraStripe);
  }
}
