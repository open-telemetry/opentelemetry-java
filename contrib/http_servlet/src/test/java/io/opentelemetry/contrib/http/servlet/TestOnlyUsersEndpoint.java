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

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/** Only used for testing. */
@RestController
public class TestOnlyUsersEndpoint {

  @GetMapping("/users")
  public ResponseEntity<List<TestOnlyUser>> getUsers() {
    List<TestOnlyUser> users = new ArrayList<>();
    return ResponseEntity.ok(users);
  }

  @PostMapping("/users")
  public ResponseEntity<Void> createUser(TestOnlyUser user) {
    return ResponseEntity.accepted().build();
  }

  @GetMapping("/users/{userId}")
  public ResponseEntity<TestOnlyUser> getUser(@PathVariable String userId) {
    TestOnlyUser user = new TestOnlyUser("junit");
    return ResponseEntity.ok(user);
  }

  @PutMapping("/users/{userId}")
  public ResponseEntity<Void> updateUser(@PathVariable String userId, TestOnlyUser user) {
    return ResponseEntity.noContent().build();
  }
}
