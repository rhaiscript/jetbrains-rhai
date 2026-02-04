package org.rhai.registry

import com.intellij.openapi.project.Project
import org.rhai.settings.RhaiCustomRegistrySettings
import org.rhai.settings.RhaiGlobalRegistrySettings

/**
 * Unified provider for all Rhai registry sources.
 * Combines:
 * - Global registry (application-level, all projects)
 * - Project registry (project-specific manual entries)
 * - Auto registry (automatically parsed from Rust files)
 */
object RhaiRegistryProvider {

    /**
     * Get all registered functions from all sources
     */
    fun getAllFunctions(project: Project): Set<String> {
        val functions = mutableSetOf<String>()

        // Add global functions
        functions.addAll(RhaiGlobalRegistrySettings.getInstance().getGlobalFunctionList())

        // Add project-specific functions
        functions.addAll(RhaiCustomRegistrySettings.getInstance(project).getCustomFunctionList())

        // Add auto-registered functions
        functions.addAll(RhaiAutoRegistryService.getInstance(project).getAutoFunctions())

        return functions
    }

    /**
     * Get all registered variables from all sources
     */
    fun getAllVariables(project: Project): Set<String> {
        val variables = mutableSetOf<String>()

        // Add global variables
        variables.addAll(RhaiGlobalRegistrySettings.getInstance().getGlobalVariableList())

        // Add project-specific variables
        variables.addAll(RhaiCustomRegistrySettings.getInstance(project).getCustomVariableList())

        // Add auto-registered variables
        variables.addAll(RhaiAutoRegistryService.getInstance(project).getAutoVariables())

        return variables
    }

    /**
     * Get all registered types from all sources
     */
    fun getAllTypes(project: Project): Set<String> {
        val types = mutableSetOf<String>()

        // Add global types
        types.addAll(RhaiGlobalRegistrySettings.getInstance().getGlobalTypeList())

        // Add project-specific types
        types.addAll(RhaiCustomRegistrySettings.getInstance(project).getCustomTypeList())

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
     * Get source information for a function
     */
    fun getFunctionSource(project: Project, name: String): RegistrySource {
        if (RhaiGlobalRegistrySettings.getInstance().getGlobalFunctionList().contains(name)) {
            return RegistrySource.GLOBAL
        }
        if (RhaiCustomRegistrySettings.getInstance(project).getCustomFunctionList().contains(name)) {
            return RegistrySource.PROJECT
        }
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
