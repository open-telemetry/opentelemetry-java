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
  "http",
  "code",
  "db",
  "gen_ai",
  "messaging",
  "rpc",
  "sanitization",
  "stability_opt_in_list"
})
@Generated("jsonschema2pojo")
public class ExperimentalGeneralInstrumentationModel {

  @JsonProperty("http")
  @Nullable
  private ExperimentalHttpInstrumentationModel http;

  @JsonProperty("code")
  @Nullable
  private ExperimentalCodeInstrumentationModel code;

  @JsonProperty("db")
  @Nullable
  private ExperimentalDbInstrumentationModel db;

  @JsonProperty("gen_ai")
  @Nullable
  private ExperimentalGenAiInstrumentationModel genAi;

  @JsonProperty("messaging")
  @Nullable
  private ExperimentalMessagingInstrumentationModel messaging;

  @JsonProperty("rpc")
  @Nullable
  private ExperimentalRpcInstrumentationModel rpc;

  @JsonProperty("sanitization")
  @Nullable
  private ExperimentalSanitizationModel sanitization;

  @JsonProperty("stability_opt_in_list")
  @Nullable
  private String stabilityOptInList;

  /**
   * Configure instrumentations following the http semantic conventions.
   *
   * <p>See http semantic conventions: https://opentelemetry.io/docs/specs/semconv/http/
   *
   * <p>If omitted, defaults as described in ExperimentalHttpInstrumentation are used.
   */
  @JsonProperty("http")
  @Nullable
  public ExperimentalHttpInstrumentationModel getHttp() {
    return http;
  }

  public ExperimentalGeneralInstrumentationModel withHttp(
      ExperimentalHttpInstrumentationModel http) {
    this.http = http;
    return this;
  }

  /**
   * Configure instrumentations following the code semantic conventions.
   *
   * <p>See code semantic conventions:
   * https://opentelemetry.io/docs/specs/semconv/registry/attributes/code/
   *
   * <p>If omitted, defaults as described in ExperimentalCodeInstrumentation are used.
   */
  @JsonProperty("code")
  @Nullable
  public ExperimentalCodeInstrumentationModel getCode() {
    return code;
  }

  public ExperimentalGeneralInstrumentationModel withCode(
      ExperimentalCodeInstrumentationModel code) {
    this.code = code;
    return this;
  }

  /**
   * Configure instrumentations following the database semantic conventions.
   *
   * <p>See database semantic conventions: https://opentelemetry.io/docs/specs/semconv/database/
   *
   * <p>If omitted, defaults as described in ExperimentalDbInstrumentation are used.
   */
  @JsonProperty("db")
  @Nullable
  public ExperimentalDbInstrumentationModel getDb() {
    return db;
  }

  public ExperimentalGeneralInstrumentationModel withDb(ExperimentalDbInstrumentationModel db) {
    this.db = db;
    return this;
  }

  /**
   * Configure instrumentations following the GenAI semantic conventions.
   *
   * <p>See GenAI semantic conventions: https://opentelemetry.io/docs/specs/semconv/gen-ai/
   *
   * <p>If omitted, defaults as described in ExperimentalGenAiInstrumentation are used.
   */
  @JsonProperty("gen_ai")
  @Nullable
  public ExperimentalGenAiInstrumentationModel getGenAi() {
    return genAi;
  }

  public ExperimentalGeneralInstrumentationModel withGenAi(
      ExperimentalGenAiInstrumentationModel genAi) {
    this.genAi = genAi;
    return this;
  }

  /**
   * Configure instrumentations following the messaging semantic conventions.
   *
   * <p>See messaging semantic conventions: https://opentelemetry.io/docs/specs/semconv/messaging/
   *
   * <p>If omitted, defaults as described in ExperimentalMessagingInstrumentation are used.
   */
  @JsonProperty("messaging")
  @Nullable
  public ExperimentalMessagingInstrumentationModel getMessaging() {
    return messaging;
  }

  public ExperimentalGeneralInstrumentationModel withMessaging(
      ExperimentalMessagingInstrumentationModel messaging) {
    this.messaging = messaging;
    return this;
  }

  /**
   * Configure instrumentations following the RPC semantic conventions.
   *
   * <p>See RPC semantic conventions: https://opentelemetry.io/docs/specs/semconv/rpc/
   *
   * <p>If omitted, defaults as described in ExperimentalRpcInstrumentation are used.
   */
  @JsonProperty("rpc")
  @Nullable
  public ExperimentalRpcInstrumentationModel getRpc() {
    return rpc;
  }

  public ExperimentalGeneralInstrumentationModel withRpc(ExperimentalRpcInstrumentationModel rpc) {
    this.rpc = rpc;
    return this;
  }

  /**
   * Configure general sanitization options.
   *
   * <p>If omitted, defaults as described in ExperimentalSanitization are used.
   */
  @JsonProperty("sanitization")
  @Nullable
  public ExperimentalSanitizationModel getSanitization() {
    return sanitization;
  }

  public ExperimentalGeneralInstrumentationModel withSanitization(
      ExperimentalSanitizationModel sanitization) {
    this.sanitization = sanitization;
    return this;
  }

  /**
   * Configure semantic convention stability opt-in as a comma-separated list.
   *
   * <p>This property follows the format and semantics of the OTEL_SEMCONV_STABILITY_OPT_IN
   * environment variable.
   *
   * <p>Controls the emission of stable vs. experimental semantic conventions for instrumentation.
   *
   * <p>This setting is only intended for migrating from experimental to stable semantic
   * conventions.
   *
   * <p>Known values include:
   *
   * <p>- http: Emit stable HTTP and networking conventions only
   *
   * <p>- http/dup: Emit both old and stable HTTP and networking conventions (for phased migration)
   *
   * <p>- database: Emit stable database conventions only
   *
   * <p>- database/dup: Emit both old and stable database conventions (for phased migration)
   *
   * <p>- rpc: Emit stable RPC conventions only
   *
   * <p>- rpc/dup: Emit both experimental and stable RPC conventions (for phased migration)
   *
   * <p>- messaging: Emit stable messaging conventions only
   *
   * <p>- messaging/dup: Emit both old and stable messaging conventions (for phased migration)
   *
   * <p>- code: Emit stable code conventions only
   *
   * <p>- code/dup: Emit both old and stable code conventions (for phased migration)
   *
   * <p>Multiple values can be specified as a comma-separated list (e.g., "http,database/dup").
   *
   * <p>Additional signal types may be supported in future versions.
   *
   * <p>Domain-specific semconv properties (e.g., .instrumentation/development.general.db.semconv)
   * take precedence over this general setting.
   *
   * <p>See:
   *
   * <p>- HTTP migration: https://opentelemetry.io/docs/specs/semconv/non-normative/http-migration/
   *
   * <p>- Database migration: https://opentelemetry.io/docs/specs/semconv/database/
   *
   * <p>- RPC: https://opentelemetry.io/docs/specs/semconv/rpc/
   *
   * <p>- Messaging: https://opentelemetry.io/docs/specs/semconv/messaging/messaging-spans/
   *
   * <p>If omitted or null, no opt-in is configured and instrumentations continue emitting their
   * default semantic convention version.
   */
  @JsonProperty("stability_opt_in_list")
  @Nullable
  public String getStabilityOptInList() {
    return stabilityOptInList;
  }

  public ExperimentalGeneralInstrumentationModel withStabilityOptInList(String stabilityOptInList) {
    this.stabilityOptInList = stabilityOptInList;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalGeneralInstrumentationModel{"
        + "http="
        + http
        + ", code="
        + code
        + ", db="
        + db
        + ", genAi="
        + genAi
        + ", messaging="
        + messaging
        + ", rpc="
        + rpc
        + ", sanitization="
        + sanitization
        + ", stabilityOptInList="
        + stabilityOptInList
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.http == null) ? 0 : this.http.hashCode();
    h *= 1000003;
    h ^= (this.code == null) ? 0 : this.code.hashCode();
    h *= 1000003;
    h ^= (this.db == null) ? 0 : this.db.hashCode();
    h *= 1000003;
    h ^= (this.genAi == null) ? 0 : this.genAi.hashCode();
    h *= 1000003;
    h ^= (this.messaging == null) ? 0 : this.messaging.hashCode();
    h *= 1000003;
    h ^= (this.rpc == null) ? 0 : this.rpc.hashCode();
    h *= 1000003;
    h ^= (this.sanitization == null) ? 0 : this.sanitization.hashCode();
    h *= 1000003;
    h ^= (this.stabilityOptInList == null) ? 0 : this.stabilityOptInList.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalGeneralInstrumentationModel) {
      ExperimentalGeneralInstrumentationModel that = (ExperimentalGeneralInstrumentationModel) o;
      return (this.http == null ? that.http == null : this.http.equals(that.http))
          && (this.code == null ? that.code == null : this.code.equals(that.code))
          && (this.db == null ? that.db == null : this.db.equals(that.db))
          && (this.genAi == null ? that.genAi == null : this.genAi.equals(that.genAi))
          && (this.messaging == null
              ? that.messaging == null
              : this.messaging.equals(that.messaging))
          && (this.rpc == null ? that.rpc == null : this.rpc.equals(that.rpc))
          && (this.sanitization == null
              ? that.sanitization == null
              : this.sanitization.equals(that.sanitization))
          && (this.stabilityOptInList == null
              ? that.stabilityOptInList == null
              : this.stabilityOptInList.equals(that.stabilityOptInList));
    }
    return false;
  }
}
