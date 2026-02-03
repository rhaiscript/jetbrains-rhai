package org.rhai.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.rhai.*
import org.rhai.lang.RhaiFile

class RhaiUnusedVariableInspection : RhaiInspectionBase() {

    override fun getDisplayName(): String = "Unused variable"

    override fun getStaticDescription(): String = "Reports variables that are declared but never used."

    override fun buildRhaiVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is RhaiLetDeclaration) {
                    checkUnusedVariable(element, holder)
                }
            }
        }
    }

    private fun checkUnusedVariable(letDecl: RhaiLetDeclaration, holder: ProblemsHolder) {
        val file = letDecl.containingFile as? RhaiFile ?: return
        val varName = letDecl.pattern.text

        // Skip if it starts with underscore (intentionally unused)
        if (varName.startsWith("_")) return

        // Find all usages of this variable
        val usages = findUsages(file, varName, letDecl.textOffset)

        if (usages.isEmpty()) {
            holder.registerProblem(
                letDecl.pattern,
                "Variable '$varName' is never used",
                ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                PrefixWithUnderscoreFix(varName),
                RemoveVariableFix()
            )
        }
    }

    private fun findUsages(file: RhaiFile, name: String, declOffset: Int): List<PsiElement> {
        val usages = mutableListOf<PsiElement>()

        PsiTreeUtil.processElements(file) { element ->
            if (element.elementType == RhaiTypes.IDENTIFIER &&
                element.text == name &&
                element.textOffset > declOffset &&
                !isDeclaration(element)) {
                usages.add(element)
            }
            true
        }

        return usages
    }

    private fun isDeclaration(element: PsiElement): Boolean {
        val parent = element.parent
        return parent is RhaiLetDeclaration ||
                parent is RhaiConstDeclaration ||
                parent is RhaiFunctionDefinition ||
                parent is RhaiPattern
    }

    private class PrefixWithUnderscoreFix(private val varName: String) : LocalQuickFix {
        override fun getName(): String = "Rename to '_$varName'"

        override fun getFamilyName(): String = "Rename unused variable"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            val document = element.containingFile.viewProvider.document ?: return

            document.replaceString(
                element.textRange.startOffset,
                element.textRange.endOffset,
                "_$varName"
            )
        }
    }

    private class RemoveVariableFix : LocalQuickFix {
        override fun getName(): String = "Remove unused variable"

        override fun getFamilyName(): String = "Remove unused variable"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val pattern = descriptor.psiElement
            val letDecl = pattern.parent as? RhaiLetDeclaration ?: return

            // Remove the entire let declaration
            letDecl.delete()
        }
    }
}
