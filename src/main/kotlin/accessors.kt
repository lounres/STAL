import com.lounres.gradle.feature.FeaturesManagementExtension
import com.lounres.gradle.feature.ProjectStructureDsl
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.getByName


public val Settings.structuring: ProjectStructureDsl get() = extensions.getByName<ProjectStructureDsl>("structuring")
public fun Settings.structuring(block: ProjectStructureDsl.() -> Unit): Unit = extensions.configure("structuring", block)

public val Settings.featuresManagement: FeaturesManagementExtension get() = extensions.getByName<FeaturesManagementExtension>("featuresManagement")
public fun Settings.featuresManagement(block: FeaturesManagementExtension.() -> Unit): Unit = extensions.configure("featuresManagement", block)