/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.processing.structure

import com.lounres.gradle.stal.collector.structure.DescriptionNode
import com.lounres.gradle.stal.collector.structure.ExplicitChildDescriptionNode
import com.lounres.gradle.stal.collector.structure.RootDescriptionNode
import com.lounres.gradle.stal.collector.structure.SubdirChildDescriptionNode
import com.lounres.gradle.stal.toGradleName
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import java.io.File
import java.io.FileFilter


// region Models
internal interface StructureNode {
    val fullNameParts: List<String>
    val fullName: String get() = fullNameParts.toGradleName()
    val tags: Set<String>
    val children: Set<ChildStructureNode>
    val project: ProjectDescriptor
}

internal interface RootStructureNode : StructureNode

internal interface ChildStructureNode : StructureNode {
    val parent: StructureNode
}

internal interface StructureNodeBuilder: StructureNode {
    override val children: MutableSet<ChildStructureNode>
    override val project: ProjectDescriptor
}

internal class RootStructureNodeBuilder(
    override val fullNameParts: List<String>,
    override val tags: Set<String>,
    override val children: MutableSet<ChildStructureNode> = mutableSetOf(),
    override val project: ProjectDescriptor,
) : RootStructureNode, StructureNodeBuilder

internal class ChildStructureNodeBuilder(
    override val fullNameParts: List<String>,
    override val tags: Set<String>,
    override val parent: StructureNode,
    override val children: MutableSet<ChildStructureNode> = mutableSetOf(),
    override val project: ProjectDescriptor,
) : ChildStructureNode, StructureNodeBuilder
// endregion

// region Resolution

internal data class ExplicitChildDescriptionAndStructure(
    val description: ExplicitChildDescriptionNode,
    val parentNode: StructureNodeBuilder,
    val parentDefaultIncludeIf: (File) -> Boolean,
)

internal data class SubdirChildDescriptionAndStructure(
    val description: SubdirChildDescriptionNode,
    val parentNode: StructureNodeBuilder,
    val parentDefaultIncludeIf: (File) -> Boolean,
)

context(Settings)
internal fun RootDescriptionNode.resolve(): RootStructureNode {
    val allNodes = mutableListOf<StructureNode>()

    val rootStructureNode = RootStructureNodeBuilder(
        fullNameParts = listOf(),
        tags = this.tags.toMutableSet(),
        project = rootProject
    )
    allNodes += rootStructureNode
    this.descriptorHandlers.forEach { rootStructureNode.project.it() }
    val rootDefaultIncludeIf = this.defaultIncludeIf ?: { true }

    val explicitModulesQueue = ArrayDeque<ExplicitChildDescriptionAndStructure>()
    val subdirModulesQueue = ArrayDeque<SubdirChildDescriptionAndStructure>()

    fun processChildren(
        parentDescription: DescriptionNode,
        parentNode: StructureNodeBuilder,
        parentDefaultIncludeIf: (File) -> Boolean,
    ) {
        explicitModulesQueue +=
            parentDescription.explicitChildren.map {
                ExplicitChildDescriptionAndStructure(
                    description = it,
                    parentNode = parentNode,
                    parentDefaultIncludeIf = parentDefaultIncludeIf,
                )
            }

        subdirModulesQueue +=
            parentDescription.subdirChildren.map {
                SubdirChildDescriptionAndStructure(
                    description = it,
                    parentNode = parentNode,
                    parentDefaultIncludeIf = parentDefaultIncludeIf,
                )
            }
    }

    processChildren(
        parentDescription = this,
        parentNode = rootStructureNode,
        parentDefaultIncludeIf = rootDefaultIncludeIf
    )

    while (explicitModulesQueue.isNotEmpty() || subdirModulesQueue.isNotEmpty()) {
        while (explicitModulesQueue.isNotEmpty()) {
            val (
                childDescription,
                parentNode,
                parentDefaultIncludeIf,
            ) = explicitModulesQueue.removeFirst()

            val childFullNameParts = parentNode.fullNameParts + childDescription.name
            val childFullName = childFullNameParts.joinToString(separator = "") { ":$it" }
            val childDefaultIncludeIf = childDescription.defaultIncludeIf ?: parentDefaultIncludeIf

            include(childFullName)
            val childProjectDescriptor = project(childFullName)
            childDescription.descriptorHandlers.forEach { childProjectDescriptor.it() }
            childProjectDescriptor.projectDir.mkdirs()
            val childNode = ChildStructureNodeBuilder(
                fullNameParts = childFullNameParts,
                tags = childDescription.tags.toMutableSet(),
                parent = parentNode,
                children = mutableSetOf(),
                project = childProjectDescriptor,
            )
            allNodes += childNode
            parentNode.children += childNode

            processChildren(
                parentDescription = childDescription,
                parentNode = childNode,
                parentDefaultIncludeIf = childDefaultIncludeIf
            )
        }

        while (subdirModulesQueue.isNotEmpty()) {
            val (
                subdirDescription,
                parentNode,
                parentDefaultIncludeIf,
            ) = subdirModulesQueue.removeFirst()

            val searchDir = subdirDescription.searchDir ?: parentNode.project.projectDir
            val childDefaultIncludeIf = subdirDescription.defaultIncludeIf ?: parentDefaultIncludeIf
            val includeIf = subdirDescription.includeIf ?: childDefaultIncludeIf
            searchDir.listFiles(FileFilter { it.isDirectory && includeIf(it) })!!.forEach { childDir ->
                val childFullNameParts = parentNode.fullNameParts + childDir.name
                val childFullName = childFullNameParts.joinToString(separator = "") { ":$it" }

                include(childFullName)
                val childProjectDescriptor = project(childFullName)
                subdirDescription.descriptorHandlers.forEach { childProjectDescriptor.it() }
                val childNode = ChildStructureNodeBuilder(
                    fullNameParts = childFullNameParts,
                    tags = subdirDescription.tags.toMutableSet(),
                    parent = parentNode,
                    children = mutableSetOf(),
                    project = childProjectDescriptor,
                )
                allNodes += childNode
                parentNode.children += childNode

                processChildren(
                    parentDescription = subdirDescription,
                    parentNode = childNode,
                    parentDefaultIncludeIf = childDefaultIncludeIf
                )
            }
        }
    }

    return rootStructureNode
}
// endregion