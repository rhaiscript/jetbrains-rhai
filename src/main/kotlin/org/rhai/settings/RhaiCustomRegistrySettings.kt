package org.rhai.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent settings for project-specific Rhai registry.
 * Stores Rhai code that defines functions, variables, and types for this project.
 */
@State(
    name = "RhaiCustomRegistrySettings",
    storages = [Storage("RhaiCustomRegistry.xml")]
)
@Service(Service.Level.PROJECT)
class RhaiCustomRegistrySettings : PersistentStateComponent<RhaiCustomRegistrySettings> {

    /**
     * Rhai code defining project scope (functions, variables, constants)
     */
    var projectRhaiCode: String = ""

    /**
     * Whether to inherit global scope definitions in this project
     */
    var inheritGlobalScope: Boolean = true

    /**
     * Enable automatic parsing of Rust files for Rhai registrations
     */
    var autoRegistryEnabled: Boolean = true

    /**
     * Where to save auto-detected registrations: "project" or "global"
     */
    var autoRegistryDestination: String = "project"

    /**
     * File patterns to include in auto-scan (glob patterns, newline-separated)
     */
    var autoScanPatterns: String = "**/*.rs"

    /**
     * File patterns to exclude from auto-scan (glob patterns, newline-separated)
     */
    var autoScanExcludePatterns: String = "**/target/**\n**/build/**"

    // Legacy fields for migration
    var customFunctions: String = ""
    var customVariables: String = ""
    var customTypes: String = ""
    var migrated: Boolean = false

    override fun getState(): RhaiCustomRegistrySettings = this

    override fun loadState(state: RhaiCustomRegistrySettings) {
        XmlSerializerUtil.copyBean(state, this)

        // Perform migration if needed
        if (!migrated && hasLegacyData()) {
            migrateFromLegacyFormat()
            migrated = true
        }
    }

    private fun hasLegacyData(): Boolean {
        return customFunctions.isNotEmpty() || customVariables.isNotEmpty() || customTypes.isNotEmpty()
    }

    private fun migrateFromLegacyFormat() {
        val builder = StringBuilder()
        builder.appendLine("// === Migrated from legacy format ===")
        builder.appendLine()

        val functions = getCustomFunctionList()
        if (functions.isNotEmpty()) {
            builder.appendLine("// Functions:")
            functions.forEach { name ->
                builder.appendLine("fn $name() {}")
            }
            builder.appendLine()
        }

        val variables = getCustomVariableList()
        if (variables.isNotEmpty()) {
            builder.appendLine("// Variables:")
            variables.forEach { name ->
                builder.appendLine("let $name;")
            }
            builder.appendLine()
        }

        val types = getCustomTypeList()
        if (types.isNotEmpty()) {
            builder.appendLine("// Types:")
            types.forEach { name ->
                builder.appendLine("// type: $name")
            }
        }

        projectRhaiCode = builder.toString().trim()

        // Clear legacy fields after migration
        customFunctions = ""
        customVariables = ""
        customTypes = ""
    }

    /**
     * Legacy method - get list of custom function names
     */
    fun getCustomFunctionList(): Set<String> {
        return customFunctions
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Legacy method - get list of custom variable names
     */
    fun getCustomVariableList(): Set<String> {
        return customVariables
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Legacy method - get list of custom type names
     */
    fun getCustomTypeList(): Set<String> {
        return customTypes
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
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

    /**
     * Add a function declaration to project Rhai code
     */
    fun addCustomFunction(name: String) {
        val declaration = "fn $name() {}"
        appendToRhaiCode(declaration)
    }

    /**
     * Add a variable declaration to project Rhai code
     */
    fun addCustomVariable(name: String) {
        val declaration = "let $name;"
        appendToRhaiCode(declaration)
    }

    /**
     * Add a type declaration (as comment) to project Rhai code
     */
    fun addCustomType(name: String) {
        val declaration = "// type: $name"
        appendToRhaiCode(declaration)
    }

    private fun appendToRhaiCode(declaration: String) {
        projectRhaiCode = if (projectRhaiCode.isBlank()) {
            declaration
        } else {
            projectRhaiCode.trimEnd() + "\n" + declaration
        }
    }

    companion object {
        fun getInstance(project: Project): RhaiCustomRegistrySettings {
            return project.getService(RhaiCustomRegistrySettings::class.java)
        }
    }
}
