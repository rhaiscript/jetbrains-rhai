package org.rhai.registry

import com.intellij.openapi.project.Project
import org.rhai.settings.RhaiCustomRegistrySettings
import org.rhai.settings.RhaiGlobalRegistrySettings

/**
 * Unified provider for all Rhai registry sources.
 * Combines:
 * - Global scope (application-level Rhai code)
 * - Project scope (project-specific Rhai code, optionally inheriting global)
 * - Auto registry (automatically parsed from Rust files)
 */
object RhaiRegistryProvider {

    /**
     * Get all registered functions from all sources
     */
    fun getAllFunctions(project: Project): Set<String> {
        val functions = mutableSetOf<String>()
        val parser = RhaiRegistryCodeParser.getInstance(project)
        val projectSettings = RhaiCustomRegistrySettings.getInstance(project)

        // Add global functions (if inherited)
        if (projectSettings.inheritGlobalScope) {
            val globalSettings = RhaiGlobalRegistrySettings.getInstance()
            val globalParsed = parser.parseRhaiCode(globalSettings.globalRhaiCode)
            functions.addAll(globalParsed.functions.map { it.name })
        }

        // Add project-specific functions
        val projectParsed = parser.parseRhaiCode(projectSettings.projectRhaiCode)
        functions.addAll(projectParsed.functions.map { it.name })

        // Add auto-registered functions
        functions.addAll(RhaiAutoRegistryService.getInstance(project).getAutoFunctions())

        return functions
    }

    /**
     * Get all registered variables from all sources
     */
    fun getAllVariables(project: Project): Set<String> {
        val variables = mutableSetOf<String>()
        val parser = RhaiRegistryCodeParser.getInstance(project)
        val projectSettings = RhaiCustomRegistrySettings.getInstance(project)

        // Add global variables (if inherited)
        if (projectSettings.inheritGlobalScope) {
            val globalSettings = RhaiGlobalRegistrySettings.getInstance()
            val globalParsed = parser.parseRhaiCode(globalSettings.globalRhaiCode)
            variables.addAll(globalParsed.variables.map { it.name })
            variables.addAll(globalParsed.constants.map { it.name })
        }

        // Add project-specific variables
        val projectParsed = parser.parseRhaiCode(projectSettings.projectRhaiCode)
        variables.addAll(projectParsed.variables.map { it.name })
        variables.addAll(projectParsed.constants.map { it.name })

        // Add auto-registered variables
        variables.addAll(RhaiAutoRegistryService.getInstance(project).getAutoVariables())

        return variables
    }

    /**
     * Get all registered types from all sources
     */
    fun getAllTypes(project: Project): Set<String> {
        val types = mutableSetOf<String>()
        val parser = RhaiRegistryCodeParser.getInstance(project)
        val projectSettings = RhaiCustomRegistrySettings.getInstance(project)

        // Add global types (if inherited)
        if (projectSettings.inheritGlobalScope) {
            val globalSettings = RhaiGlobalRegistrySettings.getInstance()
            val globalParsed = parser.parseRhaiCode(globalSettings.globalRhaiCode)
            types.addAll(globalParsed.types)
        }

        // Add project-specific types
        val projectParsed = parser.parseRhaiCode(projectSettings.projectRhaiCode)
        types.addAll(projectParsed.types)

        // Add auto-registered types
        types.addAll(RhaiAutoRegistryService.getInstance(project).getAutoTypes())

        return types
    }

    /**
     * Get all registered property names (getters/setters)
     */
    fun getAllProperties(project: Project): Set<String> {
        return RhaiAutoRegistryService.getInstance(project).getAutoProperties()
    }

    /**
     * Check if a function is registered in any source
     */
    fun isFunctionRegistered(project: Project, name: String): Boolean {
        return getAllFunctions(project).contains(name)
    }

    /**
     * Check if a variable is registered in any source
     */
    fun isVariableRegistered(project: Project, name: String): Boolean {
        return getAllVariables(project).contains(name)
    }

    /**
     * Check if a type is registered in any source
     */
    fun isTypeRegistered(project: Project, name: String): Boolean {
        return getAllTypes(project).contains(name)
    }

    /**
     * Check if identifier is known (function, variable, or type)
     */
    fun isKnownIdentifier(project: Project, name: String): Boolean {
        return isFunctionRegistered(project, name) ||
                isVariableRegistered(project, name) ||
                isTypeRegistered(project, name)
    }

    /**
     * Get function info with signature
     */
    fun getFunctionInfo(project: Project, name: String): ParsedRegistryEntry? {
        val parser = RhaiRegistryCodeParser.getInstance(project)
        val projectSettings = RhaiCustomRegistrySettings.getInstance(project)

        // Check project first
        val projectParsed = parser.parseRhaiCode(projectSettings.projectRhaiCode)
        projectParsed.functions.find { it.name == name }?.let { return it }

        // Check global (if inherited)
        if (projectSettings.inheritGlobalScope) {
            val globalSettings = RhaiGlobalRegistrySettings.getInstance()
            val globalParsed = parser.parseRhaiCode(globalSettings.globalRhaiCode)
            globalParsed.functions.find { it.name == name }?.let { return it }
        }

        return null
    }

    /**
     * Get all function entries with full info
     */
    fun getAllFunctionEntries(project: Project): Set<ParsedRegistryEntry> {
        val entries = mutableSetOf<ParsedRegistryEntry>()
        val parser = RhaiRegistryCodeParser.getInstance(project)
        val projectSettings = RhaiCustomRegistrySettings.getInstance(project)

        // Add global functions (if inherited)
        if (projectSettings.inheritGlobalScope) {
            val globalSettings = RhaiGlobalRegistrySettings.getInstance()
            val globalParsed = parser.parseRhaiCode(globalSettings.globalRhaiCode)
            entries.addAll(globalParsed.functions)
        }

        // Add project-specific functions
        val projectParsed = parser.parseRhaiCode(projectSettings.projectRhaiCode)
        entries.addAll(projectParsed.functions)

        return entries
    }

    /**
     * Get source information for a function
     */
    fun getFunctionSource(project: Project, name: String): RegistrySource {
        val parser = RhaiRegistryCodeParser.getInstance(project)
        val projectSettings = RhaiCustomRegistrySettings.getInstance(project)

        // Check global first
        val globalSettings = RhaiGlobalRegistrySettings.getInstance()
        val globalParsed = parser.parseRhaiCode(globalSettings.globalRhaiCode)
        if (globalParsed.functions.any { it.name == name }) {
            return RegistrySource.GLOBAL
        }

        // Check project
        val projectParsed = parser.parseRhaiCode(projectSettings.projectRhaiCode)
        if (projectParsed.functions.any { it.name == name }) {
            return RegistrySource.PROJECT
        }

        // Check auto-registry
        if (RhaiAutoRegistryService.getInstance(project).getAutoFunctions().contains(name)) {
            return RegistrySource.AUTO
        }

        return RegistrySource.UNKNOWN
    }

    /**
     * Trigger rescan of auto-registry
     */
    fun rescanAutoRegistry(project: Project) {
        RhaiAutoRegistryService.getInstance(project).rescan()
    }

    /**
     * Invalidate parser cache
     */
    fun invalidateCache(project: Project) {
        RhaiRegistryCodeParser.getInstance(project).invalidateCache()
    }
}

/**
 * Source of a registry entry
 */
enum class RegistrySource {
    GLOBAL,   // Application-level, available in all projects
    PROJECT,  // Project-specific manual entry
    AUTO,     // Automatically parsed from Rust files
    UNKNOWN   // Not found in registry
}
