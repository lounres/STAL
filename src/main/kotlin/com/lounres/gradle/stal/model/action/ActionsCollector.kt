/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.model.action

import com.lounres.gradle.stal.dsl.Action
import com.lounres.gradle.stal.dsl.ActionDsl
import com.lounres.gradle.stal.dsl.Predicate
import com.lounres.gradle.stal.dsl.WheneverDslAtom


internal data class ActionDescription(
    val predicate: Predicate,
    val action: Action
)

internal interface ActionsCollector {
    val actionDescriptions: List<ActionDescription>
}

internal class ActionsCollectorImpl: ActionDsl, ActionsCollector {
    override val actionDescriptions: MutableList<ActionDescription> = mutableListOf()

    override fun on(tag: String, action: Action) {
        actionDescriptions += ActionDescription({ has(tag) }, action)
    }
    override fun onAny(vararg tags: String, action: Action) {
        actionDescriptions += ActionDescription({ hasAnyOf(*tags) }, action)
    }
    override fun onAll(vararg tags: String, action: Action) {
        actionDescriptions += ActionDescription({ hasAllOf(*tags) }, action)
    }
    override infix fun WheneverDslAtom.imply(action: Action) {
        this@ActionsCollectorImpl.actionDescriptions += ActionDescription(predicate, action)
    }
}