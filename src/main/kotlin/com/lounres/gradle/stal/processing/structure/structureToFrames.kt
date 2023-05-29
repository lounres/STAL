/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.processing.structure

import com.lounres.gradle.stal.*
import com.lounres.gradle.stal.ChildProjectFrameBuilder
import com.lounres.gradle.stal.RootProjectFrameBuilder
import org.gradle.api.Project


internal data class StructureConversionResult(
    val rootFrame: RootProjectFrame,
    val allFrames: List<ProjectFrame>,
)

context(Project)
internal fun RootStructureNode.toFrame(): StructureConversionResult {
    val allFrames = mutableListOf<ProjectFrame>()

    val rootFrame = RootProjectFrameBuilder(
        tags = this.tags,
        project = rootProject,
    )

    allFrames += rootFrame

    fun process(
        childStructure: StructureNode,
        parentFrame: ProjectFrameBuilder,
    ) {
        val childFrame = ChildProjectFrameBuilder(
            fullNameParts = childStructure.fullNameParts,
            tags = childStructure.tags,
            project = project(childStructure.fullNameParts.toGradleName()),
            parent = parentFrame,
        )
        parentFrame.children += childFrame
        allFrames += childFrame
        for (grandchildStructure in childStructure.children)
            process(
                childStructure = grandchildStructure,
                parentFrame = childFrame
            )
    }

    for (childStructure in this.children)
        process(
            childStructure = childStructure,
            parentFrame = rootFrame,
        )

    return StructureConversionResult(
        rootFrame = rootFrame,
        allFrames = allFrames,
    )
}