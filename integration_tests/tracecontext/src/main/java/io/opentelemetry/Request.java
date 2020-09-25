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

package io.opentelemetry;

import java.util.Arrays;

public class Request {
  private String url;
  private Request[] arguments;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Request[] getArguments() {
    return arguments;
  }

  public void setArguments(Request[] arguments) {
    this.arguments = arguments;
  }

  @Override
  public String toString() {
    return "Request{" + "url='" + url + '\'' + ", arguments=" + Arrays.toString(arguments) + '}';
  }
}
