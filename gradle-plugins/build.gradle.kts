import com.gradle.publish.PluginBundleExtension

plugins {
    kotlin("jvm") version "1.4.0" apply false
    id("com.gradle.plugin-publish") version "0.10.1" apply false
    id("de.fuerstenau.buildconfig") version "1.1.8" apply false
}

subprojects {
    group = BuildProperties.group
    version = BuildProperties.version

    repositories {
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        jcenter()
        mavenLocal()
    }

    plugins.withId("java") {
        configureIfExists<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }

    plugins.withId("maven-publish") {
        configureIfExists<PublishingExtension> {
            repositories {
                maven("ComposeRepo") {
                    setUrl(System.getenv("COMPOSE_REPO_URL"))
                    credentials {
                        username = System.getenv("COMPOSE_REPO_USERNAME")
                        password = System.getenv("COMPOSE_REPO_KEY")
                    }
                }
            }
        }
    }

    afterEvaluate {
        gradlePluginConfig?.let { configureGradlePlugin(it) }
    }
}

fun Project.configureGradlePlugin(config: GradlePluginConfigExtension) {
    // maven publication for plugin
    configureIfExists<PublishingExtension> {
        publications.create<MavenPublication>("gradlePlugin") {
            artifactId = config.artifactId
            pom {
                name.set(config.displayName)
                description.set(config.description)
                url.set(BuildProperties.website)
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }

    // metadata for gradle plugin portal (relates to pluginBundle extension block from com.gradle.plugin-publish)
    configureIfExists<PluginBundleExtension> {
        vcsUrl = BuildProperties.vcs
        website = BuildProperties.website
        description = config.description
    }

    // gradle plugin definition (relates to gradlePlugin extension block from java-gradle-plugin)
    configureIfExists<GradlePluginDevelopmentExtension> {
        plugins {
            create("gradlePlugin") {
                id = config.pluginId
                displayName = config.displayName
                description = config.description
                implementationClass = config.implementationClass
                version = project.version
            }
        }
    }
}
