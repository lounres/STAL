/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package dev.lounres.gradle.stal.collector.action

import dev.lounres.gradle.stal.dsl.ProjectAction
import dev.lounres.gradle.stal.dsl.ActionDsl
import dev.lounres.gradle.stal.dsl.CaseDslAtom
import dev.lounres.gradle.stal.dsl.ProjectFramePredicate


internal data class ActionDescription(
    val predicate: ProjectFramePredicate,
    val action: ProjectAction
)

internal interface ActionsCollector {
    val actionDescriptions: List<ActionDescription>
}

internal class ActionsCollectorImpl: ActionDsl, ActionsCollector {
    override val actionDescriptions: MutableList<ActionDescription> = mutableListOf()

    override fun String.does(action: ProjectAction) {
        actionDescriptions += ActionDescription(predicate = { has(this@String) }, action = action)
    }
    override fun String.invoke(action: ProjectAction) {
        actionDescriptions += ActionDescription(predicate = { has(this@String) }, action = action)
    }

    override fun CaseDslAtom.implies(action: ProjectAction) {
        this@ActionsCollectorImpl.actionDescriptions += ActionDescription(predicate = predicate, action = action)
    }
}