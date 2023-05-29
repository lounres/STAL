/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.processing.tag

import com.lounres.gradle.stal.ProjectFrame
import com.lounres.gradle.stal.collector.action.ActionDescription
import com.lounres.gradle.stal.collector.tag.TagDependency
import com.lounres.gradle.stal.dsl.ProjectAction


internal fun Set<String>.topSort(tagDependencies: List<TagDependency>): List<String> {
    val size = size
    val allTags = this.toList()
    val result = IntArray(size)
    var resultSize = 0
    val tagsIndices = allTags.mapIndexed { index, tag -> tag to index }.toMap()
    val edges: List<List<Int>> = buildList<MutableList<Int>>(size) {
        repeat(size) { add(mutableListOf()) }
        for ((dependent, dependency) in tagDependencies) {
            this[tagsIndices[dependent]!!] += tagsIndices[dependency]!!
        }
    }
    val marks = ByteArray(size) { 0 }

    fun visit(node: Int) {
        when (marks[node]) {
            2.toByte() -> return
            1.toByte() -> throw IllegalArgumentException("There is a dependency cycle.")
        }

        marks[node] = 1

        edges[node].forEach { visit(it) }

        marks[node] = 2
        result[resultSize] = node
        resultSize++
    }

    for (index in this.indices) if (marks[index] != 2.toByte()) visit(index)

    return List(size) { allTags[result[it]] }
}

internal fun process(tagDependencies: List<TagDependency>, actionDescriptions: List<ActionDescription>, frames: List<ProjectFrame>) {
    val allTags = buildSet {
        tagDependencies.forEach {
            add(it.dependentTag)
            add(it.dependencyTag)
        }
        actionDescriptions.forEach {
            add(it.tag)
        }
    }

    val allTagsSorted = allTags.topSort(tagDependencies)
    val tagsIndices = allTagsSorted.mapIndexed { index, tag -> tag to index }.toMap()
    val reversedEdges: List<List<Int>> = buildList<MutableList<Int>>(allTagsSorted.size) {
        repeat(allTagsSorted.size) { add(mutableListOf()) }
        for ((dependent, dependency) in tagDependencies) {
            this[tagsIndices[dependency]!!] += tagsIndices[dependent]!!
        }
    }
    val dependants: List<Set<String>> = buildList(allTagsSorted.size) {
        repeat(allTagsSorted.size) { add(setOf()) }
        for (i in allTagsSorted.lastIndex downTo 0)
            this[i] = buildSet {
                add(allTagsSorted[i])
                for (dependant in reversedEdges[i])
                    addAll(this@buildList[dependant])
            }
    }
    val actions: List<List<ProjectAction>> = buildList<MutableList<ProjectAction>>(allTagsSorted.size) {
        repeat(allTagsSorted.size) { add(mutableListOf()) }
        for ((tag, action) in actionDescriptions) this[tagsIndices[tag]!!] += action
    }

    for (frame in frames) for (i in allTagsSorted.indices)
        if ((frame.tags intersect dependants[i]).isNotEmpty())
            actions[i].forEach { frame.project.it() }
}