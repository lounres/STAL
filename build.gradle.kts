import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Warning


plugins {
    alias(versions.plugins.kotlin.jvm)
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(versions.plugins.dokka)
    `maven-publish`
    signing
    alias(versions.plugins.nexus.publish.plugin)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.kotlin.link")
}

kotlin {
    jvmToolchain(11)
    
    explicitApi = Warning

    sourceSets {
        all {
            languageSettings {
                progressiveMode = true
                languageVersion = "2.0"
                enableLanguageFeature("ContextReceivers")
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

java {
    withSourcesJar()
}

task<Jar>("dokkaJar") {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier = "javadoc"
    afterEvaluate {
        val dokkaGeneratePublicationHtml by tasks.getting
        dependsOn(dokkaGeneratePublicationHtml)
        from(dokkaGeneratePublicationHtml)
    }
}

afterEvaluate {
    publishing.publications.withType<MavenPublication> {
        artifact(tasks.named<Jar>("dokkaJar"))
    }
}

// GR-26091
tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}

publishing {
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.properties["ossrhUsername"].toString()
                password = project.properties["ossrhPassword"].toString()
            }
        }
    }
    publications.withType<MavenPublication> {
        pom {
            name = "STAL Gradle plugin"
            description = "Gradle plugin for declarative definition of complex modules structure"
            url = "https://github.com/lounres/STAL"

            licenses {
                license {
                    name = "Apache License, Version 2.0"
                    url = "https://opensource.org/license/apache-2-0/"
                }
            }
            developers {
                developer {
                    id = "lounres"
                    name = "Gleb Minaev"
                    email = "minaevgleb@yandex.ru"
                }
            }
            scm {
                url = "https://github.com/lounres/STAL"
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

gradlePlugin {
    website = "https://github.com/lounres/STAL"
    vcsUrl = "https://github.com/lounres/STAL.git"

    plugins {
        create("stal") {
            id = "dev.lounres.gradle.stal"
            displayName = "STAL structuring and configuration plugin"
            description = "Settings plugin for flexible multi-module project configuration"
            tags = listOf("settings", "mutlimodule")
            implementationClass = "dev.lounres.gradle.stal.plugin.StalSettingsPlugin"
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}