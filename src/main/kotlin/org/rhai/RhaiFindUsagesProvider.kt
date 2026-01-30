package org.rhai

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import org.rhai.RhaiLetDeclaration
import org.rhai.RhaiConstDeclaration
import org.rhai.RhaiFunctionDefinition

class RhaiFindUsagesProvider : FindUsagesProvider {
    override fun canFindUsagesFor(element: PsiElement): Boolean {
        return element is RhaiLetDeclaration ||
                element is RhaiConstDeclaration ||
                element is RhaiFunctionDefinition
    }

    override fun getHelpId(element: PsiElement): String? = null

    override fun getType(element: PsiElement): String {
        return when (element) {
            is RhaiLetDeclaration -> "variable"
            is RhaiConstDeclaration -> "constant"
            is RhaiFunctionDefinition -> "function"
//            is RhaiIdentifier -> "identifier"
            else -> "unknown"
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return element.text
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return getDescriptiveName(element)
    }
}
