package io.opentelemetry.sdk.metrics;/*
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

import com.google.errorprone.annotations.Immutable;
import io.opentelemetry.metrics.DefaultMeter;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.Resource;

public enum TestSdk {
    NO_SDK(new SdkBuilder() {
        @Override
        Meter build() {
            return DefaultMeter.getInstance();
        }
    }),
    SDK(new SdkBuilder() {
        @Override
        Meter build() {
            MeterProviderSharedState METER_PROVIDER_SHARED_STATE =
                    MeterProviderSharedState.create(MillisClock.getInstance(), Resource.getEmpty());
            InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
                    InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics", null);

            return new MeterSdk(METER_PROVIDER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO);
        }
    });


    private final SdkBuilder sdkBuilder;

    TestSdk(SdkBuilder sdkBuilder) {
        this.sdkBuilder = sdkBuilder;
    }

    public Meter getMeter() {
        return sdkBuilder.build();
    }

    @Immutable
    private abstract static class SdkBuilder {
        abstract Meter build();
    }
}
