package com.lounres.gradle.feature

import java.io.File


public interface ProjectStructureDsl {
    public var subprojectsDefaultDirectory: File?
    public fun tags(vararg tags: String)
    public fun project(name: String, vararg tags: String, dir: File? = null, block: ChildProjectStructureDsl.() -> Unit = {})
    public operator fun String.invoke(vararg tags: String, dir: File? = null, block: ChildProjectStructureDsl.() -> Unit = {}): Unit = project(name = this, tags = tags, dir = dir, block = block)
    public fun subdirs(vararg tags: String, searchDir: File? = null, decide: (File) -> Boolean = { true })
}

public interface ChildProjectStructureDsl: ProjectStructureDsl {
    public var directory: File?
}

internal open class ProjectStructureInformationBuilder: ProjectStructureDsl, ProjectStructureInformation {
    override val children: MutableMap<String, ChildProjectStructureInformationBuilder> = mutableMapOf()
    override var subprojectsDefaultDirectory: File? = null

    override val subdirBuilders: MutableList<SubdirectoryBuilderInformation> = mutableListOf()

    override val tags: MutableSet<String> = mutableSetOf()

    override fun tags(vararg tags: String) {
        this.tags.addAll(tags)
    }
    override fun project(name: String, vararg tags: String, dir: File?, block: ChildProjectStructureDsl.() -> Unit) {
        children
            .getOrPut(name) { ChildProjectStructureInformationBuilder(this) }
            .apply {
                this.tags.addAll(tags)
                directory = dir
                block()
            }
    }
    override fun subdirs(vararg tags: String, searchDir: File?, decide: (File) -> Boolean) {
        subdirBuilders.add(SubdirectoryBuilderInformation(tags.toList(), searchDir, decide))
    }
}

internal class ChildProjectStructureInformationBuilder(
    override val parent: ProjectStructureInformationBuilder
): ProjectStructureInformationBuilder(), ChildProjectStructureDsl, ChildProjectStructureInformation {
    override var directory: File? = null
        set(value) = when {
            value == null -> Unit
            field == null -> field = value
            else -> error("You cannot specify project's directory when it's already specified.")
        }
}