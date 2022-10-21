package com.lounres.gradle.feature

import org.gradle.api.Project


public class FeaturesManagementExtension {
    internal var rootProjectSpec: ProjectStructureSpec? = null

    internal val tagAssignmentSettings : TagAssignmentSettings get() = tagAssignmentSettingsBuilder
    internal interface TagAssignmentSettings {
        val tagAssigners: List<TagAssigner>
    }
    private val tagAssignmentSettingsBuilder = TagAssignmentSettingsBuilder()
    private class TagAssignmentSettingsBuilder: TagAssignmentSettings {
        override val tagAssigners: MutableList<TagAssigner> = mutableListOf()
    }

    internal val tagProcessingSettings : TagProcessingSettings get() = tagProcessingSettingsBuilder
    internal interface TagProcessingSettings {
        val tagProcessors: Map<String, MutableList<Project.() -> Unit>>
    }
    private val tagProcessingSettingsBuilder = TagProcessingSettingsBuilder()
    private class TagProcessingSettingsBuilder: TagProcessingSettings {
        override val tagProcessors: MutableMap<String, MutableList<Project.() -> Unit>> = mutableMapOf()
    }

    private val tagAssignersCollector: TagAssignersCollector = TagAssignersCollector()
    public inner class TagAssignersCollector {
        public infix fun String.since( block: ProjectDescriptor.() -> Boolean) {
            tagAssignmentSettingsBuilder.tagAssigners.add(TagAssigner(this, block))
        }
    }

    private val tagProcessorsCollector: TagProcessorsCollector = TagProcessorsCollector()
    public inner class TagProcessorsCollector {
        public fun on(vararg tags: String, action: Project.() -> Unit) {
            for (tag in tags) tagProcessingSettingsBuilder.tagProcessors.getOrPut(tag) { mutableListOf() }.add(action)
        }
    }

    public fun tagsAssignment(block: TagAssignersCollector.() -> Unit): Unit = tagAssignersCollector.block()
    public fun features(block: TagProcessorsCollector.() -> Unit): Unit = tagProcessorsCollector.block()
}

internal interface TagAssigner {
    val tag: String
    fun check(project: ProjectDescriptor): Boolean
}
@PublishedApi
internal fun TagAssigner(tag: String, decide: ProjectDescriptor.() -> Boolean): TagAssigner = object: TagAssigner {
    override val tag: String = tag
    override fun check(project: ProjectDescriptor): Boolean = project.decide()
}

