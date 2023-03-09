/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.plugin.settings

import com.lounres.gradle.stal.dsl.ChildProjectFrame
import com.lounres.gradle.stal.dsl.MutableProjectFrame
import com.lounres.gradle.stal.dsl.ProjectFrame
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings


public val ProjectDescriptor.sanePath: String get() = path.let { if (it == ":") "" else it }

//context(Settings)
internal open class SettingsProjectFrameBuilder(
    val context: Settings,
    override val project: ProjectDescriptor
) : MutableProjectFrame<ProjectDescriptor> {
    override val tags: MutableSet<String> = mutableSetOf()
    override val children: MutableSet<ChildProjectFrame<ProjectDescriptor>> = mutableSetOf()

    fun addChild(name: String): ChildSettingsProjectFrameBuilder {
        val childPath = "${project.sanePath}:$name"
        context.include(childPath)
        val childProject = context.project(childPath)
        val child = ChildSettingsProjectFrameBuilder(context, this, childProject)
        children.add(child)
        return child
    }
}


//context(Settings)
internal class ChildSettingsProjectFrameBuilder(
    context: Settings,
    override val parent: ProjectFrame<ProjectDescriptor>,
    project: ProjectDescriptor
) : SettingsProjectFrameBuilder(context, project), ChildProjectFrame<ProjectDescriptor> {
    override val tags: MutableSet<String> = mutableSetOf()
    override val children: MutableSet<ChildProjectFrame<ProjectDescriptor>> = mutableSetOf()
}