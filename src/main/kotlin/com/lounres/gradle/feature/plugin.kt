package com.lounres.gradle.feature

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.add
import java.io.File


public class FeatureSettingsPlugin: Plugin<Settings> {
    override fun apply(settings: Settings) {
        val structuringExtension = ProjectStructureInformationBuilder()
        settings.extensions.add<ProjectStructureDsl>("structuring", structuringExtension)
        val featuresManagementExtension = FeaturesManagementExtension()
        settings.extensions.add("featuresManagement", featuresManagementExtension)
        settings.gradle.settingsEvaluated {
            featuresManagementExtension.rootProjectSpec = processStructureInformation(structuringExtension)
        }
        settings.gradle.projectsLoaded {
            rootProject.extensions.add("featuresManagement", featuresManagementExtension)
            rootProject.afterEvaluate {
                val rootProjectSpec: ProjectStructureSpec = featuresManagementExtension.rootProjectSpec
                    ?: error("Feature Plugin internal error: project handlers list still is not initialised")

                val projectHandlers = processSctructureSpecs(rootProjectSpec)
                processTagAssignment(projectHandlers, featuresManagementExtension.tagAssigners)
                processTagProcessing(projectHandlers, featuresManagementExtension.tagProcessors)
            }
        }
    }
}

internal fun Settings.processStructureInformation(rootProjectStructure: ProjectStructureInformation): ProjectStructureSpec {
    abstract class SubdirectoryBuilder {
        abstract val parent: ProjectStructureSpecBuilder
        abstract val dir: File
        abstract val tags: List<String>
        abstract fun decide(dir: File): Boolean
    }

    val occupiedPaths = mutableMapOf<String, ProjectStructureSpecBuilder>()
    val occupiedSubdirsPaths = mutableMapOf<String, ProjectStructureSpecBuilder>()
    val rootProjectSpec = RootProjectStructureSpecBuilder(rootProjectStructure.tags.toMutableSet())
    val subdirsToProcess = mutableListOf<SubdirectoryBuilder>()

    fun ProjectStructureSpecBuilder.processNamedFrom(projectStructure: ProjectStructureInformation) {
        val projectDir: File = project(projectFullName).projectDir
        subdirsToProcess.addAll(
            projectStructure.subdirBuilders.map {
                object: SubdirectoryBuilder() {
                    override val parent: ProjectStructureSpecBuilder = this@processNamedFrom
                    override val dir: File = it.dir ?: projectStructure.subprojectsDefaultDirectory ?: projectDir
                    override val tags: List<String> = it.tags
                    override fun decide(dir: File): Boolean = it.decide(dir)
                }
            }
        )
        for ((name, childStructure) in projectStructure.children) {
            val childSpec = child(name = name, tags = childStructure.tags.toMutableSet())
            include(childSpec.projectFullName)
            val childDir = childStructure.directory ?: (projectStructure.subprojectsDefaultDirectory ?: projectDir).resolve(name)
            require(childDir.absolutePath !in occupiedPaths) { "Directory ${childDir.absolutePath} is already occupied." }
            occupiedPaths[childDir.absolutePath] = childSpec
            project(childSpec.projectFullName).projectDir = childDir
            childSpec.processNamedFrom(childStructure)
        }
    }
    rootProjectSpec.processNamedFrom(rootProjectStructure)

    for (subdirBuilder in subdirsToProcess)
        subdirBuilder.dir
            .listFiles { file -> file.isDirectory && file.absolutePath !in occupiedPaths && subdirBuilder.decide(file) }
            ?.forEach {
                val childSpec = occupiedSubdirsPaths.getOrPut(it.absolutePath) { subdirBuilder.parent.child(it.name, tags = mutableSetOf()) }
                require(childSpec is ChildProjectStructureSpec && childSpec.parent == subdirBuilder.parent) { "Directory $it is covered by 'subdirs' scopes of different parent projects" }
                childSpec.tags.addAll(subdirBuilder.tags)
                include(childSpec.projectFullName)
                project(childSpec.projectFullName).projectDir = it
            }

    return rootProjectSpec
}

internal fun Project.processSctructureSpecs(rootProjectSpec: ProjectStructureSpec): List<ProjectHandler> {
    val projectHandlers = mutableListOf<ProjectHandler>()

    val rootProjectHandler = RootProjectHandlerBuilder(
        project = this,
        tags = rootProjectSpec.tags.toMutableSet()
    )
    fun ProjectHandlerBuilder.processChildrenWith(projectSpec: ProjectStructureSpec) {
        projectHandlers.add(this)
        for (childSpec in projectSpec.children)
            child(project = project(childSpec.projectFullName), tags = childSpec.tags.toMutableSet()).processChildrenWith(childSpec)
    }
    rootProjectHandler.processChildrenWith(rootProjectSpec)

    return projectHandlers
}

internal fun processTagAssignment(projectHandlers: List<ProjectHandler>, assigners: List<TagAssigner>) {
    val ticksLimit = projectHandlers.size * assigners.size

    var handlerIndex = 0
    var assignerIndex = 0
    var ticksWithoutChanges = 0

    while (true) {
        if (ticksWithoutChanges == ticksLimit) break

        val handler = projectHandlers[handlerIndex]
        val assigner = assigners[assignerIndex]
        if (assigner.tag !in handler.tags && assigner.check(handler)) {
            handler.addTag(assigner.tag)
            ticksWithoutChanges = 0
        } else {
            ticksWithoutChanges++
        }

        handlerIndex++
        if (handlerIndex == projectHandlers.size) {
            handlerIndex = 0
            assignerIndex++
            if (assignerIndex == assigners.size) assignerIndex = 0
        }
    }
}

internal fun processTagProcessing(projectDescriptors: List<ProjectDescriptor>, tagProcessors: Map<String, List<Project.() -> Unit>>) {
    for (projectDescriptor in projectDescriptors)
        for (tag in projectDescriptor.tags)
            for (processor in tagProcessors[tag] ?: emptyList())
                projectDescriptor.project.processor()
}