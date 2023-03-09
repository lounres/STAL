/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.plugin.settings

import com.lounres.gradle.stal.dsl.ProjectFrame
import com.lounres.gradle.stal.model.structure.StructureVertexInformation
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import java.io.FileFilter


//context(Settings)
internal fun SettingsProjectFrameBuilder.buildWith(context: Settings, vertex: StructureVertexInformation): ProjectFrame<ProjectDescriptor> {

    vertex.descriptorHandlers.forEach { project.it() }
    tags += vertex.tags

    vertex.explicitChildren.forEach { addChild(it.name).buildWith(context, it) }

    vertex.subdirChildren.forEach { childVertex ->
        val searchDir = childVertex.searchDir ?: project.projectDir
        for (dir in searchDir.listFiles(FileFilter { it.isDirectory && context.findProject(it) == null })!!) {
            addChild(dir.name).buildWith(context, childVertex)
        }
    }

    return this
}

internal fun settingsProjectFrameBuilders(context: Settings, vertex: StructureVertexInformation) =
    SettingsProjectFrameBuilder(context, context.rootProject).buildWith(context, vertex)