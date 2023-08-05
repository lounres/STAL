/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.processing.tag

import com.lounres.gradle.stal.MutableProjectFrame
import com.lounres.gradle.stal.collector.action.ActionDescription
import com.lounres.gradle.stal.collector.tag.TagPredicate


internal fun processTags(
    projectFrames: List<MutableProjectFrame>,
    tagPredicates: List<TagPredicate>,
) {
    var thereIsChange: Boolean
    do {
        thereIsChange = false
        for ((tag, predicate) in tagPredicates) for (frame in projectFrames) {
            if (tag !in frame.tags && frame.predicate()) {
                frame.tags += tag
                thereIsChange = true
            }
        }
    } while (thereIsChange)
}

internal fun applyActions(
    projectFrames: List<MutableProjectFrame>,
    actionsDescriptions: List<ActionDescription>,
) {
    for (frame in projectFrames) for ((predicate, action) in actionsDescriptions)
        if (frame.predicate()) frame.project.action()
}