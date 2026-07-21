/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "general", "cpp", "dotnet", "erlang", "go", "java", "js", "php", "python", "ruby", "rust", "swift"
})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalInstrumentationModel {

  @Nullable private ExperimentalGeneralInstrumentationModel general;
  @Nullable private ExperimentalLanguageSpecificInstrumentationModel cpp;
  @Nullable private ExperimentalLanguageSpecificInstrumentationModel dotnet;
  @Nullable private ExperimentalLanguageSpecificInstrumentationModel erlang;
  @Nullable private ExperimentalLanguageSpecificInstrumentationModel go;
  @Nullable private ExperimentalLanguageSpecificInstrumentationModel java;
  @Nullable private ExperimentalLanguageSpecificInstrumentationModel js;
  @Nullable private ExperimentalLanguageSpecificInstrumentationModel php;
  @Nullable private ExperimentalLanguageSpecificInstrumentationModel python;
  @Nullable private ExperimentalLanguageSpecificInstrumentationModel ruby;
  @Nullable private ExperimentalLanguageSpecificInstrumentationModel rust;
  @Nullable private ExperimentalLanguageSpecificInstrumentationModel swift;

  /**
   * Configure general SemConv options that may apply to multiple languages and instrumentations.
   *
   * <p>Instrumenation may merge general config options with the language specific configuration at
   * .instrumentation.<language>.
   *
   * <p>If omitted, default values as described in ExperimentalGeneralInstrumentation are used.
   */
  @JsonProperty("general")
  @Nullable
  public ExperimentalGeneralInstrumentationModel getGeneral() {
    return general;
  }

  @JsonProperty("general")
  public ExperimentalInstrumentationModel withGeneral(
      ExperimentalGeneralInstrumentationModel general) {
    this.general = general;
    return this;
  }

  /**
   * Configure C++ language-specific instrumentation libraries.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("cpp")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getCpp() {
    return cpp;
  }

  @JsonProperty("cpp")
  public ExperimentalInstrumentationModel withCpp(
      ExperimentalLanguageSpecificInstrumentationModel cpp) {
    this.cpp = cpp;
    return this;
  }

  /**
   * Configure .NET language-specific instrumentation libraries.
   *
   * <p>Each entry's key identifies a particular instrumentation library. The corresponding value
   * configures it.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("dotnet")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getDotnet() {
    return dotnet;
  }

  @JsonProperty("dotnet")
  public ExperimentalInstrumentationModel withDotnet(
      ExperimentalLanguageSpecificInstrumentationModel dotnet) {
    this.dotnet = dotnet;
    return this;
  }

  /**
   * Configure Erlang language-specific instrumentation libraries.
   *
   * <p>Each entry's key identifies a particular instrumentation library. The corresponding value
   * configures it.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("erlang")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getErlang() {
    return erlang;
  }

  @JsonProperty("erlang")
  public ExperimentalInstrumentationModel withErlang(
      ExperimentalLanguageSpecificInstrumentationModel erlang) {
    this.erlang = erlang;
    return this;
  }

  /**
   * Configure Go language-specific instrumentation libraries.
   *
   * <p>Each entry's key identifies a particular instrumentation library. The corresponding value
   * configures it.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("go")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getGo() {
    return go;
  }

  @JsonProperty("go")
  public ExperimentalInstrumentationModel withGo(
      ExperimentalLanguageSpecificInstrumentationModel go) {
    this.go = go;
    return this;
  }

  /**
   * Configure Java language-specific instrumentation libraries.
   *
   * <p>Each entry's key identifies a particular instrumentation library. The corresponding value
   * configures it.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("java")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getJava() {
    return java;
  }

  @JsonProperty("java")
  public ExperimentalInstrumentationModel withJava(
      ExperimentalLanguageSpecificInstrumentationModel java) {
    this.java = java;
    return this;
  }

  /**
   * Configure JavaScript language-specific instrumentation libraries.
   *
   * <p>Each entry's key identifies a particular instrumentation library. The corresponding value
   * configures it.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("js")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getJs() {
    return js;
  }

  @JsonProperty("js")
  public ExperimentalInstrumentationModel withJs(
      ExperimentalLanguageSpecificInstrumentationModel js) {
    this.js = js;
    return this;
  }

  /**
   * Configure PHP language-specific instrumentation libraries.
   *
   * <p>Each entry's key identifies a particular instrumentation library. The corresponding value
   * configures it.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("php")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getPhp() {
    return php;
  }

  @JsonProperty("php")
  public ExperimentalInstrumentationModel withPhp(
      ExperimentalLanguageSpecificInstrumentationModel php) {
    this.php = php;
    return this;
  }

  /**
   * Configure Python language-specific instrumentation libraries.
   *
   * <p>Each entry's key identifies a particular instrumentation library. The corresponding value
   * configures it.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("python")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getPython() {
    return python;
  }

  @JsonProperty("python")
  public ExperimentalInstrumentationModel withPython(
      ExperimentalLanguageSpecificInstrumentationModel python) {
    this.python = python;
    return this;
  }

  /**
   * Configure Ruby language-specific instrumentation libraries.
   *
   * <p>Each entry's key identifies a particular instrumentation library. The corresponding value
   * configures it.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("ruby")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getRuby() {
    return ruby;
  }

  @JsonProperty("ruby")
  public ExperimentalInstrumentationModel withRuby(
      ExperimentalLanguageSpecificInstrumentationModel ruby) {
    this.ruby = ruby;
    return this;
  }

  /**
   * Configure Rust language-specific instrumentation libraries.
   *
   * <p>Each entry's key identifies a particular instrumentation library. The corresponding value
   * configures it.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("rust")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getRust() {
    return rust;
  }

  @JsonProperty("rust")
  public ExperimentalInstrumentationModel withRust(
      ExperimentalLanguageSpecificInstrumentationModel rust) {
    this.rust = rust;
    return this;
  }

  /**
   * Configure Swift language-specific instrumentation libraries.
   *
   * <p>Each entry's key identifies a particular instrumentation library. The corresponding value
   * configures it.
   *
   * <p>If omitted, instrumentation defaults are used.
   */
  @JsonProperty("swift")
  @Nullable
  public ExperimentalLanguageSpecificInstrumentationModel getSwift() {
    return swift;
  }

  @JsonProperty("swift")
  public ExperimentalInstrumentationModel withSwift(
      ExperimentalLanguageSpecificInstrumentationModel swift) {
    this.swift = swift;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalInstrumentationModel{"
        + "general="
        + general
        + ", cpp="
        + cpp
        + ", dotnet="
        + dotnet
        + ", erlang="
        + erlang
        + ", go="
        + go
        + ", java="
        + java
        + ", js="
        + js
        + ", php="
        + php
        + ", python="
        + python
        + ", ruby="
        + ruby
        + ", rust="
        + rust
        + ", swift="
        + swift
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.general == null) ? 0 : this.general.hashCode();
    h *= 1000003;
    h ^= (this.cpp == null) ? 0 : this.cpp.hashCode();
    h *= 1000003;
    h ^= (this.dotnet == null) ? 0 : this.dotnet.hashCode();
    h *= 1000003;
    h ^= (this.erlang == null) ? 0 : this.erlang.hashCode();
    h *= 1000003;
    h ^= (this.go == null) ? 0 : this.go.hashCode();
    h *= 1000003;
    h ^= (this.java == null) ? 0 : this.java.hashCode();
    h *= 1000003;
    h ^= (this.js == null) ? 0 : this.js.hashCode();
    h *= 1000003;
    h ^= (this.php == null) ? 0 : this.php.hashCode();
    h *= 1000003;
    h ^= (this.python == null) ? 0 : this.python.hashCode();
    h *= 1000003;
    h ^= (this.ruby == null) ? 0 : this.ruby.hashCode();
    h *= 1000003;
    h ^= (this.rust == null) ? 0 : this.rust.hashCode();
    h *= 1000003;
    h ^= (this.swift == null) ? 0 : this.swift.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalInstrumentationModel) {
      ExperimentalInstrumentationModel that = (ExperimentalInstrumentationModel) o;
      return (this.general == null ? that.general == null : this.general.equals(that.general))
          && (this.cpp == null ? that.cpp == null : this.cpp.equals(that.cpp))
          && (this.dotnet == null ? that.dotnet == null : this.dotnet.equals(that.dotnet))
          && (this.erlang == null ? that.erlang == null : this.erlang.equals(that.erlang))
          && (this.go == null ? that.go == null : this.go.equals(that.go))
          && (this.java == null ? that.java == null : this.java.equals(that.java))
          && (this.js == null ? that.js == null : this.js.equals(that.js))
          && (this.php == null ? that.php == null : this.php.equals(that.php))
          && (this.python == null ? that.python == null : this.python.equals(that.python))
          && (this.ruby == null ? that.ruby == null : this.ruby.equals(that.ruby))
          && (this.rust == null ? that.rust == null : this.rust.equals(that.rust))
          && (this.swift == null ? that.swift == null : this.swift.equals(that.swift));
    }
    return false;
  }
}
