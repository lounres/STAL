package com.lounres.gradle.feature

import org.gradle.api.Project


public interface ProjectDescriptor {
    public val project: Project

    public val parent: ProjectDescriptor?
    public val children: List<ChildProjectDescriptor>

    public val tags: Set<String>
    public fun hasTags(vararg tags: String): Boolean = tags.all { it in this.tags }
}

public interface RootProjectDescriptor: ProjectDescriptor {
    public override val parent: Nothing? get() = null
}

public interface ChildProjectDescriptor: ProjectDescriptor {
    public override val parent: ProjectDescriptor
}

public interface ProjectHandler: ProjectDescriptor {
    override val tags: MutableSet<String>
    public fun addTag(tag: String) {
        tags.add(tag)
    }
}

internal abstract class ProjectHandlerBuilder(
    override val tags: MutableSet<String>
): ProjectHandler {
    override val children: MutableList<ChildProjectHandlerBuilder> = mutableListOf()
    fun child(project: Project, tags: MutableSet<String>) = ChildProjectHandlerBuilder(
        project = project,
        parent = this,
        tags = tags
    ).also { children.add(it) }
}

internal class RootProjectHandlerBuilder(
    override val project: Project,
    tags: MutableSet<String>,
) : ProjectHandlerBuilder(tags), RootProjectDescriptor

internal class ChildProjectHandlerBuilder(
    override val project: Project,
    override val parent: ProjectHandlerBuilder,
    tags: MutableSet<String>,
) : ProjectHandlerBuilder(tags), ChildProjectDescriptor