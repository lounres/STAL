/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package dev.lounres.gradle.stal.dsl

import dev.lounres.gradle.stal.ProjectFrame
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
@StalRootProjectDslMarker
public typealias ProjectFramePredicate = ProjectFrame.() -> Boolean

@StalRootProjectDslMarker
public typealias ProjectAction = Project.() -> Unit
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
    // Settings
    public var defaultIncludeIf: ((File) -> Boolean)?
    // Building
    public operator fun String.invoke(tags: Collection<String> = emptyList(), subScope: (DirDsl.() -> Unit)? = null)
    public operator fun String.invoke(vararg tags: String = emptyArray(), subScope: (DirDsl.() -> Unit)? = null)
    public fun subdirs(tags: List<String> = emptyList(), searchDir: File? = null, includeIf: ((File) -> Boolean)? = null, subScope: (DirDsl.() -> Unit)? = null)
    public fun subdirs(vararg tags: String = emptyArray(), searchDir: File? = null, includeIf: ((File) -> Boolean)? = null, subScope: (DirDsl.() -> Unit)? = null)
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
    public infix fun String.since(predicate: ProjectFramePredicate)
}
// endregion

// region Action
@StalSettingsDslMarker
@StalRootProjectDslMarker
@JvmInline
public value class CaseDslAtom(
    public val predicate: ProjectFramePredicate
)

@StalSettingsDslMarker
@StalRootProjectDslMarker
public interface ActionDsl {
    public infix fun String.does(action: ProjectAction)
    public operator fun String.invoke(action: ProjectAction)
    public fun case(predicate: ProjectFramePredicate): CaseDslAtom = CaseDslAtom(predicate)
    public infix fun CaseDslAtom.implies(action: ProjectAction)
}
// endregion

// region Look up
@StalRootProjectDslMarker
public interface LookUpDsl {
    public val allProjectFrames: List<ProjectFrame>
    public val allProject: List<Project>
    public fun projectFramesThat(predicate: ProjectFramePredicate): List<ProjectFrame>
    public fun projectsThat(predicate: ProjectFramePredicate): List<Project>
}
// endregion