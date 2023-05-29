/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.collector.tag

import com.lounres.gradle.stal.dsl.TagDsl


internal data class TagDependency(
    val dependentTag: String,
    val dependencyTag: String,
)

internal interface TagDependencyCollector {
    val tagDependencies: List<TagDependency>
}

internal class TagDependencyCollectorImpl: TagDsl, TagDependencyCollector {
    override val tagDependencies: MutableList<TagDependency> = mutableListOf()

    override fun String.dependsOn(vararg tags: String) {
        tagDependencies.addAll(tags.map { TagDependency(this, it) })
    }
    override fun String.dependsOn(tags: Collection<String>) {
        tagDependencies.addAll(tags.map { TagDependency(this, it) })
    }
    override fun String.dependsOn(tag: String) {
        tagDependencies.add(TagDependency(this, tag))
    }
}