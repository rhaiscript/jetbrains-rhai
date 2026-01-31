package org.rhai.features

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import org.rhai.RhaiLetDeclaration

class RhaiDocumentationProvider : DocumentationProvider {
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return when (element) {
            is RhaiLetDeclaration -> {
                """
                <b>Let Statement</b><br/>
                <b>Expression:</b> ${element.expression?.text}<br/>
                <b>Scope:</b> ${element.containingFile?.name}
                """.trimIndent()
            }

            else -> null
        }
    }
}
