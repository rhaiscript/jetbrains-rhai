package org.rhai.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import org.rhai.registry.RhaiAutoRegistryService
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Configuration UI for Rhai registry settings.
 * Supports both project-specific and global registries.
 */
class RhaiCustomRegistryConfigurable(private val project: Project) : Configurable {

    private var autoRegistryCheckbox: JBCheckBox? = null
    private var destinationProjectRadio: JBRadioButton? = null
    private var destinationGlobalRadio: JBRadioButton? = null
    private var projectFunctionsTextArea: JBTextArea? = null
    private var projectVariablesTextArea: JBTextArea? = null
    private var projectTypesTextArea: JBTextArea? = null
    private var includePatternTextArea: JBTextArea? = null
    private var excludePatternTextArea: JBTextArea? = null

    private var globalFunctionsTextArea: JBTextArea? = null
    private var globalVariablesTextArea: JBTextArea? = null
    private var globalTypesTextArea: JBTextArea? = null

    private var autoRegistryInfoLabel: JBLabel? = null

    override fun getDisplayName(): String = "Rhai Custom Registry"

    override fun createComponent(): JComponent {
        val tabbedPane = JBTabbedPane()

        tabbedPane.addTab("Project Registry", createProjectPanel())
        tabbedPane.addTab("Global Registry", createGlobalPanel())
        tabbedPane.addTab("Auto Registry", createAutoRegistryPanel())

        // Initialize with current settings
        reset()

        return tabbedPane
    }

    private fun createProjectPanel(): JComponent {
        projectFunctionsTextArea = JBTextArea(6, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }

        projectVariablesTextArea = JBTextArea(6, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }

        projectTypesTextArea = JBTextArea(4, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }

        return panel {
            row {
                label("<html><b>Project-Specific Registry</b><br>" +
                    "These functions, variables, and types are only available in this project.</html>")
            }
            group("Functions") {
                row {
                    scrollCell(projectFunctionsTextArea!!)
                        .align(Align.FILL)
                        .resizableColumn()
                }.resizableRow()
                row {
                    comment("e.g.: my_rust_func, calculate_something, get_config")
                }
            }
            group("Variables") {
                row {
                    scrollCell(projectVariablesTextArea!!)
                        .align(Align.FILL)
                        .resizableColumn()
                }.resizableRow()
                row {
                    comment("e.g.: MY_CONSTANT, config_value, app_state")
                }
            }
            group("Types") {
                row {
                    scrollCell(projectTypesTextArea!!)
                        .align(Align.FILL)
                        .resizableColumn()
                }.resizableRow()
                row {
                    comment("e.g.: MyStruct, CustomType, AppConfig")
                }
            }
        }.apply {
            border = JBUI.Borders.empty(10)
        }
    }

    private fun createGlobalPanel(): JComponent {
        globalFunctionsTextArea = JBTextArea(6, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }

        globalVariablesTextArea = JBTextArea(6, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }

        globalTypesTextArea = JBTextArea(4, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }

        return panel {
            row {
                label("<html><b>Global Registry</b><br>" +
                    "These functions, variables, and types are available in ALL projects.</html>")
            }
            group("Functions") {
                row {
                    scrollCell(globalFunctionsTextArea!!)
                        .align(Align.FILL)
                        .resizableColumn()
                }.resizableRow()
                row {
                    comment("Functions available in all Rhai files across all projects")
                }
            }
            group("Variables") {
                row {
                    scrollCell(globalVariablesTextArea!!)
                        .align(Align.FILL)
                        .resizableColumn()
                }.resizableRow()
                row {
                    comment("Variables/constants available in all projects")
                }
            }
            group("Types") {
                row {
                    scrollCell(globalTypesTextArea!!)
                        .align(Align.FILL)
                        .resizableColumn()
                }.resizableRow()
                row {
                    comment("Types available in all projects")
                }
            }
        }.apply {
            border = JBUI.Borders.empty(10)
        }
    }

    private fun createAutoRegistryPanel(): JComponent {
        autoRegistryCheckbox = JBCheckBox("Enable automatic registry scanning", true)
        destinationProjectRadio = JBRadioButton("Project registry (only this project)", true)
        destinationGlobalRadio = JBRadioButton("Global registry (all projects)", false)

        val destinationGroup = ButtonGroup()
        destinationGroup.add(destinationProjectRadio)
        destinationGroup.add(destinationGlobalRadio)

        // Wrap radio buttons in a JPanel to avoid DSL buttonsGroup requirement
        val radioPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(destinationProjectRadio)
            add(destinationGlobalRadio)
        }

        includePatternTextArea = JBTextArea(3, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }

        excludePatternTextArea = JBTextArea(3, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }

        autoRegistryInfoLabel = JBLabel("")

        val rescanButton = JButton("Rescan Now").apply {
            addActionListener {
                RhaiAutoRegistryService.getInstance(project).rescan()
                updateAutoRegistryInfo()
            }
        }

        return panel {
            row {
                label("<html><b>Automatic Registry</b><br>" +
                    "Automatically parse Rust files to find Rhai function registrations.</html>")
            }
            row {
                cell(autoRegistryCheckbox!!)
            }
            separator()
            group("Save detected registrations to:") {
                row {
                    cell(radioPanel)
                }
                row {
                    comment("Choose where auto-detected functions, variables, and types will be saved")
                }
            }
            separator()
            group("Scan Patterns") {
                row("Include patterns:") {
                    scrollCell(includePatternTextArea!!)
                        .align(Align.FILL)
                        .resizableColumn()
                }
                row {
                    comment("Glob patterns for files to scan (one per line)")
                }
                row("Exclude patterns:") {
                    scrollCell(excludePatternTextArea!!)
                        .align(Align.FILL)
                        .resizableColumn()
                }
                row {
                    comment("Glob patterns for files to exclude (one per line)")
                }
            }
            separator()
            group("Status") {
                row {
                    cell(autoRegistryInfoLabel!!)
                }
                row {
                    cell(rescanButton)
                }
            }
        }.apply {
            border = JBUI.Borders.empty(10)
        }
    }

    private fun updateAutoRegistryInfo() {
        val autoService = RhaiAutoRegistryService.getInstance(project)
        val functions = autoService.getAutoFunctions().size
        val variables = autoService.getAutoVariables().size
        val types = autoService.getAutoTypes().size
        val properties = autoService.getAutoProperties().size
        val destination = if (destinationGlobalRadio?.isSelected == true) "Global" else "Project"

        autoRegistryInfoLabel?.text = "<html>Auto-detected registrations (saved to <b>$destination</b> registry):<br>" +
            "&nbsp;&nbsp;Functions: $functions<br>" +
            "&nbsp;&nbsp;Variables: $variables<br>" +
            "&nbsp;&nbsp;Types: $types<br>" +
            "&nbsp;&nbsp;Properties: $properties</html>"
    }

    override fun isModified(): Boolean {
        val projectSettings = RhaiCustomRegistrySettings.getInstance(project)
        val globalSettings = RhaiGlobalRegistrySettings.getInstance()

        val currentDestination = if (destinationGlobalRadio?.isSelected == true) "global" else "project"

        return autoRegistryCheckbox?.isSelected != projectSettings.autoRegistryEnabled ||
            currentDestination != projectSettings.autoRegistryDestination ||
            projectFunctionsTextArea?.text != projectSettings.customFunctions ||
            projectVariablesTextArea?.text != projectSettings.customVariables ||
            projectTypesTextArea?.text != projectSettings.customTypes ||
            includePatternTextArea?.text != projectSettings.autoScanPatterns ||
            excludePatternTextArea?.text != projectSettings.autoScanExcludePatterns ||
            globalFunctionsTextArea?.text != globalSettings.globalFunctions ||
            globalVariablesTextArea?.text != globalSettings.globalVariables ||
            globalTypesTextArea?.text != globalSettings.globalTypes
    }

    override fun apply() {
        val projectSettings = RhaiCustomRegistrySettings.getInstance(project)
        projectSettings.autoRegistryEnabled = autoRegistryCheckbox?.isSelected ?: true
        projectSettings.autoRegistryDestination = if (destinationGlobalRadio?.isSelected == true) "global" else "project"
        projectSettings.customFunctions = projectFunctionsTextArea?.text ?: ""
        projectSettings.customVariables = projectVariablesTextArea?.text ?: ""
        projectSettings.customTypes = projectTypesTextArea?.text ?: ""
        projectSettings.autoScanPatterns = includePatternTextArea?.text ?: "**/*.rs"
        projectSettings.autoScanExcludePatterns = excludePatternTextArea?.text ?: ""

        val globalSettings = RhaiGlobalRegistrySettings.getInstance()
        globalSettings.globalFunctions = globalFunctionsTextArea?.text ?: ""
        globalSettings.globalVariables = globalVariablesTextArea?.text ?: ""
        globalSettings.globalTypes = globalTypesTextArea?.text ?: ""

        // Trigger rescan if auto-registry is enabled
        if (projectSettings.autoRegistryEnabled) {
            RhaiAutoRegistryService.getInstance(project).rescan()
        }
    }

    override fun reset() {
        val projectSettings = RhaiCustomRegistrySettings.getInstance(project)
        autoRegistryCheckbox?.isSelected = projectSettings.autoRegistryEnabled
        destinationProjectRadio?.isSelected = projectSettings.autoRegistryDestination != "global"
        destinationGlobalRadio?.isSelected = projectSettings.autoRegistryDestination == "global"
        projectFunctionsTextArea?.text = projectSettings.customFunctions
        projectVariablesTextArea?.text = projectSettings.customVariables
        projectTypesTextArea?.text = projectSettings.customTypes
        includePatternTextArea?.text = projectSettings.autoScanPatterns
        excludePatternTextArea?.text = projectSettings.autoScanExcludePatterns

        val globalSettings = RhaiGlobalRegistrySettings.getInstance()
        globalFunctionsTextArea?.text = globalSettings.globalFunctions
        globalVariablesTextArea?.text = globalSettings.globalVariables
        globalTypesTextArea?.text = globalSettings.globalTypes

        updateAutoRegistryInfo()
    }

    override fun disposeUIResources() {
        autoRegistryCheckbox = null
        destinationProjectRadio = null
        destinationGlobalRadio = null
        projectFunctionsTextArea = null
        projectVariablesTextArea = null
        projectTypesTextArea = null
        includePatternTextArea = null
        excludePatternTextArea = null
        globalFunctionsTextArea = null
        globalVariablesTextArea = null
        globalTypesTextArea = null
        autoRegistryInfoLabel = null
    }
}
