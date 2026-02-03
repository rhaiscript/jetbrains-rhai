package org.rhai.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.rhai.RhaiFunctionDefinition
import org.rhai.lang.RhaiFile

class RhaiDuplicateFunctionInspection : RhaiInspectionBase() {

    override fun getDisplayName(): String = "Duplicate function definition"

    override fun getStaticDescription(): String = "Reports functions with the same name defined multiple times."

    override fun buildRhaiVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            private val seenFunctions = mutableMapOf<String, RhaiFunctionDefinition>()

            override fun visitElement(element: PsiElement) {
                if (element is RhaiFile) {
                    checkDuplicateFunctions(element, holder)
                }
            }

            private fun checkDuplicateFunctions(file: RhaiFile, holder: ProblemsHolder) {
                val functions = PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java)
                val seen = mutableMapOf<String, MutableList<RhaiFunctionDefinition>>()

                for (func in functions) {
                    val name = func.identifier.text
                    val paramCount = func.parameters?.parameterList?.size ?: 0
                    val key = "$name/$paramCount"

                    seen.getOrPut(key) { mutableListOf() }.add(func)
                }

                for ((_, funcs) in seen) {
                    if (funcs.size > 1) {
                        // Report all duplicates except the first one
                        for (i in 1 until funcs.size) {
                            val func = funcs[i]
                            holder.registerProblem(
                                func.identifier,
                                "Function '${func.identifier.text}' is already defined",
                                ProblemHighlightType.GENERIC_ERROR
                            )
                        }
                    }
                }
            }
        }
    }
}
