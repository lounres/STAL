/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package dev.lounres.gradle.stal.collector.structure

import org.gradle.api.initialization.ProjectDescriptor
import java.io.File


internal interface DescriptionNode {
    val tags: Set<String>
    val defaultIncludeIf: ((File) -> Boolean)?
    val descriptorHandlers: Iterable<ProjectDescriptor.() -> Unit>
    val explicitChildren: Set<ExplicitChildDescriptionNode>
    val subdirChildren: Set<SubdirChildDescriptionNode>
}

internal interface RootDescriptionNode: DescriptionNode

internal interface ExplicitChildDescriptionNode : DescriptionNode {
    val name: String
}

internal interface SubdirChildDescriptionNode : DescriptionNode {
    val searchDir: File?
    val includeIf: ((File) -> Boolean)?
}


internal abstract class DescriptionNodeBuilder: DescriptionNode {
    override val tags: MutableSet<String> = mutableSetOf()
    override var defaultIncludeIf: ((File) -> Boolean)? = null
    override val descriptorHandlers: MutableList<ProjectDescriptor.() -> Unit> = mutableListOf()
    override val explicitChildren: MutableSet<ExplicitChildDescriptionNode> = mutableSetOf()
    override val subdirChildren: MutableSet<SubdirChildDescriptionNode> = mutableSetOf()
}

internal class RootDescriptionNodeBuilder: DescriptionNodeBuilder(), RootDescriptionNode

internal class ExplicitChildDescriptionNodeBuilder(
    override val name: String,
): DescriptionNodeBuilder(), ExplicitChildDescriptionNode

internal class SubdirChildDescriptionNodeBuilder(
    override val searchDir: File?,
    override val includeIf: ((File) -> Boolean)?,
): DescriptionNodeBuilder(), SubdirChildDescriptionNode


