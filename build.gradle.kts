import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML


plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.23"
  id("org.jetbrains.intellij") version "1.17.3"
  id("org.jetbrains.changelog") version "2.0.0"
}

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

group = "okfe-code-helper"
version = providers.gradleProperty("pluginVersion").getOrElse("0.0.1")

repositories {
  mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2023.2.6")
  type.set("IC") // Target IDE Platform

  // plugins.set(listOf("JavaScript"))
}

changelog {
    groups.empty()
    repositoryUrl.set(properties("pluginRepositoryUrl"))
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  // patchPluginXml {
  //   sinceBuild.set("232")
  //   untilBuild.set("300")
  //   version.set(properties("pluginVersion"))
  // }

  patchPluginXml {
    version.set(properties("pluginVersion"))
    sinceBuild.set("223")
    untilBuild.set("241.*")


//    copy readme content and inject to plugin.xml description

    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
//    pluginDescription
    pluginDescription.set(providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
        val start = "<!-- Plugin description -->"
        val end = "<!-- Plugin description end -->"

        with (it.lines()) {
            if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
            }
            subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
        }
    })


//      copy changelog to replace the change-notes at plugin.xml

     val changelog = project.changelog // local variable for configuration cache compatibility
//     Get the latest available change notes from the changelog file
    changeNotes.set(properties("pluginVersion").map { pluginVersion ->
         with(changelog) {
          val changeLogItem = getOrNull(pluginVersion)
            if (changeLogItem == null) {
                throw GradleException("Changelog for version $pluginVersion not found")
            }
             renderItem(
                 changeLogItem
                     .withHeader(true)
                     .withEmptySections(true)
                     .withSummary(true)
                     .withLinks(true),
                 Changelog.OutputType.HTML,
             )
         }
     })
  }

  signPlugin {
    certificateChain.set(file(".keys/chain.crt").readText(Charsets.UTF_8))
    privateKey.set(file(".keys/private.pem").readText(Charsets.UTF_8))
    password.set(file(".keys/password.txt").readText(Charsets.UTF_8))
  }

  publishPlugin {
    token.set(file(".keys/token.txt").readText(Charsets.UTF_8))
    channels.set(listOf(properties("publishVersion").getOrNull()))
  }
}
