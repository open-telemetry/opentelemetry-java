/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "general", "cpp", "dotnet", "erlang", "go", "java", "js", "php", "python", "ruby", "rust", "swift"
})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalInstrumentationModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("general")
  private ExperimentalGeneralInstrumentationModel general;

  /** (Can be null) */
  @Nullable
  @JsonProperty("cpp")
  private ExperimentalLanguageSpecificInstrumentationModel cpp;

  /** (Can be null) */
  @Nullable
  @JsonProperty("dotnet")
  private ExperimentalLanguageSpecificInstrumentationModel dotnet;

  /** (Can be null) */
  @Nullable
  @JsonProperty("erlang")
  private ExperimentalLanguageSpecificInstrumentationModel erlang;

  /** (Can be null) */
  @Nullable
  @JsonProperty("go")
  private ExperimentalLanguageSpecificInstrumentationModel go;

  /** (Can be null) */
  @Nullable
  @JsonProperty("java")
  private ExperimentalLanguageSpecificInstrumentationModel java;

  /** (Can be null) */
  @Nullable
  @JsonProperty("js")
  private ExperimentalLanguageSpecificInstrumentationModel js;

  /** (Can be null) */
  @Nullable
  @JsonProperty("php")
  private ExperimentalLanguageSpecificInstrumentationModel php;

  /** (Can be null) */
  @Nullable
  @JsonProperty("python")
  private ExperimentalLanguageSpecificInstrumentationModel python;

  /** (Can be null) */
  @Nullable
  @JsonProperty("ruby")
  private ExperimentalLanguageSpecificInstrumentationModel ruby;

  /** (Can be null) */
  @Nullable
  @JsonProperty("rust")
  private ExperimentalLanguageSpecificInstrumentationModel rust;

  /** (Can be null) */
  @Nullable
  @JsonProperty("swift")
  private ExperimentalLanguageSpecificInstrumentationModel swift;

  @JsonProperty("general")
  @Nullable
  public ExperimentalGeneralInstrumentationModel getGeneral() {
    return general;
  }

  public ExperimentalInstrumentationModel withGeneral(
      ExperimentalGeneralInstrumentationModel general) {
    this.general = general;
    return this;
  }

  @JsonProperty("cpp")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getCpp() {
    return cpp;
  }

  public ExperimentalInstrumentationModel withCpp(
      ExperimentalLanguageSpecificInstrumentationModel cpp) {
    this.cpp = cpp;
    return this;
  }

  @JsonProperty("dotnet")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getDotnet() {
    return dotnet;
  }

  public ExperimentalInstrumentationModel withDotnet(
      ExperimentalLanguageSpecificInstrumentationModel dotnet) {
    this.dotnet = dotnet;
    return this;
  }

  @JsonProperty("erlang")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getErlang() {
    return erlang;
  }

  public ExperimentalInstrumentationModel withErlang(
      ExperimentalLanguageSpecificInstrumentationModel erlang) {
    this.erlang = erlang;
    return this;
  }

  @JsonProperty("go")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getGo() {
    return go;
  }

  public ExperimentalInstrumentationModel withGo(
      ExperimentalLanguageSpecificInstrumentationModel go) {
    this.go = go;
    return this;
  }

  @JsonProperty("java")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getJava() {
    return java;
  }

  public ExperimentalInstrumentationModel withJava(
      ExperimentalLanguageSpecificInstrumentationModel java) {
    this.java = java;
    return this;
  }

  @JsonProperty("js")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getJs() {
    return js;
  }

  public ExperimentalInstrumentationModel withJs(
      ExperimentalLanguageSpecificInstrumentationModel js) {
    this.js = js;
    return this;
  }

  @JsonProperty("php")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getPhp() {
    return php;
  }

  public ExperimentalInstrumentationModel withPhp(
      ExperimentalLanguageSpecificInstrumentationModel php) {
    this.php = php;
    return this;
  }

  @JsonProperty("python")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getPython() {
    return python;
  }

  public ExperimentalInstrumentationModel withPython(
      ExperimentalLanguageSpecificInstrumentationModel python) {
    this.python = python;
    return this;
  }

  @JsonProperty("ruby")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getRuby() {
    return ruby;
  }

  public ExperimentalInstrumentationModel withRuby(
      ExperimentalLanguageSpecificInstrumentationModel ruby) {
    this.ruby = ruby;
    return this;
  }

  @JsonProperty("rust")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getRust() {
    return rust;
  }

  public ExperimentalInstrumentationModel withRust(
      ExperimentalLanguageSpecificInstrumentationModel rust) {
    this.rust = rust;
    return this;
  }

  @JsonProperty("swift")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getSwift() {
    return swift;
  }

  public ExperimentalInstrumentationModel withSwift(
      ExperimentalLanguageSpecificInstrumentationModel swift) {
    this.swift = swift;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalInstrumentationModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("general");
    sb.append('=');
    sb.append(((this.general == null) ? "<null>" : this.general));
    sb.append(',');
    sb.append("cpp");
    sb.append('=');
    sb.append(((this.cpp == null) ? "<null>" : this.cpp));
    sb.append(',');
    sb.append("dotnet");
    sb.append('=');
    sb.append(((this.dotnet == null) ? "<null>" : this.dotnet));
    sb.append(',');
    sb.append("erlang");
    sb.append('=');
    sb.append(((this.erlang == null) ? "<null>" : this.erlang));
    sb.append(',');
    sb.append("go");
    sb.append('=');
    sb.append(((this.go == null) ? "<null>" : this.go));
    sb.append(',');
    sb.append("java");
    sb.append('=');
    sb.append(((this.java == null) ? "<null>" : this.java));
    sb.append(',');
    sb.append("js");
    sb.append('=');
    sb.append(((this.js == null) ? "<null>" : this.js));
    sb.append(',');
    sb.append("php");
    sb.append('=');
    sb.append(((this.php == null) ? "<null>" : this.php));
    sb.append(',');
    sb.append("python");
    sb.append('=');
    sb.append(((this.python == null) ? "<null>" : this.python));
    sb.append(',');
    sb.append("ruby");
    sb.append('=');
    sb.append(((this.ruby == null) ? "<null>" : this.ruby));
    sb.append(',');
    sb.append("rust");
    sb.append('=');
    sb.append(((this.rust == null) ? "<null>" : this.rust));
    sb.append(',');
    sb.append("swift");
    sb.append('=');
    sb.append(((this.swift == null) ? "<null>" : this.swift));
    sb.append(',');
    if (sb.charAt((sb.length() - 1)) == ',') {
      sb.setCharAt((sb.length() - 1), ']');
    } else {
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = ((result * 31) + ((this.general == null) ? 0 : this.general.hashCode()));
    result = ((result * 31) + ((this.rust == null) ? 0 : this.rust.hashCode()));
    result = ((result * 31) + ((this.cpp == null) ? 0 : this.cpp.hashCode()));
    result = ((result * 31) + ((this.python == null) ? 0 : this.python.hashCode()));
    result = ((result * 31) + ((this.dotnet == null) ? 0 : this.dotnet.hashCode()));
    result = ((result * 31) + ((this.java == null) ? 0 : this.java.hashCode()));
    result = ((result * 31) + ((this.go == null) ? 0 : this.go.hashCode()));
    result = ((result * 31) + ((this.erlang == null) ? 0 : this.erlang.hashCode()));
    result = ((result * 31) + ((this.js == null) ? 0 : this.js.hashCode()));
    result = ((result * 31) + ((this.php == null) ? 0 : this.php.hashCode()));
    result = ((result * 31) + ((this.ruby == null) ? 0 : this.ruby.hashCode()));
    result = ((result * 31) + ((this.swift == null) ? 0 : this.swift.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalInstrumentationModel) == false) {
      return false;
    }
    ExperimentalInstrumentationModel rhs = ((ExperimentalInstrumentationModel) other);
    return (((((((((((((this.general == rhs.general)
                                                    || ((this.general != null)
                                                        && this.general.equals(rhs.general)))
                                                && ((this.rust == rhs.rust)
                                                    || ((this.rust != null)
                                                        && this.rust.equals(rhs.rust))))
                                            && ((this.cpp == rhs.cpp)
                                                || ((this.cpp != null)
                                                    && this.cpp.equals(rhs.cpp))))
                                        && ((this.python == rhs.python)
                                            || ((this.python != null)
                                                && this.python.equals(rhs.python))))
                                    && ((this.dotnet == rhs.dotnet)
                                        || ((this.dotnet != null)
                                            && this.dotnet.equals(rhs.dotnet))))
                                && ((this.java == rhs.java)
                                    || ((this.java != null) && this.java.equals(rhs.java))))
                            && ((this.go == rhs.go)
                                || ((this.go != null) && this.go.equals(rhs.go))))
                        && ((this.erlang == rhs.erlang)
                            || ((this.erlang != null) && this.erlang.equals(rhs.erlang))))
                    && ((this.js == rhs.js) || ((this.js != null) && this.js.equals(rhs.js))))
                && ((this.php == rhs.php) || ((this.php != null) && this.php.equals(rhs.php))))
            && ((this.ruby == rhs.ruby) || ((this.ruby != null) && this.ruby.equals(rhs.ruby))))
        && ((this.swift == rhs.swift) || ((this.swift != null) && this.swift.equals(rhs.swift))));
  }
}
