package org.rhai.features

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import org.rhai.lang.RhaiFile

class RhaiLiveTemplateContext : TemplateContextType("Rhai") {

    override fun isInContext(context: TemplateActionContext): Boolean {
        return context.file is RhaiFile
    }
}
