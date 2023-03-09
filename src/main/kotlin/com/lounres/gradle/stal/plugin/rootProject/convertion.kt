/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.plugin.rootProject

import com.lounres.gradle.stal.dsl.MutableProjectFrame
import com.lounres.gradle.stal.dsl.ProjectFrame
import org.gradle.api.Project
import org.gradle.api.initialization.ProjectDescriptor


//context(Project)
internal fun BuildProjectFrameBuilder.buildWith(context: Project, settingsFrame: ProjectFrame<ProjectDescriptor>): List<MutableProjectFrame<Project>> {
    val result = mutableListOf<MutableProjectFrame<Project>>(this)

    tags += settingsFrame.tags

    settingsFrame.children.forEach { result += addChild(context.project(it.project.path)).buildWith(context, it) }

    return result
}

//context(Project)
internal fun buildProjectFrameBuildersList(context: Project, settingsFrame: ProjectFrame<ProjectDescriptor>) =
    BuildProjectFrameBuilder(context, context.rootProject).buildWith(context, settingsFrame)