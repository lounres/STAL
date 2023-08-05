/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.plugin

import com.lounres.gradle.stal.collector.action.ActionDescription
import com.lounres.gradle.stal.collector.action.ActionsCollectorImpl
import com.lounres.gradle.stal.collector.lookUp.LookUpDslImpl
import com.lounres.gradle.stal.collector.structure.RootDescriptionNode
import com.lounres.gradle.stal.collector.structure.StructureDslImpl
import com.lounres.gradle.stal.collector.tag.TagDependencyCollectorImpl
import com.lounres.gradle.stal.collector.tag.TagPredicate
import com.lounres.gradle.stal.dsl.*
import com.lounres.gradle.stal.processing.structure.resolveProjectHierarchy
import com.lounres.gradle.stal.processing.structure.convertToMutableProjectFrame
import com.lounres.gradle.stal.processing.tag.applyActions
import com.lounres.gradle.stal.processing.tag.processTags
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.add


internal class StalDslImpl(
    structureDslImpl: StructureDslImpl = StructureDslImpl(),
    tagDslImpl: TagDependencyCollectorImpl = TagDependencyCollectorImpl(),
    actionsCollector: ActionsCollectorImpl = ActionsCollectorImpl(),
    lookUpDslImpl: LookUpDslImpl = LookUpDslImpl(),
): StalSettingsDsl, StalRootProjectDsl {
    override val structure: StructureDsl = structureDslImpl
    val rootStructureVertex: RootDescriptionNode = structureDslImpl.vertex

    override val tag: TagDsl = tagDslImpl
    override fun tag(block: TagDsl.() -> Unit) { tag.block() }
    val tagPredicates: List<TagPredicate> = tagDslImpl.tagPredicates

    override val action: ActionDsl = actionsCollector
    override fun action(block: ActionDsl.() -> Unit) { action.block() }
    val actionDescriptions: List<ActionDescription> = actionsCollector.actionDescriptions

    override val lookUp: LookUpDslImpl = lookUpDslImpl
}

public class StalSettingsPlugin: Plugin<Settings> {
    override fun apply(settings: Settings) {
        val stalDslImpl = StalDslImpl()
        settings.extensions.add<StalSettingsDsl>("stal", stalDslImpl)

        val gradle = settings.gradle
        gradle.settingsEvaluated {
            val rootStructureNode = stalDslImpl.rootStructureVertex.resolveProjectHierarchy()

            gradle.projectsLoaded {
                rootProject {
                    logger.info("[STAL] Structure is processed. Root project is loaded. Adding STAL extension to root project.")
                    extensions.add<StalRootProjectDsl>("stal", stalDslImpl)

                    logger.info("[STAL] Converting structure nodes to project frames.")
                    val (rootFrame, allFrames) = rootStructureNode.convertToMutableProjectFrame()

                    logger.info("[STAL] Making look-up accessible.")
                    stalDslImpl.lookUp.projectsFramesListProvider = allFrames

                    afterEvaluate {
                        logger.info("[STAL] Root project is evaluated. Processing tags.")
                        processTags(
                            allFrames,
                            stalDslImpl.tagPredicates,
                        )
                        logger.info("[STAL] Applying actions.")
                        applyActions(
                            allFrames,
                            stalDslImpl.actionDescriptions,
                        )
                    }
                }
            }
        }
    }
}