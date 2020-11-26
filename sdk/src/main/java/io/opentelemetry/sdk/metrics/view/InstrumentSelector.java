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

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class InstrumentSelector {

  public static Builder newBuilder() {
    return new AutoValue_InstrumentSelector.Builder();
  }

  @Nullable
  public abstract InstrumentType instrumentType();

  @Nullable
  public abstract String instrumentNameRegex();

  @Memoized
  @Nullable
  public Pattern instrumentNamePattern() {
    return instrumentNameRegex() == null ? null : Pattern.compile(instrumentNameRegex());
  }

  @AutoValue.Builder
  public interface Builder {
    Builder instrumentType(InstrumentType instrumentType);

    Builder instrumentNameRegex(String regex);

    InstrumentSelector build();
  }
}
