package com.lounres.gradle.feature

internal interface ProjectStructureSpec {
    val projectPath: List<String>
    val projectFullName: String get() = projectPath.joinToString(separator = ":", prefix = ":")

    val children: List<ChildProjectStructureSpec>

    val tags: Set<String>
}

internal interface ChildProjectStructureSpec: ProjectStructureSpec {
    val parent: ProjectStructureSpec

    val name: String get() = projectPath.last()
}

internal abstract class ProjectStructureSpecBuilder: ProjectStructureSpec {
    override val children: MutableList<ChildProjectStructureSpecBuilder> = mutableListOf()
    abstract override val tags: MutableSet<String>

    fun child(name: String, tags: MutableSet<String>) = ChildProjectStructureSpecBuilder(
        parent = this,
        projectPath = projectPath + name,
        tags = tags
    ).also { children.add(it) }
}

internal class RootProjectStructureSpecBuilder(
    override val tags: MutableSet<String>
) : ProjectStructureSpecBuilder() {
    override val projectPath: List<String> = emptyList()
}

internal class ChildProjectStructureSpecBuilder(
    override val parent: ProjectStructureSpecBuilder,
    override val projectPath: List<String>,
    override val tags: MutableSet<String>,
): ProjectStructureSpecBuilder(), ChildProjectStructureSpec
