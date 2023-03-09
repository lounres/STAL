/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.plugin

import com.lounres.gradle.stal.dsl.MutableProjectFrame
import com.lounres.gradle.stal.dsl.StalRootProjectDsl
import com.lounres.gradle.stal.dsl.StalSettingsDsl
import com.lounres.gradle.stal.extensions.StalRootProjectDslImpl
import com.lounres.gradle.stal.extensions.StalSettingsDslImpl
import com.lounres.gradle.stal.model.action.ActionDescription
import com.lounres.gradle.stal.model.action.ActionsCollectorImpl
import com.lounres.gradle.stal.model.lookUp.LookUpServer
import com.lounres.gradle.stal.model.structure.StructureDslImpl
import com.lounres.gradle.stal.model.tag.TagAssignment
import com.lounres.gradle.stal.model.tag.TagAssignmentsCollectorImpl
import com.lounres.gradle.stal.plugin.rootProject.buildProjectFrameBuildersList
import com.lounres.gradle.stal.plugin.settings.settingsProjectFrameBuilders
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings


public class StalSettingsPlugin: Plugin<Settings> {
    override fun apply(settings: Settings): Unit = with(settings) {
        val structureDsl = StructureDslImpl()
        val tagDsl = TagAssignmentsCollectorImpl()
        val actionDsl = ActionsCollectorImpl()
        val lookUpDsl = LookUpServer()

        val settingsExtension: StalSettingsDsl = StalSettingsDslImpl(structureDsl, tagDsl, actionDsl)
        val rootProjectExtension: StalRootProjectDsl = StalRootProjectDslImpl(tagDsl, actionDsl, lookUpDsl)

        extensions.add("stal", settingsExtension)
        gradle.settingsEvaluated {

            val vertexInfo = structureDsl.vertex
            val settingsProjectFrame = settingsProjectFrameBuilders(settings, vertexInfo)

            gradle.projectsLoaded {
                rootProject.extensions.add("stal", rootProjectExtension)

                val buildProjectFrameList = buildProjectFrameBuildersList(rootProject, settingsProjectFrame)
                lookUpDsl.projectsFramesListProvider = buildProjectFrameList

                rootProject.afterEvaluate {
                    tagAssignation(buildProjectFrameList, tagDsl.tagAssignments)
                    actionPerforming(buildProjectFrameList, actionDsl.actionDescriptions)
                }
            }
        }
    }
}

internal fun tagAssignation(
    buildProjectFrameList: List<MutableProjectFrame<Project>>,
    tagAssignments: List<TagAssignment>,
) {
    var thereIsChange: Boolean
    do {
        thereIsChange = false
        for ((tag, predicate) in tagAssignments) for (frame in buildProjectFrameList) {
            if (tag !in frame.tags && frame.predicate()) {
                frame.tags += tag
                thereIsChange = true
            }
        }
    } while (thereIsChange)
}

internal fun actionPerforming(
    buildProjectFrameList: List<MutableProjectFrame<Project>>,
    actionDescriptions: List<ActionDescription>,
) {
    for ((predicate, action) in actionDescriptions) for (frame in buildProjectFrameList)
        if (frame.predicate()) frame.project.beforeEvaluate(action)
}
