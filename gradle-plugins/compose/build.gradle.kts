import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem

plugins {
    kotlin("jvm")
    id("de.fuerstenau.buildconfig")
    id("com.gradle.plugin-publish")
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

gradlePluginConfig {
    pluginId = "org.jetbrains.compose"
    artifactId = "compose-gradle-plugin"
    displayName = "JetBrains Compose Plugin"
    description = "JetBrains Compose Gradle plugin for easy configuration"
    implementationClass = "org.jetbrains.compose.ComposePlugin"
}

buildConfig {
    packageName = "org.jetbrains.compose"
    clsName = "ComposeBuildConfig"
    buildConfigField("String", "composeVersion", BuildProperties.composeVersion(project))
}

val embedded by configurations.creating

dependencies {
    compileOnly(gradleApi())
    compileOnly(localGroovy())
    compileOnly(kotlin("gradle-plugin-api"))
    compileOnly(kotlin("gradle-plugin"))
    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.13")

    fun embeddedCompileOnly(dep: String) {
        compileOnly(dep)
        embedded(dep)
    }
    // include relocated download task to avoid potential runtime conflicts
    embeddedCompileOnly("de.undercouch:gradle-download-task:4.1.1")
}

val shadow = tasks.named<ShadowJar>("shadowJar") {
    val fromPackage = "de.undercouch"
    val toPackage = "org.jetbrains.compose.$fromPackage"
    relocate(fromPackage, toPackage)
    archiveClassifier.set("shadow")
    configurations = listOf(embedded)
    exclude("META-INF/gradle-plugins/de.undercouch.download.properties")
}

val jar = tasks.named<Jar>("jar") {
    dependsOn(shadow)
    from(zipTree(shadow.get().archiveFile))
    this.duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

val gradleVersionForTests = "6.7"
val java14Home: String? = when (JavaVersion.current()) {
    JavaVersion.VERSION_14 -> System.getProperty("java.home")
    else -> System.getenv("JDK_14")
}
val isWindows = getCurrentOperatingSystem().isWindows

afterEvaluate {
    tasks.named<Test>("test") {
        dependsOn("publishToMavenLocal")
        systemProperty("compose.plugin.version", BuildProperties.deployVersion(project))
        systemProperty("gradle.version.for.tests", gradleVersionForTests)

        if (java14Home != null) {
            val executableFileName = if (isWindows) "java.exe" else "java"
            executable = File(java14Home).resolve("bin/$executableFileName").absolutePath
        } else {
            doFirst { error("Use JDK 14 to run tests or set up JDK_14 env. var") }
        }
    }
}
