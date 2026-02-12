package org.rhai.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Global (application-level) settings for Rhai registry.
 * Stores Rhai code that defines functions, variables, and types available across all projects.
 */
@State(
    name = "RhaiGlobalRegistrySettings",
    storages = [Storage("RhaiGlobalRegistry.xml")]
)
@Service(Service.Level.APP)
class RhaiGlobalRegistrySettings : PersistentStateComponent<RhaiGlobalRegistrySettings> {

    /**
     * Rhai code defining global scope (functions, variables, constants)
     */
    var globalRhaiCode: String = ""

    /**
     * Legacy: Global functions (comma or newline separated) - kept for migration
     */
    var globalFunctions: String = ""

    /**
     * Legacy: Global variables (comma or newline separated) - kept for migration
     */
    var globalVariables: String = ""

    /**
     * Legacy: Global types (comma or newline separated) - kept for migration
     */
    var globalTypes: String = ""

    /**
     * Flag indicating if migration from legacy format has been done
     */
    var migrated: Boolean = false

    override fun getState(): RhaiGlobalRegistrySettings = this

    override fun loadState(state: RhaiGlobalRegistrySettings) {
        XmlSerializerUtil.copyBean(state, this)

        // Perform migration if needed
        if (!migrated && hasLegacyData()) {
            migrateFromLegacyFormat()
            migrated = true
        }
    }

    private fun hasLegacyData(): Boolean {
        return globalFunctions.isNotEmpty() || globalVariables.isNotEmpty() || globalTypes.isNotEmpty()
    }

    private fun migrateFromLegacyFormat() {
        val builder = StringBuilder()
        builder.appendLine("// === Migrated from legacy format ===")
        builder.appendLine()

        val functions = getGlobalFunctionList()
        if (functions.isNotEmpty()) {
            builder.appendLine("// Functions:")
            functions.forEach { name ->
                builder.appendLine("fn $name() {}")
            }
            builder.appendLine()
        }

        val variables = getGlobalVariableList()
        if (variables.isNotEmpty()) {
            builder.appendLine("// Variables:")
            variables.forEach { name ->
                builder.appendLine("let $name;")
            }
            builder.appendLine()
        }

        val types = getGlobalTypeList()
        if (types.isNotEmpty()) {
            builder.appendLine("// Types:")
            types.forEach { name ->
                builder.appendLine("// type: $name")
            }
        }

        globalRhaiCode = builder.toString().trim()

        // Clear legacy fields after migration
        globalFunctions = ""
        globalVariables = ""
        globalTypes = ""
    }

    /**
     * Legacy method - get list of function names from legacy field
     */
    fun getGlobalFunctionList(): Set<String> {
        return globalFunctions
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Legacy method - get list of variable names from legacy field
     */
    fun getGlobalVariableList(): Set<String> {
        return globalVariables
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Legacy method - get list of type names from legacy field
     */
    fun getGlobalTypeList(): Set<String> {
        return globalTypes
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Add a function declaration to global Rhai code
     */
    fun addGlobalFunction(name: String) {
        val declaration = "fn $name() {}"
        appendToRhaiCode(declaration)
    }

    /**
     * Add a variable declaration to global Rhai code
     */
    fun addGlobalVariable(name: String) {
        val declaration = "let $name;"
        appendToRhaiCode(declaration)
    }

    /**
     * Add a type declaration (as comment) to global Rhai code
     */
    fun addGlobalType(name: String) {
        val declaration = "// type: $name"
        appendToRhaiCode(declaration)
    }

    private fun appendToRhaiCode(declaration: String) {
        globalRhaiCode = if (globalRhaiCode.isBlank()) {
            declaration
        } else {
            globalRhaiCode.trimEnd() + "\n" + declaration
        }
    }

    companion object {
        fun getInstance(): RhaiGlobalRegistrySettings {
            return ApplicationManager.getApplication().getService(RhaiGlobalRegistrySettings::class.java)
        }
    }
}
