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

package io.opentelemetry.contrib.spring.boot.actuate;

import io.opentelemetry.sdk.resources.Resource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.Ordered;

/**
 * Provides a {@link Resource} implementation which supplies a subset of labels to be merged into
 * the composite resource labels used by OpenTelemetry instrumentation.
 */
public interface ResourceProvider extends FactoryBean<Resource>, Ordered {}
