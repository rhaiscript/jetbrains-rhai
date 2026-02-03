package org.rhai.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import org.rhai.lang.RhaiFile

class RhaiRunConfigurationProducer : LazyRunConfigurationProducer<RhaiRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory {
        return RhaiRunConfigurationType.INSTANCE.configurationFactories[0]
    }

    override fun isConfigurationFromContext(
        configuration: RhaiRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val rhaiFile = getRhaiFile(context) ?: return false
        val scriptPath = rhaiFile.virtualFile?.path ?: return false
        return configuration.scriptPath == scriptPath
    }

    override fun setupConfigurationFromContext(
        configuration: RhaiRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val rhaiFile = getRhaiFile(context) ?: return false
        val virtualFile = rhaiFile.virtualFile ?: return false

        configuration.scriptPath = virtualFile.path
        configuration.name = virtualFile.nameWithoutExtension

        // Try to find default interpreter or use "rhai-repl" as default
        if (configuration.interpreterPath.isBlank()) {
            configuration.interpreterPath = RhaiRunConfiguration.findDefaultInterpreter() ?: "rhai-repl"
        }

        // Set working directory to project base path
        context.project.basePath?.let {
            configuration.workingDirectory = it
        }

        return true
    }

    private fun getRhaiFile(context: ConfigurationContext): RhaiFile? {
        val location = context.location ?: return null
        val psiFile = location.psiElement.containingFile
        return psiFile as? RhaiFile
    }
}
