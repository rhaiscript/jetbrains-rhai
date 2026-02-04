package org.rhai.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project

/**
 * Provider for RhaiCustomRegistryConfigurable.
 * Required for project-level configurables.
 */
class RhaiCustomRegistryConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun createConfigurable(): Configurable {
        return RhaiCustomRegistryConfigurable(project)
    }
}
