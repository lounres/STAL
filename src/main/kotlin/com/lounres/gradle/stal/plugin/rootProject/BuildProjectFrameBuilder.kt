/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.plugin.rootProject

import com.lounres.gradle.stal.dsl.ChildProjectFrame
import com.lounres.gradle.stal.dsl.MutableProjectFrame
import org.gradle.api.Project


public val Project.sanePath: String get() = path.let { if (it == ":") "" else it }

//context(Project)
internal open class BuildProjectFrameBuilder(val context: Project, override val project: Project) : MutableProjectFrame<Project> {
    override val tags: MutableSet<String> = mutableSetOf()
    override val children: MutableSet<ChildProjectFrame<Project>> = mutableSetOf()

    fun addChild(childProject: Project): ChildBuildProjectFrameBuilder {
        val child = ChildBuildProjectFrameBuilder(context, this, childProject)
        children.add(child)
        return child
    }
}


//context(Project)
internal class ChildBuildProjectFrameBuilder(
    context: Project,
    override val parent: BuildProjectFrameBuilder,
    project: Project
) : BuildProjectFrameBuilder(context, project), ChildProjectFrame<Project> {
    override val tags: MutableSet<String> = mutableSetOf()
    override val children: MutableSet<ChildProjectFrame<Project>> = mutableSetOf()
}