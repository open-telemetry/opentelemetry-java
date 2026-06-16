/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalGeneralInstrumentationModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("http")
  private ExperimentalHttpInstrumentationModel http;

  /** (Can be null) */
  @Nullable
  @JsonProperty("code")
  private ExperimentalCodeInstrumentationModel code;

  /** (Can be null) */
  @Nullable
  @JsonProperty("db")
  private ExperimentalDbInstrumentationModel db;

  /** (Can be null) */
  @Nullable
  @JsonProperty("gen_ai")
  private ExperimentalGenAiInstrumentationModel genAi;

  /** (Can be null) */
  @Nullable
  @JsonProperty("messaging")
  private ExperimentalMessagingInstrumentationModel messaging;

  /** (Can be null) */
  @Nullable
  @JsonProperty("rpc")
  private ExperimentalRpcInstrumentationModel rpc;

  /** (Can be null) */
  @Nullable
  @JsonProperty("sanitization")
  private ExperimentalSanitizationModel sanitization;

  /**
   * Configure semantic convention stability opt-in as a comma-separated list. This property follows
   * the format and semantics of the OTEL_SEMCONV_STABILITY_OPT_IN environment variable. Controls
   * the emission of stable vs. experimental semantic conventions for instrumentation. This setting
   * is only intended for migrating from experimental to stable semantic conventions.
   *
   * <p>Known values include: - http: Emit stable HTTP and networking conventions only - http/dup:
   * Emit both old and stable HTTP and networking conventions (for phased migration) - database:
   * Emit stable database conventions only - database/dup: Emit both old and stable database
   * conventions (for phased migration) - rpc: Emit stable RPC conventions only - rpc/dup: Emit both
   * experimental and stable RPC conventions (for phased migration) - messaging: Emit stable
   * messaging conventions only - messaging/dup: Emit both old and stable messaging conventions (for
   * phased migration) - code: Emit stable code conventions only - code/dup: Emit both old and
   * stable code conventions (for phased migration)
   *
   * <p>Multiple values can be specified as a comma-separated list (e.g., "http,database/dup").
   * Additional signal types may be supported in future versions.
   *
   * <p>Domain-specific semconv properties (e.g., .instrumentation/development.general.db.semconv)
   * take precedence over this general setting.
   *
   * <p>See: - HTTP migration:
   * https://opentelemetry.io/docs/specs/semconv/non-normative/http-migration/ - Database migration:
   * https://opentelemetry.io/docs/specs/semconv/database/ - RPC:
   * https://opentelemetry.io/docs/specs/semconv/rpc/ - Messaging:
   * https://opentelemetry.io/docs/specs/semconv/messaging/messaging-spans/ If omitted or null, no
   * opt-in is configured and instrumentations continue emitting their default semantic convention
   * version.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("stability_opt_in_list")
  @JsonPropertyDescription(
      "Configure semantic convention stability opt-in as a comma-separated list.\nThis property follows the format and semantics of the OTEL_SEMCONV_STABILITY_OPT_IN environment variable.\nControls the emission of stable vs. experimental semantic conventions for instrumentation.\nThis setting is only intended for migrating from experimental to stable semantic conventions.\n\nKnown values include:\n- http: Emit stable HTTP and networking conventions only\n- http/dup: Emit both old and stable HTTP and networking conventions (for phased migration)\n- database: Emit stable database conventions only\n- database/dup: Emit both old and stable database conventions (for phased migration)\n- rpc: Emit stable RPC conventions only\n- rpc/dup: Emit both experimental and stable RPC conventions (for phased migration)\n- messaging: Emit stable messaging conventions only\n- messaging/dup: Emit both old and stable messaging conventions (for phased migration)\n- code: Emit stable code conventions only\n- code/dup: Emit both old and stable code conventions (for phased migration)\n\nMultiple values can be specified as a comma-separated list (e.g., \"http,database/dup\").\nAdditional signal types may be supported in future versions.\n\nDomain-specific semconv properties (e.g., .instrumentation/development.general.db.semconv) take precedence over this general setting.\n\nSee:\n- HTTP migration: https://opentelemetry.io/docs/specs/semconv/non-normative/http-migration/\n- Database migration: https://opentelemetry.io/docs/specs/semconv/database/\n- RPC: https://opentelemetry.io/docs/specs/semconv/rpc/\n- Messaging: https://opentelemetry.io/docs/specs/semconv/messaging/messaging-spans/\nIf omitted or null, no opt-in is configured and instrumentations continue emitting their default semantic convention version.\n")
  private String stabilityOptInList;

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

  @JsonProperty("db")
  @Nullable
  public ExperimentalDbInstrumentationModel getDb() {
    return db;
  }

  public ExperimentalGeneralInstrumentationModel withDb(ExperimentalDbInstrumentationModel db) {
    this.db = db;
    return this;
  }

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

  @JsonProperty("rpc")
  @Nullable
  public ExperimentalRpcInstrumentationModel getRpc() {
    return rpc;
  }

  public ExperimentalGeneralInstrumentationModel withRpc(ExperimentalRpcInstrumentationModel rpc) {
    this.rpc = rpc;
    return this;
  }

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
   * Configure semantic convention stability opt-in as a comma-separated list. This property follows
   * the format and semantics of the OTEL_SEMCONV_STABILITY_OPT_IN environment variable. Controls
   * the emission of stable vs. experimental semantic conventions for instrumentation. This setting
   * is only intended for migrating from experimental to stable semantic conventions.
   *
   * <p>Known values include: - http: Emit stable HTTP and networking conventions only - http/dup:
   * Emit both old and stable HTTP and networking conventions (for phased migration) - database:
   * Emit stable database conventions only - database/dup: Emit both old and stable database
   * conventions (for phased migration) - rpc: Emit stable RPC conventions only - rpc/dup: Emit both
   * experimental and stable RPC conventions (for phased migration) - messaging: Emit stable
   * messaging conventions only - messaging/dup: Emit both old and stable messaging conventions (for
   * phased migration) - code: Emit stable code conventions only - code/dup: Emit both old and
   * stable code conventions (for phased migration)
   *
   * <p>Multiple values can be specified as a comma-separated list (e.g., "http,database/dup").
   * Additional signal types may be supported in future versions.
   *
   * <p>Domain-specific semconv properties (e.g., .instrumentation/development.general.db.semconv)
   * take precedence over this general setting.
   *
   * <p>See: - HTTP migration:
   * https://opentelemetry.io/docs/specs/semconv/non-normative/http-migration/ - Database migration:
   * https://opentelemetry.io/docs/specs/semconv/database/ - RPC:
   * https://opentelemetry.io/docs/specs/semconv/rpc/ - Messaging:
   * https://opentelemetry.io/docs/specs/semconv/messaging/messaging-spans/ If omitted or null, no
   * opt-in is configured and instrumentations continue emitting their default semantic convention
   * version.
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
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalGeneralInstrumentationModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("http");
    sb.append('=');
    sb.append(((this.http == null) ? "<null>" : this.http));
    sb.append(',');
    sb.append("code");
    sb.append('=');
    sb.append(((this.code == null) ? "<null>" : this.code));
    sb.append(',');
    sb.append("db");
    sb.append('=');
    sb.append(((this.db == null) ? "<null>" : this.db));
    sb.append(',');
    sb.append("genAi");
    sb.append('=');
    sb.append(((this.genAi == null) ? "<null>" : this.genAi));
    sb.append(',');
    sb.append("messaging");
    sb.append('=');
    sb.append(((this.messaging == null) ? "<null>" : this.messaging));
    sb.append(',');
    sb.append("rpc");
    sb.append('=');
    sb.append(((this.rpc == null) ? "<null>" : this.rpc));
    sb.append(',');
    sb.append("sanitization");
    sb.append('=');
    sb.append(((this.sanitization == null) ? "<null>" : this.sanitization));
    sb.append(',');
    sb.append("stabilityOptInList");
    sb.append('=');
    sb.append(((this.stabilityOptInList == null) ? "<null>" : this.stabilityOptInList));
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
    result = ((result * 31) + ((this.sanitization == null) ? 0 : this.sanitization.hashCode()));
    result = ((result * 31) + ((this.code == null) ? 0 : this.code.hashCode()));
    result = ((result * 31) + ((this.genAi == null) ? 0 : this.genAi.hashCode()));
    result = ((result * 31) + ((this.rpc == null) ? 0 : this.rpc.hashCode()));
    result = ((result * 31) + ((this.http == null) ? 0 : this.http.hashCode()));
    result =
        ((result * 31)
            + ((this.stabilityOptInList == null) ? 0 : this.stabilityOptInList.hashCode()));
    result = ((result * 31) + ((this.db == null) ? 0 : this.db.hashCode()));
    result = ((result * 31) + ((this.messaging == null) ? 0 : this.messaging.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalGeneralInstrumentationModel) == false) {
      return false;
    }
    ExperimentalGeneralInstrumentationModel rhs = ((ExperimentalGeneralInstrumentationModel) other);
    return (((((((((this.sanitization == rhs.sanitization)
                                    || ((this.sanitization != null)
                                        && this.sanitization.equals(rhs.sanitization)))
                                && ((this.code == rhs.code)
                                    || ((this.code != null) && this.code.equals(rhs.code))))
                            && ((this.genAi == rhs.genAi)
                                || ((this.genAi != null) && this.genAi.equals(rhs.genAi))))
                        && ((this.rpc == rhs.rpc)
                            || ((this.rpc != null) && this.rpc.equals(rhs.rpc))))
                    && ((this.http == rhs.http)
                        || ((this.http != null) && this.http.equals(rhs.http))))
                && ((this.stabilityOptInList == rhs.stabilityOptInList)
                    || ((this.stabilityOptInList != null)
                        && this.stabilityOptInList.equals(rhs.stabilityOptInList))))
            && ((this.db == rhs.db) || ((this.db != null) && this.db.equals(rhs.db))))
        && ((this.messaging == rhs.messaging)
            || ((this.messaging != null) && this.messaging.equals(rhs.messaging))));
  }
}
