/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal

import org.gradle.api.Project


// Utils
internal fun List<String>.toGradleName(): String = joinToString(separator = "") { ":$it" }.let { if (it == "") ":" else it }
//

// region Model
public interface ProjectFrame {
    // Naming
    public val fullName: String get() = fullNameParts.toGradleName()
    public val fullNameParts: List<String>
    // Tags
    public val tags: Set<String>
    public fun has(soughtTag: String): Boolean = soughtTag in tags
    public fun hasAnyOf(vararg soughtTags: String): Boolean = soughtTags.any { it in tags }
    public fun hasAnyOf(soughtTags: Collection<String>): Boolean = soughtTags.any { it in tags }
    public fun hasAllOf(vararg soughtTags: String): Boolean = soughtTags.all { it in tags }
    public fun hasAllOf(soughtTags: Collection<String>): Boolean = soughtTags.all { it in tags }
    public operator fun String.unaryPlus(): Boolean = this in tags
    // Hierarchy
    public val children: Set<ChildProjectFrame>
    // Gradle API
    public val project: Project
    public fun project(block: Project.() -> Unit) { project.block() }
}

public interface RootProjectFrame : ProjectFrame {
    override val fullNameParts: List<String> get() = emptyList()
}

public interface ChildProjectFrame : ProjectFrame {
    public val name: String get() = fullNameParts.first()
    // Hierarchy
    public val parent: ProjectFrame
}

public fun <P> ProjectFrame.parentOrNull(): ProjectFrame? = if (this is ChildProjectFrame) parent else null
// endregion

// region Mutable model
public interface MutableProjectFrame: ProjectFrame {
    // Tags
    public override val tags: MutableSet<String>
}

public interface MutableRootProjectFrame : MutableProjectFrame, RootProjectFrame

public interface MutableChildProjectFrame : MutableProjectFrame, ChildProjectFrame
// endregion

// region Builders
internal interface ProjectFrameBuilder: MutableProjectFrame {
    override val children: MutableSet<ChildProjectFrame>
}

internal class RootProjectFrameBuilder(
    override val tags: MutableSet<String>,
    override val project: Project,
    override val children: MutableSet<ChildProjectFrame> = mutableSetOf(),
) : MutableRootProjectFrame, ProjectFrameBuilder

internal class ChildProjectFrameBuilder(
    override val fullNameParts: List<String>,
    override val tags: MutableSet<String>,
    override val project: Project,
    override val parent: ProjectFrame,
    override val children: MutableSet<ChildProjectFrame> = mutableSetOf(),
) : MutableChildProjectFrame, ProjectFrameBuilder
// endregion