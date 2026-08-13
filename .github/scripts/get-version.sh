#!/bin/bash -e

grep "var ver = " version.gradle.kts | grep -Eo "[0-9]+\.[0-9]+\.[0-9]+"
