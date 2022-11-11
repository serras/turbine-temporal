@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
}

group = "fp.serrano"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.test)
    implementation(libs.coroutines.core)
    implementation(libs.turbine)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    explicitApi()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.dokkaHtml.configure {
    outputDirectory.set(rootDir.resolve("docs"))
    // moduleName.set("Inikio")
    dokkaSourceSets {
        named("main") {
            includes.from("docs.md")
        }
    }
}