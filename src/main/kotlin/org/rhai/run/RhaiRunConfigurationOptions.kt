package org.rhai.run

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class RhaiRunConfigurationOptions : RunConfigurationOptions() {
    private val scriptPathOption: StoredProperty<String?> = string("").provideDelegate(this, "scriptPath")
    private val interpreterPathOption: StoredProperty<String?> = string("").provideDelegate(this, "interpreterPath")
    private val scriptArgumentsOption: StoredProperty<String?> = string("").provideDelegate(this, "scriptArguments")
    private val workingDirectoryOption: StoredProperty<String?> = string("").provideDelegate(this, "workingDirectory")

    var scriptPath: String
        get() = scriptPathOption.getValue(this) ?: ""
        set(value) = scriptPathOption.setValue(this, value)

    var interpreterPath: String
        get() = interpreterPathOption.getValue(this) ?: ""
        set(value) = interpreterPathOption.setValue(this, value)

    var scriptArguments: String
        get() = scriptArgumentsOption.getValue(this) ?: ""
        set(value) = scriptArgumentsOption.setValue(this, value)

    var workingDirectory: String
        get() = workingDirectoryOption.getValue(this) ?: ""
        set(value) = workingDirectoryOption.setValue(this, value)
}
