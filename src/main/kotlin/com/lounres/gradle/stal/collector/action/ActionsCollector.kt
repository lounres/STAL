/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.collector.action

import com.lounres.gradle.stal.dsl.ProjectAction
import com.lounres.gradle.stal.dsl.ActionDsl


internal data class ActionDescription(
    val tag: String,
    val action: ProjectAction
)

internal interface ActionsCollector {
    val actionDescriptions: List<ActionDescription>
}

internal class ActionsCollectorImpl: ActionDsl, ActionsCollector {
    override val actionDescriptions: MutableList<ActionDescription> = mutableListOf()

    override fun String.does(action: ProjectAction) {
        actionDescriptions += ActionDescription(tag = this, action = action)
    }
    override fun String.invoke(action: ProjectAction) {
        actionDescriptions += ActionDescription(tag = this, action = action)
    }
}