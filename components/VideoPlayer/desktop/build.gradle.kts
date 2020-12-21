import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm {}
    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":VideoPlayer:common"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.jetbrains.compose.videoplayer.demo.MainKt"
    }
}