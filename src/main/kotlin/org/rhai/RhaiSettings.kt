package org.rhai

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "RhaiSettings", storages = [Storage("rhai.xml")])
class RhaiSettings : PersistentStateComponent<RhaiSettings> {
    var enableAdvancedFeatures = true
    var showTypeHints = false
    var autoFormatOnSave = true

    override fun getState(): RhaiSettings = this
    override fun loadState(state: RhaiSettings) = XmlSerializerUtil.copyBean(state, this)

    companion object {
        fun getInstance(): RhaiSettings = ApplicationManager.getApplication().getService(RhaiSettings::class.java)
    }
}
