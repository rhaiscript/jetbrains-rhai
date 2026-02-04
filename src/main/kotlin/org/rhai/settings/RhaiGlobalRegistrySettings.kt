package org.rhai.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*

/**
 * Global (application-level) settings for Rhai functions and variables.
 * These are available across all projects.
 */
@State(
    name = "RhaiGlobalRegistrySettings",
    storages = [Storage("RhaiGlobalRegistry.xml")]
)
@Service(Service.Level.APP)
class RhaiGlobalRegistrySettings : PersistentStateComponent<RhaiGlobalRegistrySettings> {

    /**
     * Global functions available in all projects (comma or newline separated)
     */
    var globalFunctions: String = ""

    /**
     * Global variables available in all projects (comma or newline separated)
     */
    var globalVariables: String = ""

    /**
     * Global types available in all projects (comma or newline separated)
     */
    var globalTypes: String = ""

    override fun getState(): RhaiGlobalRegistrySettings = this

    override fun loadState(state: RhaiGlobalRegistrySettings) {
        globalFunctions = state.globalFunctions
        globalVariables = state.globalVariables
        globalTypes = state.globalTypes
    }

    fun getGlobalFunctionList(): Set<String> {
        return globalFunctions
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    fun getGlobalVariableList(): Set<String> {
        return globalVariables
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    fun getGlobalTypeList(): Set<String> {
        return globalTypes
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    fun addGlobalFunction(name: String) {
        val functions = getGlobalFunctionList().toMutableSet()
        functions.add(name)
        globalFunctions = functions.sorted().joinToString("\n")
    }

    fun addGlobalVariable(name: String) {
        val variables = getGlobalVariableList().toMutableSet()
        variables.add(name)
        globalVariables = variables.sorted().joinToString("\n")
    }

    fun addGlobalType(name: String) {
        val types = getGlobalTypeList().toMutableSet()
        types.add(name)
        globalTypes = types.sorted().joinToString("\n")
    }

    companion object {
        fun getInstance(): RhaiGlobalRegistrySettings {
            return ApplicationManager.getApplication().getService(RhaiGlobalRegistrySettings::class.java)
        }
    }
}
