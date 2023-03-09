/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.dsl

import org.gradle.api.Project
import org.gradle.api.initialization.ProjectDescriptor
import java.io.File
import kotlin.annotation.AnnotationTarget.*


@DslMarker
@Target(TYPE, CLASS, TYPEALIAS)
internal annotation class StalSettingsDslMarker

@DslMarker
@Target(TYPE, CLASS, TYPEALIAS)
internal annotation class StalRootProjectDslMarker

// region Common aliases
@StalSettingsDslMarker
@StalRootProjectDslMarker
public typealias Predicate = ProjectFrame<Project>.() -> Boolean

@StalSettingsDslMarker
@StalRootProjectDslMarker
public typealias Action = Project.() -> Unit
// endregion

// region Global DSL model
@StalSettingsDslMarker
public interface StalSettingsDsl {
    public val structure: StructureDsl
    public fun structure(block: StructureDsl.() -> Unit): Unit = structure.block()
    public val tag: TagDsl
    public fun tag(block: TagDsl.() -> Unit): Unit = tag.block()
    public val action: ActionDsl
    public fun action(block: ActionDsl.() -> Unit): Unit = action.block()
}
@StalRootProjectDslMarker
public interface StalRootProjectDsl {
    public val tag: TagDsl
    public fun tag(block: TagDsl.() -> Unit): Unit = tag.block()
    public val action: ActionDsl
    public fun action(block: ActionDsl.() -> Unit): Unit = action.block()
    public val lookUp: LookUpDsl
    public fun lookUp(block: LookUpDsl.() -> Unit): Unit = lookUp.block()
}
// endregion

// region Structure
@StalSettingsDslMarker
public typealias StructureDsl = DirDsl
@StalSettingsDslMarker
public interface DirDsl {
    // Building
    public operator fun String.invoke(tags: Collection<String> = emptyList(), subScope: (DirDsl.() -> Unit)? = null)
    public operator fun String.invoke(vararg tags: String = emptyArray(), subScope: (DirDsl.() -> Unit)? = null)
    public fun subdirs(tags: List<String> = emptyList(), searchDir: File? = null, includeIf: (File) -> Boolean = { true }, subScope: (DirDsl.() -> Unit)? = null)
    public fun subdirs(vararg tags: String = emptyArray(), searchDir: File? = null, includeIf: (File) -> Boolean = { true }, subScope: (DirDsl.() -> Unit)? = null)
    // Tagging
    public fun taggedWith(vararg tags: String)
    // Configuring
    public fun descriptor(configuration: (@StalSettingsDslMarker ProjectDescriptor).() -> Unit)
}
// endregion

// region Tag
@StalSettingsDslMarker
@StalRootProjectDslMarker
public interface TagDsl {
    public infix fun String.since(predicate: Predicate)
}
// endregion

// region Action
// FIXME: Make me a value class instead of a data class
@StalSettingsDslMarker
@StalRootProjectDslMarker
public data class WheneverDslAtom(
    val predicate: Predicate
)

@StalSettingsDslMarker
@StalRootProjectDslMarker
public interface ActionDsl {
    // Simple action descriptions
    public fun on(tag: String, action: Action)
    public fun onAny(vararg tags: String, action: Action)
    public fun onAll(vararg tags: String, action: Action)
    // Full-value action description
    public fun whenever(predicate: Predicate) : WheneverDslAtom = WheneverDslAtom(predicate)
    public infix fun WheneverDslAtom.imply(action: Action)
}
// endregion

// region Look up
@StalRootProjectDslMarker
public interface LookUpDsl {
    public val allProjectFrames: List<ProjectFrame<Project>>
    public val allProject: List<Project>
    public fun projectFramesThat(predicate: Predicate): List<ProjectFrame<Project>>
    public fun projectsThat(predicate: Predicate): List<Project>
}
// endregion