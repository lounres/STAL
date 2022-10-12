import com.lounres.gradle.feature.ProjectStructureDsl
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.getByName


public val Settings.structuring: ProjectStructureDsl get() = extensions.getByName<ProjectStructureDsl>("structuring")
public fun Settings.structuring(block: ProjectStructureDsl.() -> Unit): Unit = extensions.configure("structuring", block)