package org.rhai.registry

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.util.concurrency.AppExecutorUtil
import org.rhai.settings.RhaiCustomRegistrySettings
import org.rhai.settings.RhaiGlobalRegistrySettings
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service that automatically scans Rust files for Rhai function registrations.
 */
@Service(Service.Level.PROJECT)
class RhaiAutoRegistryService(private val project: Project) {

    private val log = Logger.getInstance(RhaiAutoRegistryService::class.java)

    private val autoRegisteredFunctions = ConcurrentHashMap<String, RhaiRegistration>()
    private val autoRegisteredVariables = ConcurrentHashMap<String, RhaiRegistration>()
    private val autoRegisteredTypes = ConcurrentHashMap<String, RhaiRegistration>()
    private val autoRegisteredGetters = ConcurrentHashMap<String, RhaiRegistration>()
    private val autoRegisteredSetters = ConcurrentHashMap<String, RhaiRegistration>()

    private val isScanning = AtomicBoolean(false)
    private var lastScanTime: Long = 0

    /**
     * Get all auto-registered function names
     */
    fun getAutoFunctions(): Set<String> = autoRegisteredFunctions.keys.toSet()

    /**
     * Get all auto-registered variable names
     */
    fun getAutoVariables(): Set<String> = autoRegisteredVariables.keys.toSet()

    /**
     * Get all auto-registered type names
     */
    fun getAutoTypes(): Set<String> = autoRegisteredTypes.keys.toSet()

    /**
     * Get all auto-registered property names (getters/setters)
     */
    fun getAutoProperties(): Set<String> {
        return autoRegisteredGetters.keys + autoRegisteredSetters.keys
    }

    /**
     * Get registration info for a function
     */
    fun getFunctionInfo(name: String): RhaiRegistration? = autoRegisteredFunctions[name]

    /**
     * Check if auto-registry is enabled
     */
    fun isAutoRegistryEnabled(): Boolean {
        return RhaiCustomRegistrySettings.getInstance(project).autoRegistryEnabled
    }

    /**
     * Trigger a rescan of Rust files
     */
    fun rescan() {
        if (!isAutoRegistryEnabled()) {
            log.info("Auto-registry is disabled, skipping scan")
            return
        }

        if (isScanning.compareAndSet(false, true)) {
            AppExecutorUtil.getAppExecutorService().submit {
                try {
                    performScan()
                } finally {
                    isScanning.set(false)
                    lastScanTime = System.currentTimeMillis()
                }
            }
        }
    }

    /**
     * Perform the actual scan of Rust files
     */
    private fun performScan() {
        log.info("Starting auto-registry scan for project: ${project.name}")

        val newFunctions = ConcurrentHashMap<String, RhaiRegistration>()
        val newVariables = ConcurrentHashMap<String, RhaiRegistration>()
        val newTypes = ConcurrentHashMap<String, RhaiRegistration>()
        val newGetters = ConcurrentHashMap<String, RhaiRegistration>()
        val newSetters = ConcurrentHashMap<String, RhaiRegistration>()

        ReadAction.run<Throwable> {
            val contentRoots = ProjectRootManager.getInstance(project).contentRoots

            for (root in contentRoots) {
                VfsUtilCore.visitChildrenRecursively(root, object : VirtualFileVisitor<Unit>() {
                    override fun visitFile(file: VirtualFile): Boolean {
                        if (file.extension == "rs") {
                            try {
                                val content = String(file.contentsToByteArray(), Charsets.UTF_8)
                                val registrations = RhaiRegistrationParser.parseRustSource(
                                    content,
                                    file.path
                                )

                                for (reg in registrations) {
                                    when (reg.type) {
                                        RegistrationType.FUNCTION -> newFunctions[reg.name] = reg
                                        RegistrationType.VARIABLE -> newVariables[reg.name] = reg
                                        RegistrationType.TYPE -> newTypes[reg.name] = reg
                                        RegistrationType.GETTER -> newGetters[reg.name] = reg
                                        RegistrationType.SETTER -> newSetters[reg.name] = reg
                                        RegistrationType.MODULE -> {} // Handle modules separately if needed
                                        RegistrationType.INDEXER -> {} // Indexers don't have names
                                        RegistrationType.OPERATOR -> {} // Operators use symbols
                                    }
                                }
                            } catch (e: Exception) {
                                log.warn("Error parsing Rust file: ${file.path}", e)
                            }
                        }
                        return true
                    }
                })
            }
        }

        // Update the maps
        autoRegisteredFunctions.clear()
        autoRegisteredFunctions.putAll(newFunctions)

        autoRegisteredVariables.clear()
        autoRegisteredVariables.putAll(newVariables)

        autoRegisteredTypes.clear()
        autoRegisteredTypes.putAll(newTypes)

        autoRegisteredGetters.clear()
        autoRegisteredGetters.putAll(newGetters)

        autoRegisteredSetters.clear()
        autoRegisteredSetters.putAll(newSetters)

        // Save to configured destination (project or global)
        saveToDestination(newFunctions.keys, newVariables.keys, newTypes.keys)

        log.info("Auto-registry scan complete. Found: " +
            "${newFunctions.size} functions, " +
            "${newVariables.size} variables, " +
            "${newTypes.size} types, " +
            "${newGetters.size} getters, " +
            "${newSetters.size} setters")
    }

    /**
     * Save detected registrations to the configured destination (project or global)
     */
    private fun saveToDestination(
        functions: Set<String>,
        variables: Set<String>,
        types: Set<String>
    ) {
        val settings = RhaiCustomRegistrySettings.getInstance(project)
        val destination = settings.autoRegistryDestination

        if (destination == "global") {
            val globalSettings = RhaiGlobalRegistrySettings.getInstance()

            // Merge with existing global functions
            val existingFunctions = globalSettings.getGlobalFunctionList().toMutableSet()
            existingFunctions.addAll(functions)
            globalSettings.globalFunctions = existingFunctions.sorted().joinToString("\n")

            // Merge with existing global variables
            val existingVariables = globalSettings.getGlobalVariableList().toMutableSet()
            existingVariables.addAll(variables)
            globalSettings.globalVariables = existingVariables.sorted().joinToString("\n")

            // Merge with existing global types
            val existingTypes = globalSettings.getGlobalTypeList().toMutableSet()
            existingTypes.addAll(types)
            globalSettings.globalTypes = existingTypes.sorted().joinToString("\n")

            log.info("Saved auto-detected registrations to global registry")
        } else {
            // Save to project registry
            // Merge with existing project functions
            val existingFunctions = settings.getCustomFunctionList().toMutableSet()
            existingFunctions.addAll(functions)
            settings.customFunctions = existingFunctions.sorted().joinToString("\n")

            // Merge with existing project variables
            val existingVariables = settings.getCustomVariableList().toMutableSet()
            existingVariables.addAll(variables)
            settings.customVariables = existingVariables.sorted().joinToString("\n")

            // Merge with existing project types
            val existingTypes = settings.getCustomTypeList().toMutableSet()
            existingTypes.addAll(types)
            settings.customTypes = existingTypes.sorted().joinToString("\n")

            log.info("Saved auto-detected registrations to project registry")
        }
    }

    /**
     * Parse a single file (useful for incremental updates)
     */
    fun parseFile(file: VirtualFile) {
        if (!isAutoRegistryEnabled()) return
        if (file.extension != "rs") return

        ReadAction.run<Throwable> {
            try {
                val content = String(file.contentsToByteArray(), Charsets.UTF_8)
                val registrations = RhaiRegistrationParser.parseRustSource(content, file.path)

                for (reg in registrations) {
                    when (reg.type) {
                        RegistrationType.FUNCTION -> autoRegisteredFunctions[reg.name] = reg
                        RegistrationType.VARIABLE -> autoRegisteredVariables[reg.name] = reg
                        RegistrationType.TYPE -> autoRegisteredTypes[reg.name] = reg
                        RegistrationType.GETTER -> autoRegisteredGetters[reg.name] = reg
                        RegistrationType.SETTER -> autoRegisteredSetters[reg.name] = reg
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                log.warn("Error parsing Rust file: ${file.path}", e)
            }
        }
    }

    /**
     * Clear all auto-registered items
     */
    fun clear() {
        autoRegisteredFunctions.clear()
        autoRegisteredVariables.clear()
        autoRegisteredTypes.clear()
        autoRegisteredGetters.clear()
        autoRegisteredSetters.clear()
    }

    companion object {
        fun getInstance(project: Project): RhaiAutoRegistryService {
            return project.getService(RhaiAutoRegistryService::class.java)
        }
    }
}
