/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.model.tag

import com.lounres.gradle.stal.dsl.Predicate
import com.lounres.gradle.stal.dsl.TagDsl


internal data class TagAssignment(
    val tag: String,
    val predicate: Predicate
)

internal interface TagAssignmentsCollector {
    val tagAssignments: List<TagAssignment>
}

internal class TagAssignmentsCollectorImpl: TagDsl, TagAssignmentsCollector {
    override val tagAssignments: MutableList<TagAssignment> = mutableListOf()

    override fun String.since(predicate: Predicate) {
        tagAssignments += TagAssignment(this, predicate)
    }
}