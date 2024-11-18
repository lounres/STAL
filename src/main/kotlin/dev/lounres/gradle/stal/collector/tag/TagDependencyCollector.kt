/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package dev.lounres.gradle.stal.collector.tag

import dev.lounres.gradle.stal.dsl.ProjectFramePredicate
import dev.lounres.gradle.stal.dsl.TagDsl


internal data class TagPredicate(
    val tag: String,
    val predicate: ProjectFramePredicate
)

internal interface TagDependencyCollector {
    val tagPredicates: List<TagPredicate>
}

internal class TagDependencyCollectorImpl: TagDsl, TagDependencyCollector {
    override val tagPredicates: MutableList<TagPredicate> = mutableListOf()

    override fun String.since(predicate: ProjectFramePredicate) {
        tagPredicates += TagPredicate(this, predicate)
    }
}