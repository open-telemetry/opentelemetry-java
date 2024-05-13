import ru.vyarus.gradle.plugin.animalsniffer.AnimalSniffer

plugins {
  `java-library`

  id("ru.vyarus.animalsniffer")
}

dependencies {
  signature(project(path = ":animal-sniffer-signature", configuration = "generatedSignature"))
}

animalsniffer {
  sourceSets = listOf(java.sourceSets.main.get())
}

tasks.withType<AnimalSniffer> {
  // always having declared output makes this task properly participate in tasks up-to-date checks
  reports.text.required.set(true)
}
