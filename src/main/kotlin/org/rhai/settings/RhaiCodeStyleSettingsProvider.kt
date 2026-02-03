package org.rhai.settings

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

class RhaiCodeStyleSettingsProvider : CodeStyleSettingsProvider() {

    override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings {
        return RhaiCodeStyleSettings(settings)
    }

    override fun getConfigurableDisplayName(): String = "Rhai"

    override fun hasSettingsPage(): Boolean = false
}
