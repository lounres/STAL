/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.collector.structure

import com.lounres.gradle.stal.dsl.DirDsl
import org.gradle.api.initialization.ProjectDescriptor
import java.io.File


internal typealias StructureDslImpl = RootDirBuilder
internal open class DirBuilder<V: DescriptionNodeBuilder>(val vertex: V): DirDsl {
    override var defaultIncludeIf: ((File) -> Boolean)? by vertex::defaultIncludeIf

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
    override fun subdirs(tags: List<String>, searchDir: File?, includeIf: ((File) -> Boolean)?, subScope: (DirDsl.() -> Unit)?) {
        val childDir = SubdirChildDirBuilder(searchDir, includeIf)
        vertex.subdirChildren += childDir.vertex
        childDir.vertex.tags += tags
        if (subScope != null) childDir.subScope()
    }
    override fun subdirs(vararg tags: String, searchDir: File?, includeIf: ((File) -> Boolean)?, subScope: (DirDsl.() -> Unit)?) {
        val childDir = SubdirChildDirBuilder(searchDir, includeIf)
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

internal class RootDirBuilder:
    DirBuilder<RootDescriptionNodeBuilder>(RootDescriptionNodeBuilder())

internal class ExplicitChildDirBuilder(name: String):
    DirBuilder<ExplicitChildDescriptionNodeBuilder>(ExplicitChildDescriptionNodeBuilder(name))

internal class SubdirChildDirBuilder(searchDir: File?, includeIf: ((File) -> Boolean)?):
    DirBuilder<SubdirChildDescriptionNodeBuilder>(SubdirChildDescriptionNodeBuilder(searchDir, includeIf))