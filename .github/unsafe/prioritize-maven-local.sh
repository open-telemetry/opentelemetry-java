#!/bin/bash
set -e

# Move mavenLocal() before mavenCentral() in the repositories block
sed -i '/repositories {/,/}/ {
  /mavenCentral()/d
  /mavenLocal()/d
  /repositories {/a\    mavenLocal()
  /repositories {/a\    mavenCentral()
}' "settings.gradle.kts"
