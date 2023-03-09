/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.dsl


// region Model
public interface ProjectFrame<out P> {
    // Tags
    public val tags: Set<String>
    public fun has(soughtTag: String): Boolean = soughtTag in tags
    public fun hasAnyOf(vararg soughtTags: String): Boolean = soughtTags.any { it in tags }
    public fun hasAnyOf(soughtTags: Collection<String>): Boolean = soughtTags.any { it in tags }
    public fun hasAllOf(vararg soughtTags: String): Boolean = soughtTags.all { it in tags }
    public fun hasAllOf(soughtTags: Collection<String>): Boolean = soughtTags.all { it in tags }
    public operator fun String.unaryPlus(): Boolean = this in tags
    // Hierarchy
    public val children: Set<ChildProjectFrame<P>>
    // Gradle API
    public val project: P
    public fun project(block: P.() -> Unit) { project.apply(block) }
}

public interface ChildProjectFrame<out P> : ProjectFrame<P> {
    // Hierarchy
    public val parent: ProjectFrame<P>
}

public fun <P> ProjectFrame<P>.parentOrNull(): ProjectFrame<P>? = if (this is ChildProjectFrame<P>) parent else null
// endregion

// region Mutable model
internal interface MutableProjectFrame<P> : ProjectFrame<P> {
    override val tags: MutableSet<String>
    override val children: MutableSet<ChildProjectFrame<P>>
}
// endregion