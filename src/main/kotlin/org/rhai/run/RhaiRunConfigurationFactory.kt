package org.rhai.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

class RhaiRunConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String = "RhaiRunConfigurationFactory"

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return RhaiRunConfiguration(project, this, "Rhai Script")
    }

    override fun getOptionsClass(): Class<out BaseState> {
        return RhaiRunConfigurationOptions::class.java
    }
}
