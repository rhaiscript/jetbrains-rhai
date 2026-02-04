package org.rhai.registry

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Startup activity that triggers auto-registry scan when project opens.
 */
class RhaiAutoRegistryStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        // Trigger initial scan if auto-registry is enabled
        val autoRegistryService = RhaiAutoRegistryService.getInstance(project)
        if (autoRegistryService.isAutoRegistryEnabled()) {
            autoRegistryService.rescan()
        }
    }
}
