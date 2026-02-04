package org.rhai.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent settings for custom Rhai functions and variables
 * registered from Rust code. These are project-specific.
 */
@State(
    name = "RhaiCustomRegistrySettings",
    storages = [Storage("RhaiCustomRegistry.xml")]
)
@Service(Service.Level.PROJECT)
class RhaiCustomRegistrySettings : PersistentStateComponent<RhaiCustomRegistrySettings> {

    /**
     * Enable automatic parsing of Rust files for Rhai registrations
     */
    var autoRegistryEnabled: Boolean = true

    /**
     * Where to save auto-detected registrations: "project" or "global"
     */
    var autoRegistryDestination: String = "project"

    /**
     * Custom functions registered from Rust (project-specific, comma-separated)
     */
    var customFunctions: String = ""

    /**
     * Custom variables/constants registered from Rust (project-specific, comma-separated)
     */
    var customVariables: String = ""

    /**
     * Custom types registered from Rust (project-specific, comma-separated)
     */
    var customTypes: String = ""

    /**
     * File patterns to include in auto-scan (glob patterns, newline-separated)
     * Default: all .rs files
     */
    var autoScanPatterns: String = "**/*.rs"

    /**
     * File patterns to exclude from auto-scan (glob patterns, newline-separated)
     */
    var autoScanExcludePatterns: String = "**/target/**\n**/build/**"

    override fun getState(): RhaiCustomRegistrySettings = this

    override fun loadState(state: RhaiCustomRegistrySettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    /**
     * Get list of custom function names (project-specific)
     */
    fun getCustomFunctionList(): Set<String> {
        return customFunctions
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Get list of custom variable names (project-specific)
     */
    fun getCustomVariableList(): Set<String> {
        return customVariables
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Get list of custom type names (project-specific)
     */
    fun getCustomTypeList(): Set<String> {
        return customTypes
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Add a function to project-specific registry
     */
    fun addCustomFunction(name: String) {
        val functions = getCustomFunctionList().toMutableSet()
        functions.add(name)
        customFunctions = functions.sorted().joinToString("\n")
    }

    /**
     * Add a variable to project-specific registry
     */
    fun addCustomVariable(name: String) {
        val variables = getCustomVariableList().toMutableSet()
        variables.add(name)
        customVariables = variables.sorted().joinToString("\n")
    }

    /**
     * Add a type to project-specific registry
     */
    fun addCustomType(name: String) {
        val types = getCustomTypeList().toMutableSet()
        types.add(name)
        customTypes = types.sorted().joinToString("\n")
    }

    /**
     * Remove a function from project-specific registry
     */
    fun removeCustomFunction(name: String) {
        val functions = getCustomFunctionList().toMutableSet()
        functions.remove(name)
        customFunctions = functions.sorted().joinToString("\n")
    }

    /**
     * Get include patterns as list
     */
    fun getIncludePatterns(): List<String> {
        return autoScanPatterns
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * Get exclude patterns as list
     */
    fun getExcludePatterns(): List<String> {
        return autoScanExcludePatterns
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    companion object {
        fun getInstance(project: Project): RhaiCustomRegistrySettings {
            return project.getService(RhaiCustomRegistrySettings::class.java)
        }
    }
}
