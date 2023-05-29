/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.collector.lookUp

import com.lounres.gradle.stal.ProjectFrame
import com.lounres.gradle.stal.dsl.LookUpDsl
import com.lounres.gradle.stal.dsl.ProjectFramePredicate
import org.gradle.api.Project


internal class LookUpDslImpl: LookUpDsl {
    internal var projectsFramesListProvider: List<ProjectFrame>? = null

    override val allProjectFrames: List<ProjectFrame>
        get() = projectsFramesListProvider ?: error("List of projects still has not been computed")
    override val allProject: List<Project>
        get() = projectsFramesListProvider?.map { it.project } ?: error("List of projects still has not been computed")

    override fun projectFramesThat(predicate: ProjectFramePredicate): List<ProjectFrame> =
        projectsFramesListProvider?.filter(predicate) ?: error("List of projects still has not been computed")

    override fun projectsThat(predicate: ProjectFramePredicate): List<Project> =
        projectsFramesListProvider?.filter(predicate)?.map { it.project } ?: error("List of projects still has not been computed")
}