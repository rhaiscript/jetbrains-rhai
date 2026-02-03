package org.rhai.run

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil
import org.rhai.lang.RhaiIcons

class RhaiRunConfigurationType : ConfigurationTypeBase(
    "RhaiRunConfiguration",
    "Rhai Script",
    "Run Rhai script",
    RhaiIcons.FILE
) {
    init {
        addFactory(RhaiRunConfigurationFactory(this))
    }

    companion object {
        val INSTANCE: RhaiRunConfigurationType
            get() = ConfigurationTypeUtil.findConfigurationType(RhaiRunConfigurationType::class.java)
    }
}
