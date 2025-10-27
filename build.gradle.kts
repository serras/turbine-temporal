import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
}

group = "fp.serrano"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    explicitApi()

    jvm()
    js {
        browser()
        nodejs()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlin.test)
                api(libs.coroutines.core)
                api(libs.turbine)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.coroutines.test)
            }
        }
    }
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(rootDir.resolve("docs"))
    }
    // moduleName.set("Turbine Temporal")
    dokkaSourceSets {
        named("commonMain") {
            includes.from("docs.md")
        }
    }
}
