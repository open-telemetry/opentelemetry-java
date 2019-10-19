/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.contrib.http.servlet;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import io.opentelemetry.context.propagation.HttpTextFormat;
import java.util.List;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides support for integrating with multiple external tracing system which use various HTTP
 * headers to propagate distributed tracing information. Keys requested are expected to be those
 * defined by the W3C Trace Context specification.
 */
public class MultiSchemeHttpPropagationGetter implements HttpTextFormat.Getter<HttpServletRequest> {

  /** W3C Trace Context traceparent header name. */
  public static final String TRACEPARENT = "traceparent";
  /** W3C Trace Context tracestate header name. */
  public static final String TRACESTATE = "tracestate";

  public static final String ALL_SCHEMES =
      W3cTraceContextHttpPropagationGetter.SCHEME_NAME
          + ","
          + B3HttpPropagationGetter.SCHEME_NAME
          + ","
          + AwsXrayHttpPropagationGetter.SCHEME_NAME;

  private final String internalSchemes;
  private List<SchemeSpecificHttpPropagationGetter> getters;

  /**
   * Constructs a getter.
   *
   * @param internalSchemes a comma-separated list of internal scheme codes
   */
  public MultiSchemeHttpPropagationGetter(String internalSchemes) {
    super();
    this.internalSchemes = internalSchemes;
  }

  /**
   * Initializes all of the configured internal getters along with any additionally supplied ones.
   *
   * @param additionalGetter externally supplied getters
   */
  public void initializeGetters(SchemeSpecificHttpPropagationGetter... additionalGetter) {
    ImmutableList.Builder<SchemeSpecificHttpPropagationGetter> builder = ImmutableList.builder();
    if (!isNullOrEmpty(internalSchemes)) {
      for (String code : Splitter.on(',').split(internalSchemes)) {
        if (W3cTraceContextHttpPropagationGetter.SCHEME_NAME.equals(code)) {
          builder.add(new W3cTraceContextHttpPropagationGetter());
        } else if (B3HttpPropagationGetter.SCHEME_NAME.equals(code)) {
          builder.add(new B3HttpPropagationGetter());
        } else if (AwsXrayHttpPropagationGetter.SCHEME_NAME.equals(code)) {
          builder.add(new AwsXrayHttpPropagationGetter());
        }
      }
    }
    if (additionalGetter != null) {
      for (SchemeSpecificHttpPropagationGetter getter : additionalGetter) {
        builder.add(getter);
      }
    }
    getters = builder.build();
    if (getters.isEmpty()) {
      throw new IllegalStateException("At least one scheme specific getter must be specified");
    }
  }

  @Nullable
  @Override
  public String get(HttpServletRequest carrier, String key) {
    checkInitialized();
    for (SchemeSpecificHttpPropagationGetter getter : getters) {
      if (getter.canProvideValues(carrier)) {
        return getter.get(carrier, key);
      }
    }
    return null;
  }

  private void checkInitialized() {
    if (getters == null) {
      throw new IllegalStateException("initializeGetters must be called before use");
    }
  }
}
