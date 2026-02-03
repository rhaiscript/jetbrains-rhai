package org.rhai.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import java.io.File

class RhaiRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<RhaiRunConfigurationOptions>(project, factory, name) {

    override fun getOptions(): RhaiRunConfigurationOptions {
        return super.getOptions() as RhaiRunConfigurationOptions
    }

    var scriptPath: String
        get() = options.scriptPath
        set(value) {
            options.scriptPath = value
        }

    var interpreterPath: String
        get() = options.interpreterPath
        set(value) {
            options.interpreterPath = value
        }

    var scriptArguments: String
        get() = options.scriptArguments
        set(value) {
            options.scriptArguments = value
        }

    var workingDirectory: String
        get() = options.workingDirectory
        set(value) {
            options.workingDirectory = value
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return RhaiRunConfigurationEditor(project)
    }

    override fun checkConfiguration() {
        if (scriptPath.isBlank()) {
            throw RuntimeConfigurationError("Script path is not specified")
        }
        if (!File(scriptPath).exists()) {
            throw RuntimeConfigurationError("Script file does not exist: $scriptPath")
        }
        if (interpreterPath.isBlank()) {
            throw RuntimeConfigurationError(
                "Interpreter path is not specified. Install via 'cargo install rhai-repl' or specify path to custom Rhai interpreter."
            )
        }
        // Only warn if it's an absolute path that doesn't exist
        // For simple command names like "rhai-run", let the OS resolve it at runtime
        if (interpreterPath.contains(File.separator) && !File(interpreterPath).exists()) {
            throw RuntimeConfigurationWarning("Interpreter file does not exist: $interpreterPath")
        }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler {
                val commandLine = GeneralCommandLine()
                    .withExePath(interpreterPath)
                    .withParameters(scriptPath)

                if (scriptArguments.isNotBlank()) {
                    commandLine.addParameters(scriptArguments.split("\\s+".toRegex()))
                }

                val effectiveWorkingDir = when {
                    workingDirectory.isNotBlank() -> workingDirectory
                    scriptPath.isNotBlank() -> File(scriptPath).parent
                    else -> project.basePath
                }
                effectiveWorkingDir?.let { commandLine.withWorkDirectory(it) }

                val processHandler = ProcessHandlerFactory.getInstance()
                    .createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }

    companion object {
        private val INTERPRETER_NAMES = listOf(
            "rhai-repl", "rhai-repl.exe",
            "rhai-run", "rhai-run.exe",
            "rhai", "rhai.exe"
        )

        fun findDefaultInterpreter(): String? {
            val pathEnv = System.getenv("PATH") ?: return null
            val pathSeparator = File.pathSeparator

            for (dir in pathEnv.split(pathSeparator)) {
                for (name in INTERPRETER_NAMES) {
                    val file = File(dir, name)
                    if (file.exists() && file.canExecute()) {
                        return file.absolutePath
                    }
                }
            }
            return null
        }
    }
}
