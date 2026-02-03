package org.rhai.features

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import org.rhai.*
import org.rhai.lang.RhaiLanguage

/**
 * Provides breadcrumb navigation for Rhai files.
 * Shows the hierarchy of functions, modules, and control structures.
 */
class RhaiBreadcrumbsProvider : BreadcrumbsProvider {

    override fun getLanguages(): Array<Language> = arrayOf(RhaiLanguage.INSTANCE)

    override fun acceptElement(element: PsiElement): Boolean {
        return element is RhaiFunctionDefinition ||
                element is RhaiModuleDeclaration ||
                element is RhaiIfStatement ||
                element is RhaiForStatement ||
                element is RhaiWhileStatement ||
                element is RhaiLoopStatement ||
                element is RhaiSwitchExpression ||
                element is RhaiTryCatchStatement ||
                element is RhaiClosureExpr ||
                element is RhaiBlock
    }

    override fun getElementInfo(element: PsiElement): String {
        return when (element) {
            is RhaiFunctionDefinition -> {
                val visibility = if (element.visibility?.text == "private") "private " else ""
                val name = element.identifier.text
                val params = element.parameters?.parameterList?.joinToString(", ") {
                    it.pattern.text
                } ?: ""
                "${visibility}fn $name($params)"
            }

            is RhaiModuleDeclaration -> {
                val name = element.node.findChildByType(RhaiTypes.IDENTIFIER)?.text ?: "module"
                "module $name"
            }

            is RhaiIfStatement -> "if"

            is RhaiForStatement -> {
                val varName = element.node.findChildByType(RhaiTypes.IDENTIFIER)?.text ?: "..."
                "for $varName"
            }

            is RhaiWhileStatement -> "while"

            is RhaiLoopStatement -> "loop"

            is RhaiSwitchExpression -> "switch"

            is RhaiTryCatchStatement -> "try"

            is RhaiClosureExpr -> {
                val params = element.closureParams?.closureParamList?.joinToString(", ") {
                    it.pattern.text
                } ?: ""
                "|$params|"
            }

            is RhaiBlock -> {
                // Don't show standalone blocks, they're usually part of other structures
                "{...}"
            }

            else -> element.text.take(20)
        }
    }

    override fun getElementTooltip(element: PsiElement): String? {
        return when (element) {
            is RhaiFunctionDefinition -> {
                val docComment = findDocComment(element)
                if (docComment != null) {
                    docComment.text.lines()
                        .map { it.removePrefix("///").removePrefix("//!").trim() }
                        .filter { it.isNotEmpty() }
                        .take(3)
                        .joinToString("\n")
                } else {
                    "Function: ${element.identifier.text}"
                }
            }

            is RhaiModuleDeclaration -> {
                val name = element.node.findChildByType(RhaiTypes.IDENTIFIER)?.text
                "Module: $name"
            }

            else -> null
        }
    }

    private fun findDocComment(element: PsiElement): PsiElement? {
        var prev = element.prevSibling
        while (prev != null) {
            if (prev.node.elementType == RhaiTypes.DOC_LINE ||
                prev.node.elementType == RhaiTypes.DOC_COMMENT
            ) {
                return prev
            }
            if (prev.text.isNotBlank() &&
                prev.node.elementType != RhaiTypes.LINE_COMMENT
            ) {
                break
            }
            prev = prev.prevSibling
        }
        return null
    }
}
