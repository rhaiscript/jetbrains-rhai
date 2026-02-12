package org.rhai.settings

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import org.rhai.lang.RhaiLanguage
import org.rhai.registry.RhaiAutoRegistryService
import org.rhai.registry.RhaiRegistryCodeParser
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Configuration UI for Rhai registry settings.
 * Supports both project-specific and global scopes with Rhai code editors.
 */
class RhaiCustomRegistryConfigurable(private val project: Project) : Configurable {

    // Code editors for Rhai code
    private var projectCodeEditor: LanguageTextField? = null
    private var globalCodeEditor: LanguageTextField? = null

    // Scope inheritance checkbox
    private var inheritGlobalScopeCheckbox: JBCheckBox? = null

    // Auto-registry components
    private var autoRegistryCheckbox: JBCheckBox? = null
    private var destinationProjectRadio: JBRadioButton? = null
    private var destinationGlobalRadio: JBRadioButton? = null
    private var includePatternTextArea: JBTextArea? = null
    private var excludePatternTextArea: JBTextArea? = null
    private var autoRegistryInfoLabel: JBLabel? = null

    override fun getDisplayName(): String = "Rhai Custom Registry"

    override fun createComponent(): JComponent {
        val tabbedPane = JBTabbedPane()

        tabbedPane.addTab("Project Scope", createProjectPanel())
        tabbedPane.addTab("Global Scope", createGlobalPanel())
        tabbedPane.addTab("Auto Registry", createAutoRegistryPanel())

        reset()
        return tabbedPane
    }

    private fun createProjectPanel(): JComponent {
        projectCodeEditor = createRhaiCodeEditor()

        inheritGlobalScopeCheckbox = JBCheckBox(
            "Inherit global scope (include global definitions in this project)",
            true
        )

        return panel {
            row {
                label("<html><b>Project Scope</b><br>" +
                    "Define functions, variables, and constants available in this project.<br>" +
                    "Use standard Rhai syntax.</html>")
            }
            row {
                cell(inheritGlobalScopeCheckbox!!)
            }
            separator()
            group("Rhai Code") {
                row {
                    scrollCell(projectCodeEditor!!)
                        .align(Align.FILL)
                        .resizableColumn()
                }.resizableRow()
                row {
                    comment("""
                        <html>
                        Examples:<br>
                        <code>fn calculate(x, y) { x + y }</code><br>
                        <code>let config_value;</code><br>
                        <code>const MAX_SIZE = 100;</code><br>
                        <code>// type: MyCustomType</code>
                        </html>
                    """.trimIndent())
                }
            }
        }.apply {
            border = JBUI.Borders.empty(10)
        }
    }

    private fun createGlobalPanel(): JComponent {
        globalCodeEditor = createRhaiCodeEditor()

        return panel {
            row {
                label("<html><b>Global Scope</b><br>" +
                    "Define functions, variables, and constants available in ALL projects.<br>" +
                    "Projects can inherit these definitions via the \"Inherit global scope\" option.</html>")
            }
            group("Rhai Code") {
                row {
                    scrollCell(globalCodeEditor!!)
                        .align(Align.FILL)
                        .resizableColumn()
                }.resizableRow()
            }
        }.apply {
            border = JBUI.Borders.empty(10)
        }
    }

    /**
     * Create a syntax-highlighted Rhai code editor
     */
    private fun createRhaiCodeEditor(): LanguageTextField {
        return object : LanguageTextField(
            RhaiLanguage.INSTANCE,
            project,
            "",
            false
        ) {
            override fun createEditor(): EditorEx {
                val editor = super.createEditor()
                editor.settings.apply {
                    isLineNumbersShown = true
                    isWhitespacesShown = false
                    isFoldingOutlineShown = true
                    isAutoCodeFoldingEnabled = true
                    additionalLinesCount = 3
                    additionalColumnsCount = 3
                    isRightMarginShown = false
                }
                editor.setVerticalScrollbarVisible(true)
                editor.setHorizontalScrollbarVisible(true)
                return editor
            }
        }.apply {
            preferredSize = java.awt.Dimension(600, 300)
            setOneLineMode(false)
        }
    }

    private fun createAutoRegistryPanel(): JComponent {
        autoRegistryCheckbox = JBCheckBox("Enable automatic registry scanning", true)
        destinationProjectRadio = JBRadioButton("Project registry (only this project)", true)
        destinationGlobalRadio = JBRadioButton("Global registry (all projects)", false)

        val destinationGroup = ButtonGroup()
        destinationGroup.add(destinationProjectRadio)
        destinationGroup.add(destinationGlobalRadio)

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

        return projectCodeEditor?.text != projectSettings.projectRhaiCode ||
            globalCodeEditor?.text != globalSettings.globalRhaiCode ||
            inheritGlobalScopeCheckbox?.isSelected != projectSettings.inheritGlobalScope ||
            autoRegistryCheckbox?.isSelected != projectSettings.autoRegistryEnabled ||
            currentDestination != projectSettings.autoRegistryDestination ||
            includePatternTextArea?.text != projectSettings.autoScanPatterns ||
            excludePatternTextArea?.text != projectSettings.autoScanExcludePatterns
    }

    override fun apply() {
        val projectSettings = RhaiCustomRegistrySettings.getInstance(project)
        projectSettings.projectRhaiCode = projectCodeEditor?.text ?: ""
        projectSettings.inheritGlobalScope = inheritGlobalScopeCheckbox?.isSelected ?: true
        projectSettings.autoRegistryEnabled = autoRegistryCheckbox?.isSelected ?: true
        projectSettings.autoRegistryDestination = if (destinationGlobalRadio?.isSelected == true) "global" else "project"
        projectSettings.autoScanPatterns = includePatternTextArea?.text ?: "**/*.rs"
        projectSettings.autoScanExcludePatterns = excludePatternTextArea?.text ?: ""

        val globalSettings = RhaiGlobalRegistrySettings.getInstance()
        globalSettings.globalRhaiCode = globalCodeEditor?.text ?: ""

        // Invalidate parser cache
        RhaiRegistryCodeParser.getInstance(project).invalidateCache()

        // Trigger rescan if auto-registry is enabled
        if (projectSettings.autoRegistryEnabled) {
            RhaiAutoRegistryService.getInstance(project).rescan()
        }
    }

    override fun reset() {
        val projectSettings = RhaiCustomRegistrySettings.getInstance(project)
        projectCodeEditor?.text = projectSettings.projectRhaiCode
        inheritGlobalScopeCheckbox?.isSelected = projectSettings.inheritGlobalScope
        autoRegistryCheckbox?.isSelected = projectSettings.autoRegistryEnabled
        destinationProjectRadio?.isSelected = projectSettings.autoRegistryDestination != "global"
        destinationGlobalRadio?.isSelected = projectSettings.autoRegistryDestination == "global"
        includePatternTextArea?.text = projectSettings.autoScanPatterns
        excludePatternTextArea?.text = projectSettings.autoScanExcludePatterns

        val globalSettings = RhaiGlobalRegistrySettings.getInstance()
        globalCodeEditor?.text = globalSettings.globalRhaiCode

        updateAutoRegistryInfo()
    }

    override fun disposeUIResources() {
        projectCodeEditor = null
        globalCodeEditor = null
        inheritGlobalScopeCheckbox = null
        autoRegistryCheckbox = null
        destinationProjectRadio = null
        destinationGlobalRadio = null
        includePatternTextArea = null
        excludePatternTextArea = null
        autoRegistryInfoLabel = null
    }
}
