package com.lounres.gradle.feature

import org.gradle.api.Project


public class FeaturesManagementExtension {
    internal var rootProjectSpec: ProjectStructureSpec? = null

    @PublishedApi
    internal val tagAssigners: MutableList<TagAssigner> = mutableListOf()
    internal val tagProcessors: MutableMap<String, MutableList<Project.() -> Unit>> = mutableMapOf()

    @PublishedApi
    internal val tagAssignersCollector: TagAssignersCollector = TagAssignersCollector()
    public inner class TagAssignersCollector {
        public infix fun String.since( block: ProjectDescriptor.() -> Boolean) {
            tagAssigners.add(TagAssigner(this, block))
        }
    }

    @PublishedApi
    internal val tagProcessorsCollector: TagProcessorsCollector = TagProcessorsCollector()
    public inner class TagProcessorsCollector {
        public fun on(vararg tags: String, action: Project.() -> Unit) {
            for (tag in tags) tagProcessors.getOrPut(tag) { mutableListOf() }.add(action)
        }
    }

    public fun tagRules(block: TagAssignersCollector.() -> Unit): Unit = tagAssignersCollector.block()
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

