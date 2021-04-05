import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.31"
    id("org.jetbrains.intellij") version "0.7.2"
    id("org.jetbrains.changelog") version "1.1.2"
}

fun properties(key: String) = project.findProperty(key).toString()

group = "org.jetbrains.compose.desktop.ide.preview"
version = properties("deploy.version")

repositories {
    mavenCentral()
    jcenter()
}

intellij {
    pluginName = "Compose Desktop Preview"
    type = "IC"
    downloadSources = true
    updateSinceUntilBuild = true
    version = "203.7717.56"

    setPlugins(
        "java",
        "com.intellij.gradle",
        "org.jetbrains.kotlin:203-1.4.31-release-IJ7148.5"
    )
}

tasks {
    // Set the compatibility versions to 1.8
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    withType<KotlinJvmCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
