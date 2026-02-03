package org.rhai.run

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent
import javax.swing.JPanel

class RhaiRunConfigurationEditor(private val project: Project) : SettingsEditor<RhaiRunConfiguration>() {
    private val scriptPathField = TextFieldWithBrowseButton()
    private val interpreterPathField = TextFieldWithBrowseButton()
    private val scriptArgumentsField = JBTextField()
    private val workingDirectoryField = TextFieldWithBrowseButton()

    private val panel: JPanel

    init {
        scriptPathField.addBrowseFolderListener(
            "Select Rhai Script",
            "Select the Rhai script to run",
            project,
            FileChooserDescriptorFactory.createSingleFileDescriptor("rhai")
        )

        interpreterPathField.addBrowseFolderListener(
            "Select Rhai Interpreter",
            "Select the Rhai interpreter executable (rhai-run or custom binary)",
            project,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
        )

        workingDirectoryField.addBrowseFolderListener(
            "Select Working Directory",
            "Select the working directory for script execution",
            project,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )

        panel = panel {
            row("Script:") {
                cell(scriptPathField)
                    .align(AlignX.FILL)
                    .resizableColumn()
                    .comment("Path to the .rhai script file")
            }
            row("Interpreter:") {
                cell(interpreterPathField)
                    .align(AlignX.FILL)
                    .resizableColumn()
                    .comment("Path to rhai-run or custom Rhai interpreter")
            }
            row("Arguments:") {
                cell(scriptArgumentsField)
                    .align(AlignX.FILL)
                    .resizableColumn()
                    .comment("Arguments to pass to the script")
            }
            row("Working directory:") {
                cell(workingDirectoryField)
                    .align(AlignX.FILL)
                    .resizableColumn()
                    .comment("Working directory for script execution (defaults to script directory)")
            }
        }
    }

    override fun resetEditorFrom(configuration: RhaiRunConfiguration) {
        scriptPathField.text = configuration.scriptPath
        interpreterPathField.text = configuration.interpreterPath.ifBlank {
            RhaiRunConfiguration.findDefaultInterpreter() ?: "rhai-repl"
        }
        scriptArgumentsField.text = configuration.scriptArguments
        workingDirectoryField.text = configuration.workingDirectory
    }

    override fun applyEditorTo(configuration: RhaiRunConfiguration) {
        configuration.scriptPath = scriptPathField.text
        configuration.interpreterPath = interpreterPathField.text
        configuration.scriptArguments = scriptArgumentsField.text
        configuration.workingDirectory = workingDirectoryField.text
    }

    override fun createEditor(): JComponent = panel
}
