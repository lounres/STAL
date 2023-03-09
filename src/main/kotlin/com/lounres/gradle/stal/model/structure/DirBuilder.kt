/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.model.structure

import com.lounres.gradle.stal.dsl.DirDsl
import org.gradle.api.initialization.ProjectDescriptor
import java.io.File


internal typealias StructureDslImpl = RootDirBuilder
internal abstract class DirBuilder<V: StructureVertexInformationBuilder>: DirDsl {
    abstract val vertex: V

    // Building
    override operator fun String.invoke(tags: Collection<String>, subScope: (DirDsl.() -> Unit)?) {
        val childDir = ExplicitChildDirBuilder(this)
        vertex.explicitChildren += childDir.vertex
        childDir.vertex.tags += tags
        if (subScope != null) childDir.subScope()
    }
    override operator fun String.invoke(vararg tags: String, subScope: (DirDsl.() -> Unit)?) {
        val childDir = ExplicitChildDirBuilder(this)
        vertex.explicitChildren += childDir.vertex
        childDir.vertex.tags += tags
        if (subScope != null) childDir.subScope()
    }
    override fun subdirs(tags: List<String>, searchDir: File?, includeIf: (File) -> Boolean, subScope: (DirDsl.() -> Unit)?) {
        val childDir = SubDirChildDirBuilder(searchDir, includeIf)
        vertex.subdirChildren += childDir.vertex
        childDir.vertex.tags += tags
        if (subScope != null) childDir.subScope()
    }
    override fun subdirs(vararg tags: String, searchDir: File?, includeIf: (File) -> Boolean, subScope: (DirDsl.() -> Unit)?) {
        val childDir = SubDirChildDirBuilder(searchDir, includeIf)
        vertex.subdirChildren += childDir.vertex
        childDir.vertex.tags += tags
        if (subScope != null) childDir.subScope()

    }
    // Tagging
    override fun taggedWith(vararg tags: String) {
        vertex.tags += tags
    }
    // Configuring
    override fun descriptor(configuration: ProjectDescriptor.() -> Unit) {
        vertex.descriptorHandlers += configuration
    }
}

internal class RootDirBuilder: DirBuilder<RootStructureVertexInformationBuilder>() {
    override val vertex = RootStructureVertexInformationBuilder()
}

internal class ExplicitChildDirBuilder(name: String): DirBuilder<ExplicitChildStructureVertexInformationBuilder>() {
    override val vertex = ExplicitChildStructureVertexInformationBuilder(name)
}

internal class SubDirChildDirBuilder(searchDir: File?, includeIf: (File) -> Boolean): DirBuilder<SubDirChildStructureVertexInformationBuilder>() {
    override val vertex = SubDirChildStructureVertexInformationBuilder(searchDir, includeIf)
}