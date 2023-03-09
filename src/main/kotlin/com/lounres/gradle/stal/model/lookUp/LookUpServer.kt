/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.model.lookUp

import com.lounres.gradle.stal.dsl.LookUpDsl
import com.lounres.gradle.stal.dsl.Predicate
import com.lounres.gradle.stal.dsl.ProjectFrame
import org.gradle.api.Project


internal class LookUpServer: LookUpDsl {
    internal var projectsFramesListProvider: List<ProjectFrame<Project>>? = null

    override val allProjectFrames: List<ProjectFrame<Project>>
        get() = projectsFramesListProvider ?: error("List of projects still has not been computed")
    override val allProject: List<Project>
        get() = projectsFramesListProvider?.map { it.project } ?: error("List of projects still has not been computed")

    override fun projectFramesThat(predicate: Predicate): List<ProjectFrame<Project>> =
        projectsFramesListProvider?.filter(predicate) ?: error("List of projects still has not been computed")

    override fun projectsThat(predicate: Predicate): List<Project> =
        projectsFramesListProvider?.filter(predicate)?.map { it.project } ?: error("List of projects still has not been computed")
}