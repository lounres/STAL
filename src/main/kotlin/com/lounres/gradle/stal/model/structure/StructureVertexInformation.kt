/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.model.structure

import org.gradle.api.initialization.ProjectDescriptor
import java.io.File


// FIXME: Make me `sealed`
internal interface StructureVertexInformation {
    val tags: Set<String>
    val descriptorHandlers: Iterable<ProjectDescriptor.() -> Unit>
    val explicitChildren: Set<ExplicitChildStructureVertexInformation>
    val subdirChildren: Set<SubDirChildStructureVertexInformation>
}

internal interface RootStructureVertexInformation: StructureVertexInformation

internal interface ExplicitChildStructureVertexInformation : StructureVertexInformation {
    val name: String
}

internal interface SubDirChildStructureVertexInformation : StructureVertexInformation {
    val searchDir: File?
    val includeIf: (File) -> Boolean
}

// FIXME: Make me `sealed`
internal abstract class StructureVertexInformationBuilder {
    val tags: MutableSet<String> = mutableSetOf()
    val descriptorHandlers: MutableList<ProjectDescriptor.() -> Unit> = mutableListOf()
    val explicitChildren: MutableSet<ExplicitChildStructureVertexInformation> = mutableSetOf()
    val subdirChildren: MutableSet<SubDirChildStructureVertexInformation> = mutableSetOf()
}

internal class RootStructureVertexInformationBuilder: StructureVertexInformationBuilder(), RootStructureVertexInformation

internal class ExplicitChildStructureVertexInformationBuilder(
    override val name: String,
): StructureVertexInformationBuilder(), ExplicitChildStructureVertexInformation

internal class SubDirChildStructureVertexInformationBuilder(
    override val searchDir: File?,
    override val includeIf: (File) -> Boolean,
): StructureVertexInformationBuilder(), SubDirChildStructureVertexInformation