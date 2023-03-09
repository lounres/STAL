# STAL Gradle Plugin

STAL ("Structure, Tag, Action, Look up") is a Gradle plugin that helps you in structuring and configuration of multimodule Gradle projects. It provides the following features.
1. (**Structure**) Define your modules' hierarchy with specialized Kotlin DSL.
2. (**Tag**) Mechanism that lets you effortlessly assign tags to modules of your project and use them to configure your project (see the next two points). Tags can be assigned manually or via assignation rules.
3. (**Action**) Apply specified actions on the project modules in cases of specified tags presence.
4. (**Look up**) Get all modules with the specified tags and do anything with the module list.

## Using in your project

Apply the plugin in your `settings.gradle.kts` file:
```kotlin
plugins {
    id("com.lounres.gradle.stal") version "<your version>"
}
```

## Quickstart

### Declare project's structure
```kotlin
// `settings.gradle.kts`
stal {
    // `structure` is a root node that corresponds to root project.
    structure {
        // Just add submodule "docs".
        "docs"()
        // Add another submodule with 3 another submodules in it.
        "frontend" {
            // You can mark modules with tags by enumerating them in brackets.
            // You'll read how to use the tags later.
            "web"("js")
            "android"()
            "ios"()
        }
        // Add one more submodule.
        "libs" {
            // But now each subdirectory of the "libs" module directory
            // is treated as a submodule with the same name.
            subdirs()
        }
    }
}
```

### Propagate tags
```kotlin
// `settings.gradle.kts` or root `build.gradle.kts`
stal {
    tags {
        // Add tag if module satisfies the predicate.
        // Here we add tag "kotlin js" if the module has tag "js".
        "kotlin js" since { has("js") }
    }
}
```

### Perform actions triggered by tags
```kotlin
// `settings.gradle.kts` or root `build.gradle.kts`
stal {
    action {
        // Apply configuration to modules with the tag.
        on("kotlin js") {
            apply<KotlinJsPluginWrapper>()
            configure<KotlinJsProjectExtension> {
                ...
            }
        }
    }
}
```

### Look up modules with the tag
```kotlin
// root `build.gradle.kts` after its evaluation
afterEvalaute {
    // Get a list of projects that has the tag and
    // process it any way you need.
    val libs = stal.lookUp.projectsThat { has("lib") }.map { it.name }
    catalog.versionCatalog {
        bundle("libs", libs)
    }
}
```