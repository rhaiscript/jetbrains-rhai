package org.rhai.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Configuration UI for custom Rhai registry settings.
 * Allows users to specify custom functions, variables, and types
 * that are registered from Rust code.
 */
class RhaiCustomRegistryConfigurable(private val project: Project) : Configurable {

    private var functionsTextArea: JBTextArea? = null
    private var variablesTextArea: JBTextArea? = null
    private var typesTextArea: JBTextArea? = null

    override fun getDisplayName(): String = "Rhai Custom Registry"

    override fun createComponent(): JComponent {
        functionsTextArea = JBTextArea(8, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }

        variablesTextArea = JBTextArea(8, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }

        typesTextArea = JBTextArea(5, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }

        return FormBuilder.createFormBuilder()
            .addComponent(JBLabel("<html><b>Custom Rhai Registry</b><br>" +
                "Define custom functions, variables, and types registered from Rust.<br>" +
                "Enter names separated by commas or newlines.</html>"))
            .addVerticalGap(10)
            .addLabeledComponent(
                JBLabel("Custom Functions:"),
                JBScrollPane(functionsTextArea),
                true
            )
            .addComponentToRightColumn(
                JBLabel("<html><small>e.g.: my_rust_func, calculate_something, get_config</small></html>")
            )
            .addVerticalGap(10)
            .addLabeledComponent(
                JBLabel("Custom Variables:"),
                JBScrollPane(variablesTextArea),
                true
            )
            .addComponentToRightColumn(
                JBLabel("<html><small>e.g.: MY_CONSTANT, config_value, app_state</small></html>")
            )
            .addVerticalGap(10)
            .addLabeledComponent(
                JBLabel("Custom Types:"),
                JBScrollPane(typesTextArea),
                true
            )
            .addComponentToRightColumn(
                JBLabel("<html><small>e.g.: MyStruct, CustomType, AppConfig</small></html>")
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val settings = RhaiCustomRegistrySettings.getInstance(project)
        return functionsTextArea?.text != settings.customFunctions ||
                variablesTextArea?.text != settings.customVariables ||
                typesTextArea?.text != settings.customTypes
    }

    override fun apply() {
        val settings = RhaiCustomRegistrySettings.getInstance(project)
        settings.customFunctions = functionsTextArea?.text ?: ""
        settings.customVariables = variablesTextArea?.text ?: ""
        settings.customTypes = typesTextArea?.text ?: ""
    }

    override fun reset() {
        val settings = RhaiCustomRegistrySettings.getInstance(project)
        functionsTextArea?.text = settings.customFunctions
        variablesTextArea?.text = settings.customVariables
        typesTextArea?.text = settings.customTypes
    }

    override fun disposeUIResources() {
        functionsTextArea = null
        variablesTextArea = null
        typesTextArea = null
    }
}
