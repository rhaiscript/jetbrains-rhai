package org.rhai.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent settings for custom Rhai functions and variables
 * registered from Rust code.
 */
@State(
    name = "RhaiCustomRegistrySettings",
    storages = [Storage("RhaiCustomRegistry.xml")]
)
@Service(Service.Level.PROJECT)
class RhaiCustomRegistrySettings : PersistentStateComponent<RhaiCustomRegistrySettings> {

    /**
     * Custom functions registered from Rust (comma-separated)
     */
    var customFunctions: String = ""

    /**
     * Custom variables/constants registered from Rust (comma-separated)
     */
    var customVariables: String = ""

    /**
     * Custom types registered from Rust (comma-separated)
     */
    var customTypes: String = ""

    override fun getState(): RhaiCustomRegistrySettings = this

    override fun loadState(state: RhaiCustomRegistrySettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    /**
     * Get list of custom function names
     */
    fun getCustomFunctionList(): Set<String> {
        return customFunctions
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Get list of custom variable names
     */
    fun getCustomVariableList(): Set<String> {
        return customVariables
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Get list of custom type names
     */
    fun getCustomTypeList(): Set<String> {
        return customTypes
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    companion object {
        fun getInstance(project: com.intellij.openapi.project.Project): RhaiCustomRegistrySettings {
            return project.getService(RhaiCustomRegistrySettings::class.java)
        }
    }
}
