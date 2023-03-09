import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Warning

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
//    signing
}

group = "com.lounres"
version = "2.0.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.kotlin.link")
}

java.targetCompatibility = JavaVersion.VERSION_11

kotlin {
    explicitApi = Warning

    target.compilations.all {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    sourceSets {
        all {
            languageSettings {
                progressiveMode = true
//                enableLanguageFeature("ContextReceivers")
            }
        }
        main {
            dependencies {
            }
        }
        test {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("stal") {
            id = "com.lounres.gradle.stal"
            displayName = "STAL structuring and configuration plugin"
            description = "Settings plugin for flexible multi-module project configuration"
            implementationClass = "com.lounres.gradle.stal.plugin.StalSettingsPlugin"
        }
    }
}