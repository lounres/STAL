package com.lounres.gradle.feature

import java.io.File


internal interface SubdirectoryBuilderInformation {
    val tags: List<String>
    val dir: File?
    fun decide(dir: File): Boolean
}
internal fun SubdirectoryBuilderInformation(tags: List<String>, dir: File?, decide: (File) -> Boolean): SubdirectoryBuilderInformation =
    object: SubdirectoryBuilderInformation {
        override val tags: List<String> = tags
        override val dir: File? = dir
        override fun decide(dir: File): Boolean = decide(dir)
    }

internal interface  ProjectStructureInformation {
    val children: Map<String, ChildProjectStructureInformation>
    val subprojectsDefaultDirectory: File?
    val subdirBuilders: List<SubdirectoryBuilderInformation>

    val tags: Set<String>
}

internal interface ChildProjectStructureInformation: ProjectStructureInformation {
    val parent: ProjectStructureInformation
    val directory: File?
}