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

package io.opentelemetry.trace;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.Attributes;
import jdk.nashorn.internal.ir.annotations.Immutable;

@AutoValue
@Immutable
public abstract class LinkEx {

  public abstract SpanContext getSpanContext();

  public abstract Attributes getAttributes();

  public static LinkEx create(SpanContext spanContext, Attributes attributes) {
    return new AutoValue_LinkEx(spanContext, attributes);
  }
}
