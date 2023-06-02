plugins {
  id("com.diffplug.spotless")
}

spotless {
  java {
    googleJavaFormat()
    licenseHeaderFile(rootProject.file("buildscripts/spotless.license.java"), "(package|import|public|// Includes work from:)")
    target("src/**/*.java")
  }
  plugins.withId("groovy") {
    groovy {
      licenseHeaderFile(rootProject.file("buildscripts/spotless.license.java"), "(package|import|class)")
    }
  }
  plugins.withId("scala") {
    scala {
      scalafmt()
      licenseHeaderFile(rootProject.file("buildscripts/spotless.license.java"), "(package|import|public)")
      target("src/**/*.scala")
    }
  }
  plugins.withId("org.jetbrains.kotlin.jvm") {
    kotlin {
      ktlint().editorConfigOverride(mapOf(
        "indent_size" to "2",
        "continuation_indent_size" to "2",
        "max_line_length" to "160",
        "insert_final_newline" to "true",
        "ktlint_standard_no-wildcard-imports" to "disabled",
        // ktlint does not break up long lines, it just fails on them
        "ktlint_standard_max-line-length" to "disabled",
        // ktlint makes it *very* hard to locate where this actually happened
        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
        // depends on ktlint_standard_wrapping
        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
        // also very hard to find out where this happens
        "ktlint_standard_wrapping" to "disabled"
      ))
      licenseHeaderFile(rootProject.file("buildscripts/spotless.license.java"), "(package|import|class|// Includes work from:)")
    }
  }
  kotlinGradle {
    ktlint().editorConfigOverride(mapOf(
      "indent_size" to "2",
      "continuation_indent_size" to "2",
      "max_line_length" to "160",
      "insert_final_newline" to "true",
      "ktlint_standard_no-wildcard-imports" to "disabled",
      // ktlint does not break up long lines, it just fails on them
      "ktlint_standard_max-line-length" to "disabled",
      // ktlint makes it *very* hard to locate where this actually happened
      "ktlint_standard_trailing-comma-on-call-site" to "disabled",
      // depends on ktlint_standard_wrapping
      "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
      // also very hard to find out where this happens
      "ktlint_standard_wrapping" to "disabled"
    ))
  }
  format("misc") {
    // not using "**/..." to help keep spotless fast
    target(
      ".gitignore",
      ".gitattributes",
      ".gitconfig",
      ".editorconfig",
      "*.md",
      "src/**/*.md",
      "docs/**/*.md",
      "*.sh",
      "src/**/*.properties",
    )
    indentWithSpaces()
    trimTrailingWhitespace()
    endWithNewline()
  }
}
