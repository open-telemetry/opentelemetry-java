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
abstract class TraceConfigzChangeTableRow {
  @AutoValue.Builder
  public abstract static class Builder {
    abstract TraceConfigzChangeTableRow build();

    /**
     * Set the print stream to emit HTML contents.
     *
     * @param out the {@link PrintStream} {@code out}.
     * @return the {@link Builder}.
     */
    abstract Builder setPrintStream(PrintStream out);

    /**
     * Set the display name of the parameter the row corresponds to.
     *
     * @param rowName the display name of the parameter the row corresponds to.
     * @return the {@link Builder}.
     */
    abstract Builder setRowName(String rowName);

    /**
     * Set the parameter name the row corresponds to.
     *
     * @param paramName the parameter name the row corresponds to (this will be used as URL query
     *     parameter, e.g. /traceconfigz?maxnumofattributes=30).
     * @return the {@link Builder}.
     */
    abstract Builder setParamName(String paramName);

    /**
     * Set the placeholder of the input element.
     *
     * @param inputPlaceholder the value of the placeholder.
     * @return the {@link Builder}.
     */
    abstract Builder setInputPlaceHolder(String inputPlaceholder);

    /**
     * Set the default value of the parameter the row corresponds to.
     *
     * @param defaultValue the default value of the corresponding parameter.
     * @return the {@link Builder}.
     */
    abstract Builder setParamDefaultValue(String defaultValue);

    /**
     * Set the background color for zebraStriping.
     *
     * @param zebraStripeColor the background color for zebraStriping.
     * @return the {@link Builder}.
     */
    abstract Builder setZebraStripeColor(String zebraStripeColor);

    /**
     * Set the boolean for zebraStriping the row.
     *
     * @param zebraStripe the boolean for zebraStriping the row.
     * @return the {@link Builder}.
     */
    abstract Builder setZebraStripe(boolean zebraStripe);

    Builder() {}
  }

  static Builder builder() {
    return new AutoValue_TraceConfigzChangeTableRow.Builder();
  }

  abstract PrintStream printStream();

  abstract String rowName();

  abstract String paramName();

  abstract String inputPlaceHolder();

  abstract String paramDefaultValue();

  abstract String zebraStripeColor();

  abstract boolean zebraStripe();

  /** Emit HTML content to the PrintStream. */
  public void emitHtml() {
    if (this.zebraStripe()) {
      this.printStream().print("<tr style=\"background-color: " + this.zebraStripeColor() + ";\">");
    } else {
      this.printStream().print("<tr>");
    }
    this.printStream().print("<td>Update " + this.rowName() + "</td>");
    this.printStream()
        .print(
            "<td class=\"border-left-dark\"><input type=text size=15 name="
                + this.paramName()
                + " value=\"\" placeholder=\""
                + this.inputPlaceHolder()
                + "\" /></td>");
    this.printStream()
        .print("<td class=\"border-left-dark\">(" + this.paramDefaultValue() + ")</td>");
    this.printStream().print("</tr>");
  }
}
