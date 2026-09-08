package org.rhai.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import org.rhai.registry.RhaiRegistration
import org.rhai.registry.RhaiRegistrationParser
import org.rhai.registry.RegistrationType
import org.rhai.settings.RhaiCustomRegistrySettings
import org.rhai.settings.RhaiGlobalRegistrySettings
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Action to add a Rhai function/variable registration to the registry.
 * Available via right-click context menu on Rust code.
 */
class AddToRhaiRegistryAction : AnAction(
    "Add to Rhai Registry",
    "Parse and add this Rhai registration to the plugin registry",
    null
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document

        // Get the current line
        val caretOffset = editor.caretModel.offset
        val lineNumber = document.getLineNumber(caretOffset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))

        // Try to parse the line
        val registration = RhaiRegistrationParser.parseSingleLine(lineText)

        if (registration != null) {
            // Successfully parsed - show scope selection dialog
            showScopeDialog(project, registration)
        } else {
            // Failed to parse - show warning dialog
            showParseErrorDialog(project, lineText)
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)

        // Only show for Rust files
        e.presentation.isEnabledAndVisible = editor != null &&
                file != null &&
                file.extension == "rs"
    }

    // update() only reads data keys (no Swing state), so it can run off the EDT.
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    private fun showScopeDialog(project: com.intellij.openapi.project.Project, registration: RhaiRegistration) {
        val dialog = ScopeSelectionDialog(project, registration)
        if (dialog.showAndGet()) {
            val scope = dialog.getSelectedScope()
            addToRegistry(project, registration, scope)

            Messages.showInfoMessage(
                project,
                "Added '${registration.name}' to ${scope.displayName} registry.",
                "Success"
            )
        }
    }

    private fun showParseErrorDialog(project: com.intellij.openapi.project.Project, lineText: String) {
        val dialog = ParseErrorDialog(project, lineText)
        val result = dialog.showAndGet()

        if (result && dialog.shouldForceAdd()) {
            // Force add - ask for name and type
            val name = Messages.showInputDialog(
                project,
                "Enter the function/variable name to register:",
                "Add to Registry",
                null
            )

            if (!name.isNullOrBlank()) {
                if (!RhaiRegistrationParser.isValidRhaiIdentifier(name)) {
                    Messages.showWarningDialog(
                        project,
                        "'$name' is not a valid Rhai identifier.",
                        "Invalid Name"
                    )
                    return
                }

                val typeOptions = arrayOf("Function", "Variable", "Type")
                val typeChoice = Messages.showDialog(
                    project,
                    "Select the type of registration:",
                    "Registration Type",
                    typeOptions,
                    0,
                    Messages.getQuestionIcon()
                )

                if (typeChoice >= 0) {
                    val regType = when (typeChoice) {
                        0 -> RegistrationType.FUNCTION
                        1 -> RegistrationType.VARIABLE
                        2 -> RegistrationType.TYPE
                        else -> RegistrationType.FUNCTION
                    }

                    val registration = RhaiRegistration(name, regType)
                    val scopeDialog = ScopeSelectionDialog(project, registration)

                    if (scopeDialog.showAndGet()) {
                        addToRegistry(project, registration, scopeDialog.getSelectedScope())
                        Messages.showInfoMessage(
                            project,
                            "Added '$name' to registry.",
                            "Success"
                        )
                    }
                }
            }
        }
    }

    private fun addToRegistry(
        project: com.intellij.openapi.project.Project,
        registration: RhaiRegistration,
        scope: RegistryScope
    ) {
        when (scope) {
            RegistryScope.GLOBAL -> {
                val settings = RhaiGlobalRegistrySettings.getInstance()
                when (registration.type) {
                    RegistrationType.FUNCTION -> settings.addGlobalFunction(registration.name)
                    RegistrationType.VARIABLE -> settings.addGlobalVariable(registration.name)
                    RegistrationType.TYPE -> settings.addGlobalType(registration.name)
                    else -> settings.addGlobalFunction(registration.name)
                }
            }
            RegistryScope.PROJECT -> {
                val settings = RhaiCustomRegistrySettings.getInstance(project)
                when (registration.type) {
                    RegistrationType.FUNCTION -> settings.addCustomFunction(registration.name)
                    RegistrationType.VARIABLE -> settings.addCustomVariable(registration.name)
                    RegistrationType.TYPE -> settings.addCustomType(registration.name)
                    else -> settings.addCustomFunction(registration.name)
                }
            }
        }
    }
}

/**
 * Dialog for selecting registry scope (global vs project)
 */
private class ScopeSelectionDialog(
    private val project: com.intellij.openapi.project.Project,
    private val registration: RhaiRegistration
) : DialogWrapper(project) {

    private val projectRadio = JBRadioButton("Project only (${project.name})", true)
    private val globalRadio = JBRadioButton("Global (all projects)", false)

    init {
        title = "Add to Rhai Registry"
        val buttonGroup = ButtonGroup()
        buttonGroup.add(projectRadio)
        buttonGroup.add(globalRadio)
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(10)
        }

        mainPanel.add(JBLabel("Adding: ${registration.name}"))
        mainPanel.add(JBLabel("Type: ${registration.type.name.lowercase().replaceFirstChar { it.uppercase() }}"))
        mainPanel.add(javax.swing.Box.createVerticalStrut(10))
        mainPanel.add(JBLabel("Select scope:"))
        mainPanel.add(projectRadio)
        mainPanel.add(globalRadio)

        return mainPanel
    }

    fun getSelectedScope(): RegistryScope {
        return if (globalRadio.isSelected) RegistryScope.GLOBAL else RegistryScope.PROJECT
    }
}

/**
 * Dialog shown when parsing fails
 */
private class ParseErrorDialog(
    project: com.intellij.openapi.project.Project,
    private val lineText: String
) : DialogWrapper(project) {

    private var forceAdd = false

    init {
        title = "Parse Error"
        setOKButtonText("Add Anyway")
        setCancelButtonText("Cancel")
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                label("Could not parse Rhai registration from this line:")
            }
            row {
                val displayText = if (lineText.length > 60) {
                    lineText.take(60) + "..."
                } else {
                    lineText
                }
                cell(JBLabel("<html><code>${displayText.replace("<", "&lt;")}</code></html>"))
            }
            separator()
            row {
                label("Expected patterns like:")
            }
            row {
                label("  engine.register_fn(\"name\", func)")
            }
            row {
                label("  module.set_fn(\"name\", func)")
            }
            row {
                label("  engine.register_type::<Type>()")
            }
        }.apply {
            border = JBUI.Borders.empty(10)
        }
    }

    override fun doOKAction() {
        forceAdd = true
        super.doOKAction()
    }

    fun shouldForceAdd(): Boolean = forceAdd
}

/**
 * Registry scope options
 */
enum class RegistryScope(val displayName: String) {
    GLOBAL("Global"),
    PROJECT("Project")
}
